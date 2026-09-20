package com.davidegea.spotifystats.domain.repository

import com.davidegea.spotifystats.domain.model.AlbumDetail
import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.ArtistDetail
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.HourlyListening
import com.davidegea.spotifystats.domain.model.ListeningHeatmapCell
import com.davidegea.spotifystats.domain.model.ListeningHistoryItem
import com.davidegea.spotifystats.domain.model.OverviewStats
import com.davidegea.spotifystats.domain.model.PlaybackBehaviorStats
import com.davidegea.spotifystats.domain.model.TrackDetail
import com.davidegea.spotifystats.domain.model.TrackRanking
import com.davidegea.spotifystats.domain.model.YearlyListening
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ListeningHistoryRepository {
    fun observeOverviewStats(): Flow<OverviewStats>

    fun observeOverviewStats(
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<OverviewStats> = observeOverviewStats()

    fun observeHourlyListening(
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<List<HourlyListening>> = flowOf(emptyList())

    fun observeListeningHeatmap(
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<List<ListeningHeatmapCell>> = flowOf(emptyList())

    fun observePlaybackBehavior(
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<PlaybackBehaviorStats> = flowOf(PlaybackBehaviorStats())

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

    fun observeTrackDetail(trackId: Long): Flow<TrackDetail?>

    fun observeTrackListeningByYear(trackId: Long): Flow<List<YearlyListening>>

    fun observeArtistDetail(artistId: Long): Flow<ArtistDetail?>

    fun observeArtistTopTracks(
        artistId: Long,
        limit: Int,
    ): Flow<List<TrackRanking>>

    fun observeAlbumDetail(albumId: Long): Flow<AlbumDetail?>

    fun observeAlbumTopTracks(
        albumId: Long,
        limit: Int,
    ): Flow<List<TrackRanking>>

    fun observeListeningHistory(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<ListeningHistoryItem>>
}
