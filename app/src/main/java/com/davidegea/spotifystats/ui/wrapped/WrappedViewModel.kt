package com.davidegea.spotifystats.ui.wrapped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.*
import com.davidegea.spotifystats.domain.usecase.AnalyticsTimeRangeResolver
import com.davidegea.spotifystats.domain.usecase.ExploreListeningUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

data class WrappedState(val period: AnalyticsPeriod = AnalyticsPeriod.THIS_YEAR, val customRange: TimeRange? = null, val recap: Recap? = null, val error: String? = null)

@HiltViewModel
class WrappedViewModel @Inject constructor(explore: ExploreListeningUseCase) : ViewModel() {
    private val selection = MutableStateFlow(AnalyticsPeriod.THIS_YEAR to null as TimeRange?)
    val state = selection.flatMapLatest { (period, range) ->
        explore.recap(range ?: AnalyticsTimeRangeResolver().resolve(period)).map { WrappedState(period, range, it) }
            .onStart { emit(WrappedState(period, range)) }
            .catch { emit(WrappedState(period, range, error = "Unable to generate recap.")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WrappedState())
    fun selectPeriod(period: AnalyticsPeriod) { selection.value = period to null }
    fun selectCustom(range: TimeRange) { selection.value = selection.value.first to range }
}
