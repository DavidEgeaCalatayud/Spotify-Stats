package com.davidegea.spotifystats.ui.components

import com.davidegea.spotifystats.domain.model.DailyListening
import com.davidegea.spotifystats.domain.model.TimeRange
import java.util.Calendar

/** Include silent days so the horizontal axis is an honest calendar, not active-day spacing. */
internal fun chartDays(days: List<DailyListening>, range: TimeRange? = null): List<DailyListening> {
    if (days.isEmpty()) return emptyList()
    val indexed = days.associateBy { it.date }
    val lastDate = if (range != null && range.toInclusive != Long.MAX_VALUE) isoDate(range.toInclusive) else days.maxOf { it.date }
    val end = com.davidegea.spotifystats.domain.analytics.DateRanges.parse(lastDate)
    val calendar = Calendar.getInstance().apply { timeInMillis = end }
    val result = mutableListOf<DailyListening>()
    repeat(30) {
        if (range == null || range.fromInclusive == Long.MIN_VALUE || isoDate(calendar.timeInMillis) >= isoDate(range.fromInclusive)) {
            val key = isoDate(calendar.timeInMillis)
            result += indexed[key] ?: DailyListening(key, 0, 0)
        }
        calendar.add(Calendar.DAY_OF_MONTH, -1)
    }
    return result.reversed()
}
