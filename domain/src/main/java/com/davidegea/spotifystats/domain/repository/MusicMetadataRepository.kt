package com.davidegea.spotifystats.domain.repository

import com.davidegea.spotifystats.domain.model.AlbumMetadata
import com.davidegea.spotifystats.domain.model.MetadataRefreshResult
import com.davidegea.spotifystats.domain.model.TrackMetadata
import kotlinx.coroutines.flow.Flow

interface MusicMetadataRepository {
    fun observeTrackMetadata(trackId: Long): Flow<TrackMetadata?>

    fun observeAlbumMetadata(albumId: Long): Flow<AlbumMetadata?>

    suspend fun refresh(
        limit: Int = 50,
        staleAfterMs: Long = 30L * 24L * 60L * 60L * 1000L,
    ): MetadataRefreshResult

    suspend fun clearCache()
}
