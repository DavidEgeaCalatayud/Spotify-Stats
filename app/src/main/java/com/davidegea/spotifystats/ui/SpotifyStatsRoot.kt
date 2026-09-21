package com.davidegea.spotifystats.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.davidegea.spotifystats.ui.albumdetail.AlbumDetailRoute
import com.davidegea.spotifystats.ui.artistdetail.ArtistDetailRoute
import com.davidegea.spotifystats.ui.calendar.CalendarRoute
import com.davidegea.spotifystats.ui.home.HomeRoute
import com.davidegea.spotifystats.ui.insights.InsightsRoute
import com.davidegea.spotifystats.ui.library.LibraryRoute
import com.davidegea.spotifystats.ui.onboarding.AppLaunchState
import com.davidegea.spotifystats.ui.onboarding.AppLaunchViewModel
import com.davidegea.spotifystats.ui.onboarding.FirstRunOnboardingScreen
import com.davidegea.spotifystats.ui.trackdetail.TrackDetailRoute
import com.davidegea.spotifystats.ui.wrapped.WrappedRoute
import com.davidegea.spotifystats.ui.you.YouRoute

private const val TRACK_DETAIL_ROUTE = "track/{trackId}"
private const val ARTIST_DETAIL_ROUTE = "artist/{artistId}"
private const val ALBUM_DETAIL_ROUTE = "album/{albumId}"

@Composable
fun SpotifyStatsRoot(
    launchViewModel: AppLaunchViewModel = hiltViewModel(),
) {
    val launchState by launchViewModel.uiState.collectAsStateWithLifecycle()
    var exploreWithoutData by rememberSaveable { mutableStateOf(false) }

    when {
        exploreWithoutData || launchState == AppLaunchState.Ready -> SpotifyStatsAppShell()
        launchState == AppLaunchState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        else -> {
            FirstRunOnboardingScreen(
                onImportStarted = launchViewModel::onImportStarted,
                onImportFinished = launchViewModel::onImportFinished,
                onContinueWithoutImport = { exploreWithoutData = true },
            )
        }
    }
}

@Composable
private fun SpotifyStatsAppShell() {
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
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = stringResource(destination.labelRes),
                                )
                            },
                            label = { Text(stringResource(destination.labelRes)) },
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
                InsightsRoute(
                    onTrack = { navController.navigate("track/$it") },
                    onArtist = { navController.navigate("artist/$it") },
                    onCalendar = { navController.navigate("calendar") },
                )
            }
            composable(AppDestination.You.route) {
                YouRoute(
                    onWrapped = { navController.navigate("wrapped") },
                    onCalendar = { navController.navigate("calendar") },
                )
            }
            composable("calendar") {
                CalendarRoute(
                    onBack = { navController.navigateUp() },
                    onTrack = { navController.navigate("track/$it") },
                    onArtist = { navController.navigate("artist/$it") },
                )
            }
            composable("wrapped") {
                WrappedRoute(onBack = { navController.navigateUp() })
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
