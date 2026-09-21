package com.davidegea.spotifystats.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.davidegea.spotifystats.database.entity.AlbumMetadataEntity
import com.davidegea.spotifystats.database.entity.TrackMetadataEntity
import kotlinx.coroutines.flow.Flow

data class MetadataCandidateRow(
    val trackId: Long,
    val spotifyUri: String,
    val trackName: String,
    val artistName: String?,
    val albumId: Long?,
    val albumName: String?,
)

@Dao
interface MetadataDao {

    @Query(
        """
        SELECT
            t.id AS trackId,
            t.spotify_uri AS spotifyUri,
            t.name AS trackName,
            (
                SELECT a.name
                FROM track_artists ta
                INNER JOIN artists a ON a.id = ta.artist_id
                WHERE ta.track_id = t.id
                ORDER BY ta.position ASC, a.id ASC
                LIMIT 1
            ) AS artistName,
            al.id AS albumId,
            al.name AS albumName
        FROM tracks t
        LEFT JOIN albums al ON al.id = t.album_id
        LEFT JOIN track_metadata tm ON tm.track_id = t.id
        WHERE t.spotify_uri IS NOT NULL
          AND (
              tm.track_id IS NULL
              OR tm.refreshed_at < :staleBeforeEpochMs
          )
        ORDER BY
            CASE WHEN tm.track_id IS NULL THEN 0 ELSE 1 END,
            t.id ASC
        LIMIT :limit
        """,
    )
    suspend fun loadCandidates(
        staleBeforeEpochMs: Long,
        limit: Int,
    ): List<MetadataCandidateRow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrackMetadata(entity: TrackMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlbumMetadata(entity: AlbumMetadataEntity)

    @Query("SELECT * FROM track_metadata WHERE track_id = :trackId LIMIT 1")
    fun observeTrackMetadata(trackId: Long): Flow<TrackMetadataEntity?>

    @Query("SELECT * FROM album_metadata WHERE album_id = :albumId LIMIT 1")
    fun observeAlbumMetadata(albumId: Long): Flow<AlbumMetadataEntity?>

    @Query("DELETE FROM track_metadata")
    suspend fun clearTrackMetadata()

    @Query("DELETE FROM album_metadata")
    suspend fun clearAlbumMetadata()
}
