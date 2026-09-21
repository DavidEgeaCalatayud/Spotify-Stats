package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.analytics.DateRanges
import com.davidegea.spotifystats.domain.analytics.SessionAccumulator
import com.davidegea.spotifystats.domain.model.ListeningTrend
import org.junit.Assert.*
import org.junit.Test
import java.util.TimeZone

class AdvancedAnalyticsTest {
    @Test fun sessionsRespectBreakBoundaryAndIgnoreZeroPlays() {
        val accumulator = SessionAccumulator(100)
        accumulator.add(0, 10, 10)
        accumulator.add(110, 130, 20)
        accumulator.add(231, 260, 29)
        accumulator.add(999, 999, 0)
        val result = accumulator.result()
        assertEquals(2L, result.count)
        assertEquals(30L, result.longestListeningMs)
        assertEquals(130L, result.longestSpanMs)
        assertEquals(29L, result.averageListeningMs)
    }
    @Test fun overlappingStreamsDoNotMoveSessionEndBackwards() {
        val accumulator = SessionAccumulator(10)
        accumulator.add(0, 100, 100)
        accumulator.add(20, 30, 10)
        accumulator.add(105, 110, 5)
        assertEquals(1L, accumulator.result().count)
        assertEquals(115L, accumulator.result().longestListeningMs)
    }
    @Test fun localDatesHandleBothDstTransitions() {
        val zone = TimeZone.getTimeZone("Europe/Madrid")
        val spring = DateRanges.dates("2026-03-29", "2026-03-29", zone)
        val autumn = DateRanges.dates("2026-10-25", "2026-10-25", zone)
        assertEquals(23 * 3_600_000L, spring.toInclusive - spring.fromInclusive + 1)
        assertEquals(25 * 3_600_000L, autumn.toInclusive - autumn.fromInclusive + 1)
        assertEquals(spring.fromInclusive - 1, DateRanges.previous(spring)?.toInclusive)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsInvalidDates() { DateRanges.dates("2026-02-30", "2026-03-01") }
    @Test(expected = IllegalArgumentException::class) fun rejectsReversedRanges() { DateRanges.dates("2026-03-30", "2026-03-01") }
    @Test fun zeroBaselineDoesNotInventPercentage() { assertNull(ListeningTrend(10, 0).change) }
}
