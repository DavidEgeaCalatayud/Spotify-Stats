package com.davidegea.spotifystats.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.usecase.ObserveOverviewStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

sealed interface AppLaunchState {
    data object Loading : AppLaunchState
    data object NeedsImport : AppLaunchState
    data object Ready : AppLaunchState
}

@HiltViewModel
class AppLaunchViewModel @Inject constructor(
    observeOverviewStats: ObserveOverviewStatsUseCase,
) : ViewModel() {

    private val importInProgress = MutableStateFlow(false)
    private val exploreWithoutData = MutableStateFlow(false)

    val uiState = combine(
        observeOverviewStats(),
        importInProgress,
        exploreWithoutData,
    ) { overview, importing, exploring ->
        when {
            importing -> AppLaunchState.NeedsImport
            overview.totalPlays > 0L || exploring -> AppLaunchState.Ready
            else -> AppLaunchState.NeedsImport
        }
    }
        .onStart { emit(AppLaunchState.Loading) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppLaunchState.Loading,
        )

    fun onImportStarted() {
        exploreWithoutData.value = false
        importInProgress.value = true
    }

    fun onImportFinished() {
        importInProgress.value = false
    }

    fun continueWithoutImport() {
        exploreWithoutData.value = true
    }
}
