package com.davidegea.spotifystats.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.usecase.ObserveHomeDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeHomeDashboard: ObserveHomeDashboardUseCase,
) : ViewModel() {

    private val period = MutableStateFlow(AnalyticsPeriod.LAST_30_DAYS)

    val uiState = period
        .flatMapLatest { selected ->
            observeHomeDashboard(selected).map { dashboard ->
                HomeUiState(
                    period = selected,
                    totalPlays = dashboard.overview.totalPlays,
                    totalListeningMs = dashboard.overview.totalListeningMs,
                    uniqueTracks = dashboard.overview.uniqueTracks,
                    uniqueArtists = dashboard.overview.uniqueArtists,
                    topTrack = dashboard.topTrack,
                    topArtist = dashboard.topArtist,
                    topAlbum = dashboard.topAlbum,
                    recentActivity = dashboard.recentActivity,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    fun selectPeriod(period: AnalyticsPeriod) {
        this.period.value = period
    }
}
