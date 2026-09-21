package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ListeningHistoryCursor
import com.davidegea.spotifystats.domain.model.ListeningHistoryPage
import com.davidegea.spotifystats.domain.model.TimeRange
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository

class LoadListeningHistoryPageUseCase(
    private val repository: ListeningHistoryRepository,
    private val rangeResolver: AnalyticsTimeRangeResolver = AnalyticsTimeRangeResolver(),
) {
    suspend operator fun invoke(
        period: AnalyticsPeriod,
        cursor: ListeningHistoryCursor? = null,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        customRange: TimeRange? = null,
    ): ListeningHistoryPage {
        val range = customRange ?: rangeResolver.resolve(period)
        return repository.loadListeningHistoryPage(
            fromInclusive = range.fromInclusive,
            toInclusive = range.toInclusive,
            cursor = cursor,
            pageSize = pageSize,
        )
    }

    private companion object {
        const val DEFAULT_PAGE_SIZE = 100
    }
}
