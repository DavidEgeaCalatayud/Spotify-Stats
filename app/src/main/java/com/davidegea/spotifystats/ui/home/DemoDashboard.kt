package com.davidegea.spotifystats.ui.home

import com.davidegea.spotifystats.domain.model.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Clearly labelled SAMPLE statistics. Never inserts play events into Room.
 * The preview is stable per day and requires no account, files or network.
 */
object DemoDashboard {
    fun sample(): HomeUiState {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -29)
        val days = (0 until 30).map { index ->
            val day = DailyListening(
                date = formatter.format(calendar.time),
                plays = (34 + ((index * 13 + 7) % 55)).toLong(),
                listeningMs = (72 + ((index * 17 + 11) % 130)) * 60_000L,
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            day
        }
        return HomeUiState(
            loading = false,
            period = AnalyticsPeriod.LAST_30_DAYS,
            totalPlays = days.sumOf { it.plays },
            totalListeningMs = days.sumOf { it.listeningMs },
            uniqueTracks = 312,
            uniqueArtists = 87,
            daily = days,
            previousListeningMs = days.sumOf { it.listeningMs } * 78 / 100,
            topArtist = ArtistRanking(
                id = -1, name = "The Weeknd", plays = 294, listeningMs = 46_000_000L,
                representativeTrackUri = "spotify:track:0VjIjW4GlUZAMYd2vXMi3b",
            ),
            topTrack = TrackRanking(
                id = -2, name = "Blinding Lights", artistName = "The Weeknd",
                plays = 84, listeningMs = 16_560_000L,
                spotifyUri = "spotify:track:0VjIjW4GlUZAMYd2vXMi3b",
            ),
            topAlbum = AlbumRanking(
                id = -3, name = "After Hours", artistName = "The Weeknd",
                plays = 136, listeningMs = 34_000_000L,
                representativeTrackUri = "spotify:track:0VjIjW4GlUZAMYd2vXMi3b",
            ),
        )
    }
}
