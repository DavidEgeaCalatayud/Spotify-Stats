package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ListeningHabits
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveListeningHabitsUseCase(
    private val repository: ListeningHistoryRepository,
    private val rangeResolver: AnalyticsTimeRangeResolver = AnalyticsTimeRangeResolver(),
    private val calculator: ListeningHabitsCalculator = ListeningHabitsCalculator(),
) {

    operator fun invoke(period: AnalyticsPeriod): Flow<ListeningHabits> {
        val range = rangeResolver.resolve(period)

        return combine(
            repository.observeHourlyListening(
                fromInclusive = range.fromInclusive,
                toInclusive = range.toInclusive,
            ),
            repository.observeListeningHeatmap(
                fromInclusive = range.fromInclusive,
                toInclusive = range.toInclusive,
            ),
            repository.observePlaybackBehavior(
                fromInclusive = range.fromInclusive,
                toInclusive = range.toInclusive,
            ),
        ) { hourly, heatmap, behavior ->
            calculator.calculate(
                hourly = hourly,
                heatmap = heatmap,
                behavior = behavior,
            )
        }
    }
}
