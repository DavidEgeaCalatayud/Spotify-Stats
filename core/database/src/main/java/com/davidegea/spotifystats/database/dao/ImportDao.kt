package com.davidegea.spotifystats.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.davidegea.spotifystats.database.entity.AlbumEntity
import com.davidegea.spotifystats.database.entity.ArtistEntity
import com.davidegea.spotifystats.database.entity.PlayEventEntity
import com.davidegea.spotifystats.database.entity.TrackArtistCrossRef
import com.davidegea.spotifystats.database.entity.TrackEntity

@Dao
interface ImportDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArtist(entity: ArtistEntity): Long

    @Query("SELECT id FROM artists WHERE identity_key = :identityKey LIMIT 1")
    suspend fun findArtistId(identityKey: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlbum(entity: AlbumEntity): Long

    @Query("SELECT id FROM albums WHERE identity_key = :identityKey LIMIT 1")
    suspend fun findAlbumId(identityKey: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(entity: TrackEntity): Long

    @Query("SELECT id FROM tracks WHERE identity_key = :identityKey LIMIT 1")
    suspend fun findTrackId(identityKey: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackArtist(crossRef: TrackArtistCrossRef): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayEvent(entity: PlayEventEntity): Long
}
