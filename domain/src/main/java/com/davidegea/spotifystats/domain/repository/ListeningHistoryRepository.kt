package com.davidegea.spotifystats.domain.repository

import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.OverviewStats
import com.davidegea.spotifystats.domain.model.TrackRanking
import kotlinx.coroutines.flow.Flow

interface ListeningHistoryRepository {
    fun observeOverviewStats(): Flow<OverviewStats>

    fun observeTopTracks(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<TrackRanking>>

    fun observeTopArtists(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<ArtistRanking>>

    fun observeTopAlbums(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<AlbumRanking>>
}
