package com.davidegea.spotifystats.ui.components

import com.davidegea.spotifystats.domain.analytics.DateRanges
import com.davidegea.spotifystats.domain.model.DailyListening
import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Test

class PresentationDataTest {
    @Test fun chartIncludesSilentDaysAndHonoursSelectedBounds() {
        val days = listOf(DailyListening("2026-09-01", 8, 200_000), DailyListening("2026-09-03", 5, 120_000))
        val series = chartDays(days, DateRanges.dates("2026-09-01", "2026-09-04"))
        assertEquals(listOf("2026-09-01", "2026-09-02", "2026-09-03", "2026-09-04"), series.map { it.date })
        assertEquals(listOf(8L, 0L, 5L, 0L), series.map { it.plays })
        assertEquals(1, chartDays(days, DateRanges.dates("2026-09-03", "2026-09-03")).size)
    }

    @Test fun largeRangesAreBoundedToThirtyCalendarDays() {
        val result = chartDays(listOf(DailyListening("2026-09-21", 2, 90_000)), DateRanges.dates("2000-01-01", "2026-09-22"))
        assertEquals(30, result.size)
        assertEquals("2026-08-24", result.first().date)
        assertEquals("2026-09-22", result.last().date)
        assertTrue(chartDays(emptyList()).isEmpty())
    }

    @Test fun pickerUtcDatesBecomeLocalInclusiveDaysAcrossDstAndNegativeOffsets() {
        val original = TimeZone.getDefault()
        try {
            for (zone in listOf("Europe/Madrid", "America/Los_Angeles")) {
                TimeZone.setDefault(TimeZone.getTimeZone(zone))
                for (date in listOf("2026-03-29", "2026-10-25", "2026-09-22")) {
                    val utc = DateRanges.parse(date, TimeZone.getTimeZone("UTC"))
                    val actual = pickerRange(utc, utc)
                    assertEquals(DateRanges.dates(date, date), actual)
                    assertEquals(date, isoDate(actual.fromInclusive))
                    assertEquals(date, isoDate(actual.toInclusive))
                }
            }
        } finally { TimeZone.setDefault(original) }
    }
}
