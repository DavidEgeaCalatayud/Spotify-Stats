package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsTimeRangeResolverTest {

    private val utc = TimeZone.getTimeZone("UTC")
    private val now = Calendar.getInstance(utc).apply {
        set(2026, Calendar.SEPTEMBER, 20, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val resolver = AnalyticsTimeRangeResolver(
        nowProvider = { now },
        timeZone = utc,
    )

    @Test
    fun lastSevenDaysUsesRollingSevenDayRange() {
        val range = resolver.resolve(AnalyticsPeriod.LAST_7_DAYS)

        assertEquals(now - TimeUnit.DAYS.toMillis(7), range.fromInclusive)
        assertEquals(now, range.toInclusive)
    }

    @Test
    fun currentYearStartsAtLocalJanuaryFirst() {
        val range = resolver.resolve(AnalyticsPeriod.THIS_YEAR)
        val expected = Calendar.getInstance(utc).apply {
            set(2026, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        assertEquals(expected, range.fromInclusive)
        assertEquals(now, range.toInclusive)
    }

    @Test
    fun allTimeUsesFullLongRange() {
        val range = resolver.resolve(AnalyticsPeriod.ALL_TIME)

        assertEquals(Long.MIN_VALUE, range.fromInclusive)
        assertEquals(Long.MAX_VALUE, range.toInclusive)
    }
}
