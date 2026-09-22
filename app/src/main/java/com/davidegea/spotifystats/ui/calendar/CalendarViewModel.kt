package com.davidegea.spotifystats.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.analytics.DateRanges
import com.davidegea.spotifystats.domain.model.*
import com.davidegea.spotifystats.domain.usecase.ExploreListeningUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.Calendar
import kotlinx.coroutines.flow.*

data class CalendarState(val year: Int, val days: List<DailyListening> = emptyList(), val error: String? = null, val loading: Boolean = false)

data class CalendarDayState(val date: String? = null, val recap: Recap? = null, val error: Boolean = false)

@HiltViewModel
class CalendarViewModel @Inject constructor(explore: ExploreListeningUseCase) : ViewModel() {
    private val year = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val day = MutableStateFlow<String?>(null)
    val selectedDay = day.asStateFlow()
    val state = year.flatMapLatest { y ->
        explore.daily(DateRanges.dates("$y-01-01", "$y-12-31")).map { CalendarState(y, it) }.onStart { emit(CalendarState(y, loading = true)) }
            .catch { emit(CalendarState(y, error = "Unable to load calendar.")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarState(year.value, loading = true))
    val detail = day.flatMapLatest { selected ->
        if (selected == null) flowOf(CalendarDayState()) else explore.recap(DateRanges.dates(selected, selected))
            .map { CalendarDayState(selected, it) }
            .onStart { emit(CalendarDayState(selected)) }
            .catch { emit(CalendarDayState(selected, error = true)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarDayState())
    fun changeYear(change: Int) { year.value = (year.value + change).coerceIn(1900, 2200); day.value = null }
    fun selectDay(date: String?) { day.value = date }
}
