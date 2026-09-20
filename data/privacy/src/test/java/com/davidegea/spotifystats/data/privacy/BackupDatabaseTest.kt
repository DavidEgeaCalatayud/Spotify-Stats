package com.davidegea.spotifystats.data.privacy

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.davidegea.spotifystats.database.SpotifyStatsDatabase
import com.davidegea.spotifystats.database.entity.*
import com.davidegea.spotifystats.model.PlaySource
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
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BackupDatabaseTest {
    private lateinit var db: SpotifyStatsDatabase
    private lateinit var context: Context
    private lateinit var repository: LocalDataControlRepository
    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, SpotifyStatsDatabase::class.java).allowMainThreadQueries().build()
        repository = LocalDataControlRepository(context, db)
    }
    @After fun close() { db.close() }
    private suspend fun seed() {
        db.importDao().insertArtist(ArtistEntity(1, null, "a", "Artist", "artist"))
        db.importDao().insertTrack(TrackEntity(1, "spotify:track:a", "t", "Song", null, null))
        db.importDao().insertTrackArtist(TrackArtistCrossRef(1, 1, 0))
        db.importDao().insertPlayEvent(PlayEventEntity(1, "a".repeat(64), 1, 100_000, 30_000, null, null, null, null, null, null, null, null, PlaySource.SPOTIFY_EXPORT))
    }
    private fun target(): File = File.createTempFile("backup-", ".zip", context.cacheDir)
    private fun File.uri() = Uri.fromFile(this).toString()
    private fun rewrite(source: File, transform: (String) -> String): File {
        val text = ZipInputStream(source.inputStream()).use { it.nextEntry; it.reader().readText() }
        return target().apply { ZipOutputStream(outputStream()).use { it.putNextEntry(ZipEntry("history.json")); it.write(transform(text).toByteArray()); it.closeEntry() } }
    }
    @Test fun roundTripPreservesNullsEventsAndRebuiltSearch() = runBlocking {
        seed(); val file = target(); repository.exportBackup(file.uri())
        repository.deleteHistory()
        assertEquals(0L, db.listeningHistoryDao().observeOverviewStats(0, Long.MAX_VALUE).first().totalPlays)
        repository.restoreBackup(file.uri())
        assertEquals(1L, db.listeningHistoryDao().observeOverviewStats(0, Long.MAX_VALUE).first().totalPlays)
        assertEquals(0L, db.listeningHistoryDao().observePlaybackBehavior(0, Long.MAX_VALUE).first().skipKnownEvents)
        assertEquals(1, db.explorationDao().search("Song", 0, Long.MAX_VALUE).first().size)
        assertEquals(0, context.cacheDir.listFiles().orEmpty().count { it.name.startsWith("restore-") })
    }
    @Test fun invalidVersionAndLateInvalidEventLeaveOriginalHistoryUntouched() = runBlocking {
        seed(); val valid = target(); repository.exportBackup(valid.uri())
        val badVersion = rewrite(valid) { it.replace("\"version\":1", "\"version\":9") }
        val badEvent = rewrite(valid) { it.replace("\"ms_played\":30000", "\"ms_played\":-1") }
        for (file in listOf(badVersion, badEvent)) {
            try { repository.restoreBackup(file.uri()); fail("Invalid backup must be rejected") } catch (_: IllegalArgumentException) { }
            assertEquals(1L, db.listeningHistoryDao().observeOverviewStats(0, Long.MAX_VALUE).first().totalPlays)
        }
    }
    @Test fun decompressionBudgetIsEnforcedOnBulkReads() {
        val stream = LimitedInputStream(ByteArray(20).inputStream(), 10)
        try { stream.read(ByteArray(20)); fail("Limit must apply") } catch (_: IllegalArgumentException) { }
    }
}
