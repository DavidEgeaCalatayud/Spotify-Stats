package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.LibraryRankings
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveLibraryRankingsUseCase(
    private val repository: ListeningHistoryRepository,
    private val rangeResolver: AnalyticsTimeRangeResolver = AnalyticsTimeRangeResolver(),
) {
    operator fun invoke(
        period: AnalyticsPeriod,
        limit: Int = DEFAULT_LIMIT,
    ): Flow<LibraryRankings> {
        val range = rangeResolver.resolve(period)

        return combine(
            repository.observeTopTracks(range.fromInclusive, range.toInclusive, limit),
            repository.observeTopArtists(range.fromInclusive, range.toInclusive, limit),
            repository.observeTopAlbums(range.fromInclusive, range.toInclusive, limit),
        ) { tracks, artists, albums ->
            LibraryRankings(
                tracks = tracks,
                artists = artists,
                albums = albums,
            )
        }
    }

    private companion object {
        const val DEFAULT_LIMIT = 100
    }
}
