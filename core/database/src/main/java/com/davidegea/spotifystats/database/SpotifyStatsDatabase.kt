package com.davidegea.spotifystats.database

import com.davidegea.spotifystats.database.dao.ExplorationDao
import com.davidegea.spotifystats.database.entity.TrackSearchEntity
import com.davidegea.spotifystats.database.entity.ArtistSearchEntity
import com.davidegea.spotifystats.database.entity.AlbumSearchEntity
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.davidegea.spotifystats.database.dao.ImportDao
import com.davidegea.spotifystats.database.dao.ImportJobDao
import com.davidegea.spotifystats.database.dao.ListeningHistoryDao
import com.davidegea.spotifystats.database.dao.MetadataDao
import com.davidegea.spotifystats.database.entity.AlbumEntity
import com.davidegea.spotifystats.database.entity.ArtistEntity
import com.davidegea.spotifystats.database.entity.PlayEventEntity
import com.davidegea.spotifystats.database.entity.TrackArtistCrossRef
import com.davidegea.spotifystats.database.entity.TrackEntity
import com.davidegea.spotifystats.database.entity.ImportRunEntity
import com.davidegea.spotifystats.database.entity.ImportDocumentDiagnosticEntity
import com.davidegea.spotifystats.database.entity.TrackMetadataEntity
import com.davidegea.spotifystats.database.entity.AlbumMetadataEntity

@Database(
    entities = [
        AlbumEntity::class,
        ArtistEntity::class,
        TrackEntity::class,
        TrackArtistCrossRef::class,
        PlayEventEntity::class,
        TrackSearchEntity::class,
        ArtistSearchEntity::class,
        AlbumSearchEntity::class,
        ImportRunEntity::class,
        ImportDocumentDiagnosticEntity::class,
        TrackMetadataEntity::class,
        AlbumMetadataEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
abstract class SpotifyStatsDatabase : RoomDatabase() {
    abstract fun listeningHistoryDao(): ListeningHistoryDao
    abstract fun explorationDao(): ExplorationDao
    abstract fun importDao(): ImportDao
    abstract fun importJobDao(): ImportJobDao
    abstract fun metadataDao(): MetadataDao

    companion object {
        const val DATABASE_NAME = "spotify_stats.db"
    }
}
