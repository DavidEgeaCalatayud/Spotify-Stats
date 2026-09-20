package com.davidegea.spotifystats

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Exercises the real activity, Hilt graph, Room database and navigation-scoped ViewModels. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = SpotifyStatsApplication::class)
class NavigationSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun startsOfflineAndNavigatesAllTopLevelScreensAndRecaps() {
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Choose history files").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Library").performClick()
        compose.onNodeWithText("Search songs, artists and albums").assertExists()
        compose.onNodeWithText("Insights").performClick()
        compose.onNodeWithText("Open listening calendar").assertExists()
        compose.onNodeWithText("You").performClick()
        compose.onNodeWithText("Generate your Wrapped").performClick()
        compose.onNodeWithText("Your Wrapped").assertExists()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Listening calendar").performClick()
        compose.onNodeWithText("Listening calendar").assertExists()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Your data, on your device").assertExists()
    }
}
