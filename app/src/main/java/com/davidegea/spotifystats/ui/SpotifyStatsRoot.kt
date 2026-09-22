package com.davidegea.spotifystats.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.domain.analytics.DateRanges
import com.davidegea.spotifystats.ui.components.StatsPage
import com.davidegea.spotifystats.ui.components.StatsTopBar
import com.davidegea.spotifystats.ui.importhistory.ImportHistorySection
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
    val showNavigation = currentRoute in AppDestination.entries.map { it.route }
    val navigate: (AppDestination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 840.dp
        Scaffold(bottomBar = {
            if (showNavigation && !expanded) NavigationBar {
                AppDestination.entries.forEach { destination ->
                    NavigationBarItem(selected = currentRoute == destination.route, onClick = { navigate(destination) },
                        icon = { Icon(destination.icon, null) }, label = { Text(stringResource(destination.labelRes)) })
                }
            }
        }) { innerPadding ->
            Row(Modifier.fillMaxSize().padding(innerPadding)) {
                if (showNavigation && expanded) NavigationRail(Modifier.fillMaxHeight()) {
                    AppDestination.entries.forEach { destination ->
                        NavigationRailItem(selected = currentRoute == destination.route, onClick = { navigate(destination) },
                            icon = { Icon(destination.icon, null) }, label = { Text(stringResource(destination.labelRes)) })
                    }
                }
                NavHost(
                    navController = navController, startDestination = AppDestination.Home.route,
                    modifier = Modifier.weight(1f),
                    enterTransition = { fadeIn(tween(240)) }, exitTransition = { fadeOut(tween(180)) },
                    popEnterTransition = { fadeIn(tween(240)) }, popExitTransition = { fadeOut(tween(180)) },
                ) {
                    composable(AppDestination.Home.route) {
                        HomeRoute(onTrackClick = { navController.navigate("track/$it") },
                            onArtistClick = { navController.navigate("artist/$it") },
                            onAlbumClick = { navController.navigate("album/$it") },
                            onImport = { navController.navigate("import") })
                    }
                    composable(AppDestination.Library.route) {
                        LibraryRoute(onTrackClick = { navController.navigate("track/$it") },
                            onArtistClick = { navController.navigate("artist/$it") },
                            onAlbumClick = { navController.navigate("album/$it") })
                    }
                    composable(AppDestination.Insights.route) {
                        InsightsRoute(onTrack = { navController.navigate("track/$it") },
                            onArtist = { navController.navigate("artist/$it") },
                            onCalendar = { navController.navigate("calendar") })
                    }
                    composable(AppDestination.You.route) {
                        YouRoute(onWrapped = { navController.navigate("wrapped") },
                            onCalendar = { navController.navigate("calendar") }, onImport = { navController.navigate("import") })
                    }
                    composable("calendar") {
                        CalendarRoute(onBack = { navController.navigateUp() }, onTrack = { navController.navigate("track/$it") },
                            onArtist = { navController.navigate("artist/$it") }, onDay = { navController.navigate("day/$it") })
                    }
                    composable("day/{date}") { entry ->
                        val date = requireNotNull(entry.arguments?.getString("date"))
                        Column(Modifier.fillMaxSize()) {
                            StatsTopBar(date, onBack = { navController.navigateUp() })
                            Box(Modifier.weight(1f)) {
                                LibraryRoute(onTrackClick = { navController.navigate("track/$it") },
                                    onArtistClick = { navController.navigate("artist/$it") },
                                    onAlbumClick = { navController.navigate("album/$it") },
                                    initialRange = DateRanges.dates(date, date), initialHistory = true)
                            }
                        }
                    }
                    composable("import") { StatsPage(stringResource(R.string.import_title), { navController.navigateUp() }) { ImportHistorySection() } }
                    composable("wrapped") { WrappedRoute(onBack = { navController.navigateUp() }) }
                    composable(TRACK_DETAIL_ROUTE) { TrackDetailRoute(onBack = { navController.navigateUp() }) }
                    composable(ARTIST_DETAIL_ROUTE) { ArtistDetailRoute(onBack = { navController.navigateUp() }) }
                    composable(ALBUM_DETAIL_ROUTE) { AlbumDetailRoute(onBack = { navController.navigateUp() }, onTrackClick = { navController.navigate("track/$it") }) }
                }
            }
        }
    }
}
