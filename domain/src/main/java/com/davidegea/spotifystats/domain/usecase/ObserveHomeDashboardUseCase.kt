package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.HomeDashboard
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveHomeDashboardUseCase(
    private val repository: ListeningHistoryRepository,
    private val rangeResolver: AnalyticsTimeRangeResolver = AnalyticsTimeRangeResolver(),
) {

    operator fun invoke(period: AnalyticsPeriod): Flow<HomeDashboard> {
        val range = rangeResolver.resolve(period)

        val topEntities = combine(
            repository.observeTopTracks(
                fromInclusive = range.fromInclusive,
                toInclusive = range.toInclusive,
                limit = 1,
            ),
            repository.observeTopArtists(
                fromInclusive = range.fromInclusive,
                toInclusive = range.toInclusive,
                limit = 1,
            ),
            repository.observeTopAlbums(
                fromInclusive = range.fromInclusive,
                toInclusive = range.toInclusive,
                limit = 1,
            ),
        ) { tracks, artists, albums ->
            Triple(
                tracks.firstOrNull(),
                artists.firstOrNull(),
                albums.firstOrNull(),
            )
        }

        return combine(
            repository.observeOverviewStats(
                fromInclusive = range.fromInclusive,
                toInclusive = range.toInclusive,
            ),
            topEntities,
            repository.observeListeningHistory(
                fromInclusive = Long.MIN_VALUE,
                toInclusive = Long.MAX_VALUE,
                limit = RECENT_ACTIVITY_LIMIT,
            ),
        ) { overview, top, recent ->
            HomeDashboard(
                overview = overview,
                topTrack = top.first,
                topArtist = top.second,
                topAlbum = top.third,
                recentActivity = recent,
            )
        }
    }

    private companion object {
        const val RECENT_ACTIVITY_LIMIT = 5
    }
}
