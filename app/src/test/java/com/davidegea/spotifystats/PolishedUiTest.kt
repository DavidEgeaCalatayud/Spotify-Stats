package com.davidegea.spotifystats

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.davidegea.spotifystats.designsystem.SpotifyStatsTheme
import com.davidegea.spotifystats.domain.analytics.DateRanges
import com.davidegea.spotifystats.domain.model.*
import com.davidegea.spotifystats.ui.components.ActivityChart
import com.davidegea.spotifystats.ui.home.HomeScreen
import com.davidegea.spotifystats.ui.home.HomeUiState
import com.davidegea.spotifystats.ui.insights.ListeningHeatmap
import com.davidegea.spotifystats.ui.library.LibraryScreen
import com.davidegea.spotifystats.ui.library.LibraryUiState
import com.davidegea.spotifystats.ui.wrapped.WrappedStories
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class, qualifiers = "w411dp-h891dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PolishedUiTest {
    @get:Rule val compose = createComposeRule()
    private val range = DateRanges.dates("2026-09-01", "2026-09-22")
    private val tracks = listOf(
        TrackRanking(1, "Blinding Lights", "The Weeknd", 84, 16_560_000),
        TrackRanking(2, "Midnight City", "M83", 62, 13_640_000),
        TrackRanking(3, "Instant Crush", "Daft Punk", 47, 10_800_000),
        TrackRanking(4, "Something About Us", "Daft Punk", 36, 8_300_000),
    )
    private val artists = listOf(ArtistRanking(7, "The Weeknd", 294, 46_000_000))
    private val albums = listOf(AlbumRanking(9, "After Hours", "The Weeknd", 136, 34_000_000))
    private val days = (1..22).map { day -> DailyListening("2026-09-" + day.toString().padStart(2, '0'), (day * 7).toLong(), (day % 6 + 1) * 1_800_000L) }

    @Test fun homeHasOneHeroAndACompactImportAction() {
        compose.setContent { SpotifyStatsTheme(darkTheme = true) { Surface {
            HomeScreen(HomeUiState(loading = false, totalPlays = 2847, totalListeningMs = 329_040_000, uniqueTracks = 312, uniqueArtists = 87,
                customRange = range, range = range, daily = days, topTrack = tracks[0], topArtist = artists[0], topAlbum = albums[0]), {}, {}, {}, {}, {}, {})
        } } }
        compose.onNodeWithText("2,847").assertExists()
        snapshot("home-dark")
        compose.onNodeWithText("Import more history").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Choose history files").assertCountEquals(0)
    }

    @Test fun libraryTabsKeepRankingNavigationAndSearch() {
        var clicked = 0L
        compose.setContent { SpotifyStatsTheme { Surface {
            LibraryScreen(LibraryUiState(tracks = tracks, artists = artists, albums = albums), {}, {}, {}, {}, {}, { clicked = it }, { clicked = it }, { clicked = it })
        } } }
        compose.onNodeWithText("Blinding Lights").assertExists()
        snapshot("library-light")
        compose.onNodeWithText("Artists").performClick()
        compose.onNodeWithText("The Weeknd").performClick()
        assertEquals(7L, clicked)
        compose.onNodeWithText("History").performClick()
        compose.onNodeWithText("No listening data yet").assertExists()
    }

    @Test fun heatmapCanBeExploredWithoutSmallTouchTargets() {
        compose.setContent { SpotifyStatsTheme { Surface { Column {
            ListeningHeatmap(listOf(ListeningHeatmapCell(2, 21, 84, 16_320_000), ListeningHeatmapCell(1, 21, 3, 60_000)))
        } } } }
        compose.onNodeWithText("Tue · 21:00 — 4h 32m · 84 plays").assertExists()
        snapshot("heatmap-light")
        compose.onNodeWithText("Mon").performClick()
        compose.onNodeWithText("Mon · 21:00 — 0h 1m · 3 plays").assertExists()
    }

    @Test fun chartSelectionIncludesZeroListeningDays() {
        compose.setContent { SpotifyStatsTheme { Surface {
            ActivityChart(listOf(DailyListening("2026-09-01", 8, 600_000)), DateRanges.dates("2026-09-01", "2026-09-02"))
        } } }
        compose.onNodeWithText("2026-09-02 · 0 plays · 0h 0m").assertExists()
        compose.onNodeWithText("Previous day").performClick()
        compose.onNodeWithText("2026-09-01 · 8 plays · 0h 10m").assertExists()
    }

    @Test fun wrappedHasSwipeAndButtonNavigationAndShareActions() {
        compose.setContent { SpotifyStatsTheme { WrappedStories(Recap(range, OverviewStats(2847, 329_040_000, 312, 87), tracks, artists, albums, 28), {}) } }
        compose.onNodeWithText("Story 1 of 4").assertExists()
        snapshot("wrapped-overview")
        compose.onNodeWithText("Next").performClick()
        compose.onNodeWithText("Blinding Lights").assertIsDisplayed()
        compose.onNodeWithText("Next").performClick()
        compose.onNodeWithText("The Weeknd").assertIsDisplayed()
        compose.onNodeWithText("Next").performClick()
        compose.onNodeWithText("Share story sequence").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Share overview card").assertExists()
    }

    private fun snapshot(name: String) {
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File("build/reports/ui-polish").mkdirs()
        File("build/reports/ui-polish/$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
