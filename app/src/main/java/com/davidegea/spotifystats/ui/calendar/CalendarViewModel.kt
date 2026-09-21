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

data class CalendarState(val year: Int, val days: List<DailyListening> = emptyList(), val error: String? = null)

@HiltViewModel
class CalendarViewModel @Inject constructor(explore: ExploreListeningUseCase) : ViewModel() {
    private val year = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val day = MutableStateFlow<String?>(null)
    val selectedDay = day.asStateFlow()
    val state = year.flatMapLatest { y ->
        explore.daily(DateRanges.dates("$y-01-01", "$y-12-31")).map { CalendarState(y, it) }
            .catch { emit(CalendarState(y, error = "Unable to load calendar.")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarState(year.value))
    val detail = day.flatMapLatest { selected ->
        if (selected == null) flowOf<Recap?>(null) else explore.recap(DateRanges.dates(selected, selected))
            .map<Recap, Recap?> { it }.onStart { emit(null) }.catch { emit(null) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    fun changeYear(change: Int) { year.value = (year.value + change).coerceIn(1900, 2200); day.value = null }
    fun selectDay(date: String?) { day.value = date }
}
