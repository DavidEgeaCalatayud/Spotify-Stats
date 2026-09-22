package com.davidegea.spotifystats.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.usecase.ObserveHomeDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.flowOf
import com.davidegea.spotifystats.domain.analytics.DateRanges
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.catch
import com.davidegea.spotifystats.domain.model.TimeRange
import com.davidegea.spotifystats.domain.usecase.ExploreListeningUseCase
import com.davidegea.spotifystats.domain.usecase.AnalyticsTimeRangeResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeHomeDashboard: ObserveHomeDashboardUseCase,
    explore: ExploreListeningUseCase,
) : ViewModel() {

    private val period = MutableStateFlow(AnalyticsPeriod.LAST_30_DAYS)

    private val custom = MutableStateFlow<TimeRange?>(null)
    val uiState = combine(period, custom) { selected, range -> selected to range }
        .flatMapLatest { (selected, range) ->
            val resolved = range ?: AnalyticsTimeRangeResolver().resolve(selected)
            val previousRange = DateRanges.previous(resolved)
            val previousFlow = previousRange?.let { explore.daily(it) } ?: flowOf(null)
            combine(observeHomeDashboard(selected, range), explore.daily(resolved), previousFlow) { dashboard, daily, previous ->
                HomeUiState(
                    period = selected,
                    customRange = range,
                    daily = daily,
                    range = resolved,
                    previousRange = previousRange,
                    previousDaily = previous,
                    previousListeningMs = previous?.sumOf { it.listeningMs },
                    loading = false,
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
                .catch { emit(HomeUiState(period = selected, customRange = range, loading = false, error = "Unable to read listening history.")) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    fun selectCustom(range: TimeRange) { custom.value = range }

    fun selectPeriod(period: AnalyticsPeriod) {
        custom.value = null
        this.period.value = period
    }
}
