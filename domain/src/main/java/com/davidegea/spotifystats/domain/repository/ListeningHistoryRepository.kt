package com.davidegea.spotifystats.domain.repository

import com.davidegea.spotifystats.domain.model.OverviewStats
import kotlinx.coroutines.flow.Flow

interface ListeningHistoryRepository {
    fun observeOverviewStats(): Flow<OverviewStats>
}
