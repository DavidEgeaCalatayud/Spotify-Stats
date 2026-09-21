package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.TimeRange
import com.davidegea.spotifystats.domain.repository.ExplorationRepository

class ExploreListeningUseCase(private val repository: ExplorationRepository) {
    fun analytics(range: TimeRange) = repository.observeAnalytics(range)
    fun daily(range: TimeRange) = repository.observeDaily(range)
    fun search(query: String, range: TimeRange) = repository.search(query, range)
    fun recap(range: TimeRange) = repository.observeRecap(range)
}
