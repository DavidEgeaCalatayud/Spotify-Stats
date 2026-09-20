package com.davidegea.spotifystats.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ListeningHeatmapCell
import com.davidegea.spotifystats.domain.usecase.ObserveListeningHabitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.davidegea.spotifystats.domain.model.TimeRange
import com.davidegea.spotifystats.domain.model.AdvancedAnalytics
import com.davidegea.spotifystats.domain.usecase.ExploreListeningUseCase
import com.davidegea.spotifystats.domain.usecase.AnalyticsTimeRangeResolver
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class InsightsUiState(
    val period: AnalyticsPeriod = AnalyticsPeriod.LAST_30_DAYS,
    val customRange: TimeRange? = null,
    val advanced: AdvancedAnalytics = AdvancedAnalytics(),
    val loading: Boolean = true,
    val error: String? = null,
    val favouriteHour: Int? = null,
    val favouriteWeekday: Int? = null,
    val morningShare: Double? = null,
    val nightShare: Double? = null,
    val skipRate: Double? = null,
    val shuffleRate: Double? = null,
    val offlineRate: Double? = null,
    val heatmap: List<ListeningHeatmapCell> = emptyList(),
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    explore: ExploreListeningUseCase,
    observeListeningHabits: ObserveListeningHabitsUseCase,
) : ViewModel() {

    private val period = MutableStateFlow(AnalyticsPeriod.LAST_30_DAYS)

    private val custom = MutableStateFlow<TimeRange?>(null)
    val uiState = combine(period, custom) { selected, range -> selected to range }
        .flatMapLatest { (selected, range) ->
            combine(observeListeningHabits(selected, range), explore.analytics(range ?: AnalyticsTimeRangeResolver().resolve(selected))) { habits, advanced ->
                InsightsUiState(
                    period = selected,
                    customRange = range, advanced = advanced, loading = false,
                    favouriteHour = habits.favouriteHour,
                    favouriteWeekday = habits.favouriteWeekday,
                    morningShare = habits.morningShare,
                    nightShare = habits.nightShare,
                    skipRate = habits.skipRate,
                    shuffleRate = habits.shuffleRate,
                    offlineRate = habits.offlineRate,
                    heatmap = habits.heatmap,
                )
            }
        }
        .catch { emit(InsightsUiState(loading = false, error = "Unable to calculate insights.")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InsightsUiState(),
        )

    fun selectCustom(range: TimeRange) { custom.value = range }

    fun selectPeriod(period: AnalyticsPeriod) {
        custom.value = null
        this.period.value = period
    }
}
