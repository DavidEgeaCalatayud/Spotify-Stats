package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AlbumDetail
import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ArtistDetail
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.ListeningHistoryItem
import com.davidegea.spotifystats.domain.model.OverviewStats
import com.davidegea.spotifystats.domain.model.TrackDetail
import com.davidegea.spotifystats.domain.model.TrackRanking
import com.davidegea.spotifystats.domain.model.YearlyListening
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import java.util.TimeZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveListeningHistoryUseCaseTest {

    @Test
    fun resolvesSelectedPeriodBeforeQueryingRepository() = runBlocking {
        val now = 1_800_000_000_000L
        val repository = HistoryFakeRepository()
        val resolver = AnalyticsTimeRangeResolver(
            nowProvider = { now },
            timeZone = TimeZone.getTimeZone("UTC"),
        )
        val useCase = ObserveListeningHistoryUseCase(repository, resolver)

        useCase(AnalyticsPeriod.LAST_7_DAYS, limit = 25).first()

        assertEquals(now, repository.toInclusive)
        assertEquals(now - 7L * 24 * 60 * 60 * 1_000, repository.fromInclusive)
        assertEquals(25, repository.limit)
    }
}

private class HistoryFakeRepository : ListeningHistoryRepository {
    var fromInclusive: Long? = null
    var toInclusive: Long? = null
    var limit: Int? = null

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

    override fun observeAlbumDetail(albumId: Long): Flow<AlbumDetail?> = flowOf(null)

    override fun observeAlbumTopTracks(
        albumId: Long,
        limit: Int,
    ): Flow<List<TrackRanking>> = flowOf(emptyList())

    override fun observeListeningHistory(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<ListeningHistoryItem>> {
        this.fromInclusive = fromInclusive
        this.toInclusive = toInclusive
        this.limit = limit
        return flowOf(emptyList())
    }
}
