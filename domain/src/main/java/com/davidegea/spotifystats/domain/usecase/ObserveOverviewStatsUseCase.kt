package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.OverviewStats
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveOverviewStatsUseCase @Inject constructor(
    private val repository: ListeningHistoryRepository,
) {
    operator fun invoke(): Flow<OverviewStats> = repository.observeOverviewStats()
}
