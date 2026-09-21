package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AlbumDetail
import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.ArtistDetail
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.ListeningHistoryItem
import com.davidegea.spotifystats.domain.model.OverviewStats
import com.davidegea.spotifystats.domain.model.TrackDetail
import com.davidegea.spotifystats.domain.model.TrackRanking
import com.davidegea.spotifystats.domain.model.YearlyListening
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveAlbumDetailUseCaseTest {

    @Test
    fun combinesAlbumMetricsWithTopTracks() = runBlocking {
        val tracks = listOf(
            TrackRanking(
                id = 7,
                name = "After Hours",
                artistName = "The Weeknd",
                plays = 621,
                listeningMs = 50_000,
            ),
        )
        val yearly = listOf(
            YearlyListening(2025, 700, 40_000),
            YearlyListening(2026, 1031, 50_000),
        )
        val repository = AlbumFakeRepository(
            detail = AlbumDetail(
                id = 2,
                name = "After Hours",
                artistName = "The Weeknd",
                totalPlays = 1731,
                totalListeningMs = 90_000,
                uniqueTracks = 14,
                firstPlayedAtEpochMs = 1,
                lastPlayedAtEpochMs = 2,
            ),
            tracks = tracks,
            yearly = yearly,
        )

        val result = ObserveAlbumDetailUseCase(repository)(2).first()

        assertEquals(14L, result?.uniqueTracks)
        assertEquals(tracks, result?.topTracks)
        assertEquals(yearly, result?.playsByYear)
    }
}

private class AlbumFakeRepository(
    private val detail: AlbumDetail?,
    private val tracks: List<TrackRanking>,
    private val yearly: List<YearlyListening>,
) : ListeningHistoryRepository {
    override fun observeOverviewStats(): Flow<OverviewStats> =
        flowOf(OverviewStats(0, 0, 0, 0))

    override fun observeTopTracks(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<TrackRanking>> = flowOf(emptyList())

    override fun observeTopArtists(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<ArtistRanking>> = flowOf(emptyList())

    override fun observeTopAlbums(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<AlbumRanking>> = flowOf(emptyList())

    override fun observeTrackDetail(trackId: Long): Flow<TrackDetail?> = flowOf(null)

    override fun observeTrackListeningByYear(trackId: Long): Flow<List<YearlyListening>> =
        flowOf(emptyList())

    override fun observeArtistDetail(artistId: Long): Flow<ArtistDetail?> = flowOf(null)

    override fun observeArtistTopTracks(
        artistId: Long,
        limit: Int,
    ): Flow<List<TrackRanking>> = flowOf(emptyList())

    override fun observeAlbumDetail(albumId: Long): Flow<AlbumDetail?> = flowOf(detail)

    override fun observeAlbumTopTracks(
        albumId: Long,
        limit: Int,
    ): Flow<List<TrackRanking>> = flowOf(tracks.take(limit))

    override fun observeAlbumListeningByYear(
        albumId: Long,
    ): Flow<List<YearlyListening>> = flowOf(yearly)

    override fun observeListeningHistory(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<ListeningHistoryItem>> = flowOf(emptyList())
}
