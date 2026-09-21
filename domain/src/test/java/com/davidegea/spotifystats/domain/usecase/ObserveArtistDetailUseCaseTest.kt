package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AlbumDetail
import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.ArtistDetail
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.ArtistYearRank
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

class ObserveArtistDetailUseCaseTest {

    @Test
    fun combinesBaseDetailWithTopTracks() = runBlocking {
        val topTracks = listOf(
            TrackRanking(
                id = 10,
                name = "After Hours",
                artistName = "The Weeknd",
                plays = 621,
                listeningMs = 10_000,
            ),
        )
        val rankByYear = listOf(
            ArtistYearRank(
                year = 2025,
                rank = 4,
                plays = 400,
                listeningMs = 40_000,
            ),
            ArtistYearRank(
                year = 2026,
                rank = 1,
                plays = 900,
                listeningMs = 90_000,
            ),
        )
        val repository = ArtistFakeRepository(
            artistDetail = ArtistDetail(
                id = 4,
                name = "The Weeknd",
                totalPlays = 8421,
                totalListeningMs = 100_000,
                uniqueTracks = 148,
                firstPlayedAtEpochMs = 1,
                lastPlayedAtEpochMs = 2,
            ),
            topTracks = topTracks,
            rankByYear = rankByYear,
        )

        val result = ObserveArtistDetailUseCase(repository)(4).first()

        assertEquals(topTracks, result?.topTracks)
        assertEquals(rankByYear, result?.rankByYear)
        assertEquals(148L, result?.uniqueTracks)
    }
}

private class ArtistFakeRepository(
    private val artistDetail: ArtistDetail? = null,
    private val topTracks: List<TrackRanking> = emptyList(),
    private val rankByYear: List<ArtistYearRank> = emptyList(),
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

    override fun observeArtistDetail(artistId: Long): Flow<ArtistDetail?> = flowOf(artistDetail)

    override fun observeArtistTopTracks(
        artistId: Long,
        limit: Int,
    ): Flow<List<TrackRanking>> = flowOf(topTracks.take(limit))

    override fun observeArtistYearRanks(
        artistId: Long,
    ): Flow<List<ArtistYearRank>> = flowOf(rankByYear)

    override fun observeAlbumDetail(albumId: Long): Flow<AlbumDetail?> = flowOf(null)

    override fun observeAlbumTopTracks(
        albumId: Long,
        limit: Int,
    ): Flow<List<TrackRanking>> = flowOf(emptyList())

    override fun observeListeningHistory(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<ListeningHistoryItem>> = flowOf(emptyList())
}
