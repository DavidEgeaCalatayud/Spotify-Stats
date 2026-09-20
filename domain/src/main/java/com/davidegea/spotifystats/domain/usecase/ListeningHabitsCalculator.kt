package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.HourlyListening
import com.davidegea.spotifystats.domain.model.ListeningHabits
import com.davidegea.spotifystats.domain.model.ListeningHeatmapCell
import com.davidegea.spotifystats.domain.model.PlaybackBehaviorStats

class ListeningHabitsCalculator {

    fun calculate(
        hourly: List<HourlyListening>,
        heatmap: List<ListeningHeatmapCell>,
        behavior: PlaybackBehaviorStats,
    ): ListeningHabits {
        val totalListeningMs = hourly.sumOf(HourlyListening::listeningMs)

        val favouriteHour = hourly
            .maxWithOrNull(
                compareBy<HourlyListening> { it.listeningMs }
                    .thenBy { it.plays },
            )
            ?.hour

        val favouriteWeekday = heatmap
            .groupBy(ListeningHeatmapCell::weekday)
            .mapValues { (_, cells) -> cells.sumOf(ListeningHeatmapCell::listeningMs) }
            .maxByOrNull { it.value }
            ?.key

        val morningListening = hourly
            .filter { it.hour in MORNING_HOURS }
            .sumOf(HourlyListening::listeningMs)
        val nightListening = hourly
            .filter { it.hour in NIGHT_HOURS }
            .sumOf(HourlyListening::listeningMs)

        return ListeningHabits(
            favouriteHour = favouriteHour,
            favouriteWeekday = favouriteWeekday,
            morningShare = ratio(morningListening, totalListeningMs),
            nightShare = ratio(nightListening, totalListeningMs),
            skipRate = ratio(behavior.skippedEvents, behavior.skipKnownEvents),
            shuffleRate = ratio(behavior.shuffleEvents, behavior.shuffleKnownEvents),
            offlineRate = ratio(behavior.offlineEvents, behavior.offlineKnownEvents),
            heatmap = heatmap,
        )
    }

    private fun ratio(
        numerator: Long,
        denominator: Long,
    ): Double? =
        if (denominator > 0L) {
            numerator.toDouble() / denominator.toDouble()
        } else {
            null
        }

    private companion object {
        val MORNING_HOURS = 6..11
        val NIGHT_HOURS = (0..5) + (22..23)
    }
}
