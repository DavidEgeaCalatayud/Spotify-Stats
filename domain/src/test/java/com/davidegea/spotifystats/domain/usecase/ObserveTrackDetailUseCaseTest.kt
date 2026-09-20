package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.ArtistDetail
import com.davidegea.spotifystats.domain.model.ArtistRanking
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

class ObserveTrackDetailUseCaseTest {

    @Test
    fun combinesBaseDetailWithYearlyHistory() = runBlocking {
        val repository = FakeListeningHistoryRepository(
            trackDetail = TrackDetail(
                id = 7,
                name = "After Hours",
                artistName = "The Weeknd",
                totalPlays = 3,
                totalListeningMs = 600_000,
                firstPlayedAtEpochMs = 1,
                lastPlayedAtEpochMs = 2,
                skippedPlays = 1,
                skipKnownPlays = 3,
            ),
            yearly = listOf(
                YearlyListening(year = 2025, plays = 1, listeningMs = 200_000),
                YearlyListening(year = 2026, plays = 2, listeningMs = 400_000),
            ),
        )

        val result = ObserveTrackDetailUseCase(repository)(7).first()

        assertEquals(listOf(2025, 2026), result?.playsByYear?.map { it.year })
        assertEquals(3L, result?.totalPlays)
    }
}

private class FakeListeningHistoryRepository(
    private val trackDetail: TrackDetail? = null,
    private val yearly: List<YearlyListening> = emptyList(),
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

    override fun observeTrackDetail(trackId: Long): Flow<TrackDetail?> = flowOf(trackDetail)

    override fun observeTrackListeningByYear(trackId: Long): Flow<List<YearlyListening>> =
        flowOf(yearly)

    override fun observeArtistDetail(artistId: Long): Flow<ArtistDetail?> = flowOf(null)

    override fun observeArtistTopTracks(
        artistId: Long,
        limit: Int,
    ): Flow<List<TrackRanking>> = flowOf(emptyList())
}
