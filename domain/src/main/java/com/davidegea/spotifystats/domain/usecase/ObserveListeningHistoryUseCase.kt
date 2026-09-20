package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ListeningHistoryItem
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveListeningHistoryUseCase(
    private val repository: ListeningHistoryRepository,
    private val rangeResolver: AnalyticsTimeRangeResolver = AnalyticsTimeRangeResolver(),
) {
    operator fun invoke(
        period: AnalyticsPeriod,
        limit: Int = DEFAULT_LIMIT,
    ): Flow<List<ListeningHistoryItem>> {
        val range = rangeResolver.resolve(period)
        return repository.observeListeningHistory(
            fromInclusive = range.fromInclusive,
            toInclusive = range.toInclusive,
            limit = limit,
        )
    }

    private companion object {
        const val DEFAULT_LIMIT = 250
    }
}
