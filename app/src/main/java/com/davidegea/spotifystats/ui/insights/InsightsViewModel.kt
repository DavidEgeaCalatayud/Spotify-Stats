package com.davidegea.spotifystats.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ListeningHeatmapCell
import com.davidegea.spotifystats.domain.usecase.ObserveListeningHabitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class InsightsUiState(
    val period: AnalyticsPeriod = AnalyticsPeriod.LAST_30_DAYS,
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
    observeListeningHabits: ObserveListeningHabitsUseCase,
) : ViewModel() {

    private val period = MutableStateFlow(AnalyticsPeriod.LAST_30_DAYS)

    val uiState = period
        .flatMapLatest { selected ->
            observeListeningHabits(selected).map { habits ->
                InsightsUiState(
                    period = selected,
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
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InsightsUiState(),
        )

    fun selectPeriod(period: AnalyticsPeriod) {
        this.period.value = period
    }
}
