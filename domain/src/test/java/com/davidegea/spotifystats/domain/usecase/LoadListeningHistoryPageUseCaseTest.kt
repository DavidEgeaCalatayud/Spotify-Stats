package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ArtistDetail
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.ListeningHistoryCursor
import com.davidegea.spotifystats.domain.model.ListeningHistoryItem
import com.davidegea.spotifystats.domain.model.ListeningHistoryPage
import com.davidegea.spotifystats.domain.model.OverviewStats
import com.davidegea.spotifystats.domain.model.TrackDetail
import com.davidegea.spotifystats.domain.model.TrackRanking
import com.davidegea.spotifystats.domain.model.YearlyListening
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LoadListeningHistoryPageUseCaseTest {

    @Test
    fun forwardsCursorPageSizeAndResolvedRange() = runBlocking {
        val repository = PagingFakeRepository()
        val cursor = ListeningHistoryCursor(
            playedAtEpochMs = 1234,
            eventId = 99,
        )

        LoadListeningHistoryPageUseCase(repository)(
            period = AnalyticsPeriod.ALL_TIME,
            cursor = cursor,
            pageSize = 73,
        )

        assertEquals(cursor, repository.cursor)
        assertEquals(73, repository.pageSize)
        assertEquals(Long.MIN_VALUE, repository.fromInclusive)
        assertEquals(Long.MAX_VALUE, repository.toInclusive)
    }
}

private class PagingFakeRepository : ListeningHistoryRepository {
    var cursor: ListeningHistoryCursor? = null
    var pageSize: Int = 0
    var fromInclusive: Long = 0
    var toInclusive: Long = 0

    override suspend fun loadListeningHistoryPage(
        fromInclusive: Long,
        toInclusive: Long,
        cursor: ListeningHistoryCursor?,
        pageSize: Int,
    ): ListeningHistoryPage {
        this.fromInclusive = fromInclusive
        this.toInclusive = toInclusive
        this.cursor = cursor
        this.pageSize = pageSize
        return ListeningHistoryPage(emptyList(), null, false)
    }

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

    override fun observeAlbumDetail(albumId: Long) = flowOf(null)

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
