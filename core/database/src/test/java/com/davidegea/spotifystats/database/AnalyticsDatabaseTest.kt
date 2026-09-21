package com.davidegea.spotifystats.database

import androidx.room.Room
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AnalyticsDatabaseTest {
    private lateinit var db: SpotifyStatsDatabase
    @Before fun setup() { db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), SpotifyStatsDatabase::class.java).allowMainThreadQueries().build() }
    @After fun close() { db.close() }

    private suspend fun seed() {
        db.importDao().insertArtist(ArtistEntity(1, null, "a", "Beyoncé", "beyoncé"))
        db.importDao().insertAlbum(AlbumEntity(1, null, "al", "Lemonade", null))
        db.importDao().insertTrack(TrackEntity(1, "spotify:track:a", "t", "Formation", 1, 100_000))
        db.importDao().insertTrackArtist(TrackArtistCrossRef(1, 1, 0))
    }
    private fun play(hash: String, time: Long, duration: Long) = PlayEventEntity(0, hash, 1, time, duration, null, null, null, null, null, null, null, null, PlaySource.SPOTIFY_EXPORT)

    @Test fun realSqlSeparatesEventsMeaningfulCompletionAndUnknownFlags() = runBlocking {
        seed()
        listOf(0L, 29_999L, 30_000L, 90_000L, 150_000L).forEachIndexed { i, ms -> db.importDao().insertPlayEvent(play("h$i", 200_000L + i, ms)) }
        val q = db.explorationDao().quality(0, Long.MAX_VALUE)
        assertEquals(5L, q.events); assertEquals(3L, q.meaningful); assertEquals(2L, q.completed)
        assertEquals(5L, q.durationKnown)
        assertEquals(0L, db.listeningHistoryDao().observePlaybackBehavior(0, Long.MAX_VALUE).first().skipKnownEvents)
        assertEquals(-1L, db.importDao().insertPlayEvent(play("h0", 200_000, 0)))
    }
    @Test fun ftsFindsUnicodeAndArtistRelatedTracksAndAlbums() = runBlocking {
        seed(); db.importDao().insertPlayEvent(play("h", 200_000, 100_000))
        val found = db.explorationDao().search("\"beyonce\"*", 0, Long.MAX_VALUE).first()
        assertEquals(setOf("track", "artist", "album"), found.map { it.kind }.toSet())
        db.openHelper.writableDatabase.execSQL("UPDATE artists SET name='Renamed' WHERE id=1")
        assertTrue(db.explorationDao().search("\"beyonce\"*", 0, Long.MAX_VALUE).first().isEmpty())
        assertEquals(3, db.explorationDao().search("\"renamed\"*", 0, Long.MAX_VALUE).first().size)
    }
    @Test fun periodBoundariesAndDiscoveryUseEarlierHistory() = runBlocking {
        seed(); db.importDao().insertPlayEvent(play("old", 1_000, 50_000))
        db.importDao().insertPlayEvent(play("new", 100L * 86_400_000, 50_000))
        assertEquals(0L, db.explorationDao().discoveryCount(2_000, Long.MAX_VALUE))
        assertEquals(99L, db.explorationDao().rediscoveries(2_000, Long.MAX_VALUE).single().gapDays)
        assertEquals(1L, db.explorationDao().quality(1_000, 1_000).events)
        assertEquals(0L, db.explorationDao().quality(1_001, 1_999).events)
    }

    @Test fun historyKeysetDoesNotSkipEventsWithIdenticalTimestamps() = runBlocking {
        seed()
        val newest = db.importDao().insertPlayEvent(play("page-newest", 3_000, 10_000))
        val sameTimeFirst = db.importDao().insertPlayEvent(play("page-same-1", 2_000, 10_000))
        val sameTimeSecond = db.importDao().insertPlayEvent(play("page-same-2", 2_000, 10_000))
        val oldest = db.importDao().insertPlayEvent(play("page-oldest", 1_000, 10_000))

        val first = db.listeningHistoryDao().loadListeningHistoryPage(
            fromInclusive = 0,
            toInclusive = Long.MAX_VALUE,
            cursorPlayedAt = null,
            cursorEventId = null,
            limit = 2,
        )
        assertEquals(listOf(newest, sameTimeSecond), first.map { it.id })

        val cursor = first.last()
        val second = db.listeningHistoryDao().loadListeningHistoryPage(
            fromInclusive = 0,
            toInclusive = Long.MAX_VALUE,
            cursorPlayedAt = cursor.playedAtEpochMs,
            cursorEventId = cursor.id,
            limit = 2,
        )
        assertEquals(listOf(sameTimeFirst, oldest), second.map { it.id })
        assertTrue(first.map { it.id }.intersect(second.map { it.id }.toSet()).isEmpty())
    }

    @Test fun v1MigrationKeepsRowsAndBuildsSearchIncludingLaterWrites() = runBlocking {
        // Create a v1-shaped file with the exact original tables and indexes; remove only v2 FTS additions.
        val context = RuntimeEnvironment.getApplication()
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val original = Room.databaseBuilder(context, SpotifyStatsDatabase::class.java, name).allowMainThreadQueries().build()
        original.openHelper.writableDatabase.execSQL("INSERT INTO artists(id,spotify_id,identity_key,name,normalized_name) VALUES(1,NULL,'a','Original','original')")
        original.close()
        android.database.sqlite.SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, 0).use { old ->
            val triggers = mutableListOf<String>()
            old.rawQuery("SELECT name FROM sqlite_master WHERE type='trigger' AND name LIKE 'room_fts_%'", null).use { while(it.moveToNext()) triggers += it.getString(0) }
            triggers.forEach { old.execSQL("DROP TRIGGER `$it`") }
            listOf("tracks_fts", "artists_fts", "albums_fts").forEach { old.execSQL("DROP TABLE `$it`") }
            old.version = 1
        }
        val migrated = Room.databaseBuilder(context, SpotifyStatsDatabase::class.java, name).allowMainThreadQueries().addMigrations(MIGRATION_1_2).build()
        try {
            migrated.openHelper.writableDatabase.query("SELECT name FROM artists WHERE id=1").use { assertTrue(it.moveToFirst()); assertEquals("Original", it.getString(0)) }
            migrated.openHelper.writableDatabase.query("SELECT rowid FROM artists_fts WHERE artists_fts MATCH 'Original'").use { assertTrue(it.moveToFirst()) }
            migrated.openHelper.writableDatabase.execSQL("INSERT INTO artists(id,spotify_id,identity_key,name,normalized_name) VALUES(2,NULL,'b','Fresh','fresh')")
            migrated.openHelper.writableDatabase.query("SELECT rowid FROM artists_fts WHERE artists_fts MATCH 'Fresh'").use { assertTrue(it.moveToFirst()) }
        } finally { migrated.close(); context.deleteDatabase(name) }
    }
}
