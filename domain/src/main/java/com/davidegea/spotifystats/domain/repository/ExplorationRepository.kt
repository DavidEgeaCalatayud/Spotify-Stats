package com.davidegea.spotifystats.domain.repository

import com.davidegea.spotifystats.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ExplorationRepository {
    fun observeAnalytics(range: TimeRange): Flow<AdvancedAnalytics>
    fun observeDaily(range: TimeRange): Flow<List<DailyListening>>
    fun search(query: String, range: TimeRange): Flow<List<SearchResult>>
    fun observeRecap(range: TimeRange): Flow<Recap>
}
