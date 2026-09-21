package com.davidegea.spotifystats.data.importer

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.davidegea.spotifystats.database.SpotifyStatsDatabase
import com.davidegea.spotifystats.domain.model.ImportDocument
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ImportDatabaseTest {
    private lateinit var db: SpotifyStatsDatabase
    private lateinit var context: Context
    private lateinit var repository: RoomSpotifyHistoryImportRepository
    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, SpotifyStatsDatabase::class.java).allowMainThreadQueries().build()
        repository = RoomSpotifyHistoryImportRepository(context, db, db.importDao())
    }
    @After fun close() { db.close() }
    private fun record(ms: Long = 30_000) = """{"ts":"2026-09-20T12:00:00Z","ms_played":$ms,"master_metadata_track_name":"Song","master_metadata_album_artist_name":"Artist","spotify_track_uri":"spotify:track:abc","ip_addr":"PRIVATE-IP","user_agent":"PRIVATE-AGENT"}"""
    private fun document(text: String): ImportDocument {
        val f = File.createTempFile("history-", ".json", context.cacheDir).apply { writeText(text) }
        return ImportDocument(Uri.fromFile(f).toString())
    }
    @Test fun jsonAndZipReimportsAreIdempotentInRoom() = runBlocking {
        val json = document("[${record()}]")
        assertEquals(1L, repository.importDocuments(listOf(json)) {}.insertedEvents)
        val zip = File.createTempFile("history-", ".zip", context.cacheDir)
        ZipOutputStream(zip.outputStream()).use { it.putNextEntry(ZipEntry("history.json")); it.write("[${record()}]".toByteArray()); it.closeEntry() }
        val again = repository.importDocuments(listOf(ImportDocument(Uri.fromFile(zip).toString()), json)) {}
        assertEquals(0L, again.insertedEvents); assertEquals(2L, again.duplicateEvents)
        assertEquals(1L, db.listeningHistoryDao().observeOverviewStats(0, Long.MAX_VALUE).first().totalPlays)
        db.openHelper.readableDatabase.query("SELECT * FROM play_events").use { cursor -> assertFalse(cursor.columnNames.any { it.contains("ip_") || it.contains("user_agent") }) }
    }
    @Test fun abortedBatchDoesNotReportRolledBackInserts() = runBlocking {
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fail_test BEFORE INSERT ON play_events WHEN NEW.ms_played=888 BEGIN SELECT RAISE(ABORT,'test failure'); END")
        val result = repository.importDocuments(listOf(document("[${record()},${record(888)}]"))) {}
        assertEquals(1, result.failedDocuments); assertEquals(0L, result.insertedEvents)
        assertEquals(0L, db.listeningHistoryDao().observeOverviewStats(0, Long.MAX_VALUE).first().totalPlays)
    }
    @Test fun cancellationIsNotReportedAsAFileFailure() = runBlocking {
        val text = (1..500).joinToString(",", "[", "]") { record(it.toLong()) }
        try {
            repository.importDocuments(listOf(document(text))) { throw CancellationException("test cancellation") }
            fail("Cancellation must escape")
        } catch (_: CancellationException) { }
    }
    @Test fun rejectsNegativeAndImpossibleDurations() = runBlocking {
        val result = repository.importDocuments(listOf(document("[${record(-1)},${record(86_400_001)}]"))) {}
        assertEquals(2L, result.skippedRecords); assertEquals(0L, result.insertedEvents)
    }
}
