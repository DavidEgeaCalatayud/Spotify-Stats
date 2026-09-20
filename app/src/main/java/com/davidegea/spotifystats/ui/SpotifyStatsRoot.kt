package com.davidegea.spotifystats.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.davidegea.spotifystats.ui.albumdetail.AlbumDetailRoute
import com.davidegea.spotifystats.ui.artistdetail.ArtistDetailRoute
import com.davidegea.spotifystats.ui.home.HomeRoute
import com.davidegea.spotifystats.ui.insights.InsightsRoute
import com.davidegea.spotifystats.ui.library.LibraryRoute
import com.davidegea.spotifystats.ui.placeholder.PlaceholderScreen
import com.davidegea.spotifystats.ui.trackdetail.TrackDetailRoute

private const val TRACK_DETAIL_ROUTE = "track/{trackId}"
private const val ARTIST_DETAIL_ROUTE = "artist/{artistId}"
private const val ALBUM_DETAIL_ROUTE = "album/{albumId}"

@Composable
fun SpotifyStatsRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevelRoutes = AppDestination.entries.map { it.route }.toSet()
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    AppDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(destination.label.take(1)) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppDestination.Home.route) {
                HomeRoute(
                    onTrackClick = { trackId ->
                        navController.navigate("track/" + trackId)
                    },
                    onArtistClick = { artistId ->
                        navController.navigate("artist/" + artistId)
                    },
                    onAlbumClick = { albumId ->
                        navController.navigate("album/" + albumId)
                    },
                )
            }
            composable(AppDestination.Library.route) {
                LibraryRoute(
                    onTrackClick = { trackId ->
                        navController.navigate("track/" + trackId)
                    },
                    onArtistClick = { artistId ->
                        navController.navigate("artist/" + artistId)
                    },
                    onAlbumClick = { albumId ->
                        navController.navigate("album/" + albumId)
                    },
                )
            }
            composable(AppDestination.Insights.route) {
                InsightsRoute()
            }
            composable(AppDestination.You.route) {
                PlaceholderScreen(
                    title = "You",
                    body = "Backups, privacy controls and optional Spotify sync will live here.",
                )
            }
            composable(TRACK_DETAIL_ROUTE) {
                TrackDetailRoute(onBack = navController::navigateUp)
            }
            composable(ARTIST_DETAIL_ROUTE) {
                ArtistDetailRoute(onBack = navController::navigateUp)
            }
            composable(ALBUM_DETAIL_ROUTE) {
                AlbumDetailRoute(
                    onBack = navController::navigateUp,
                    onTrackClick = { trackId ->
                        navController.navigate("track/" + trackId)
                    },
                )
            }
        }
    }
}
