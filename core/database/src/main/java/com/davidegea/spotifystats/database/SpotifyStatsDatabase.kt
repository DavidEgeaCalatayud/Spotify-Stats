package com.davidegea.spotifystats.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.davidegea.spotifystats.database.dao.ListeningHistoryDao
import com.davidegea.spotifystats.database.entity.AlbumEntity
import com.davidegea.spotifystats.database.entity.ArtistEntity
import com.davidegea.spotifystats.database.entity.PlayEventEntity
import com.davidegea.spotifystats.database.entity.TrackArtistCrossRef
import com.davidegea.spotifystats.database.entity.TrackEntity

@Database(
    entities = [
        AlbumEntity::class,
        ArtistEntity::class,
        TrackEntity::class,
        TrackArtistCrossRef::class,
        PlayEventEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
abstract class SpotifyStatsDatabase : RoomDatabase() {
    abstract fun listeningHistoryDao(): ListeningHistoryDao

    companion object {
        const val DATABASE_NAME = "spotify_stats.db"
    }
}
