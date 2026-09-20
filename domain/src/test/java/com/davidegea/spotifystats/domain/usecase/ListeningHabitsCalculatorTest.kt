package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.HourlyListening
import com.davidegea.spotifystats.domain.model.ListeningHeatmapCell
import com.davidegea.spotifystats.domain.model.PlaybackBehaviorStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ListeningHabitsCalculatorTest {

    private val calculator = ListeningHabitsCalculator()

    @Test
    fun derivesFavouriteTimeSharesAndKnownFlagRates() {
        val result = calculator.calculate(
            hourly = listOf(
                HourlyListening(hour = 8, plays = 2, listeningMs = 60_000),
                HourlyListening(hour = 23, plays = 4, listeningMs = 240_000),
            ),
            heatmap = listOf(
                ListeningHeatmapCell(weekday = 1, hour = 8, plays = 2, listeningMs = 60_000),
                ListeningHeatmapCell(weekday = 6, hour = 23, plays = 4, listeningMs = 240_000),
            ),
            behavior = PlaybackBehaviorStats(
                skippedEvents = 2,
                skipKnownEvents = 10,
                shuffleEvents = 6,
                shuffleKnownEvents = 10,
                offlineEvents = 1,
                offlineKnownEvents = 4,
            ),
        )

        assertEquals(23, result.favouriteHour)
        assertEquals(6, result.favouriteWeekday)
        assertEquals(0.2, result.morningShare ?: 0.0, 0.0001)
        assertEquals(0.8, result.nightShare ?: 0.0, 0.0001)
        assertEquals(0.2, result.skipRate ?: 0.0, 0.0001)
        assertEquals(0.6, result.shuffleRate ?: 0.0, 0.0001)
        assertEquals(0.25, result.offlineRate ?: 0.0, 0.0001)
    }

    @Test
    fun unknownFlagsDoNotBecomeFalseRates() {
        val result = calculator.calculate(
            hourly = emptyList(),
            heatmap = emptyList(),
            behavior = PlaybackBehaviorStats(),
        )

        assertNull(result.morningShare)
        assertNull(result.nightShare)
        assertNull(result.skipRate)
        assertNull(result.shuffleRate)
        assertNull(result.offlineRate)
    }
}
