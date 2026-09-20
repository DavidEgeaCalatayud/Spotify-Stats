package com.davidegea.spotifystats.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.usecase.ObserveOverviewStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeOverviewStats: ObserveOverviewStatsUseCase,
) : ViewModel() {

    val uiState = observeOverviewStats()
        .map { stats ->
            HomeUiState(
                totalPlays = stats.totalPlays,
                totalListeningMs = stats.totalListeningMs,
                uniqueTracks = stats.uniqueTracks,
                uniqueArtists = stats.uniqueArtists,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )
}
