package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.OverviewStats
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveOverviewStatsUseCase(
    private val repository: ListeningHistoryRepository,
) {
    operator fun invoke(): Flow<OverviewStats> = repository.observeOverviewStats()
}
