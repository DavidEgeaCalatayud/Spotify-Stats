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
import com.davidegea.spotifystats.ui.home.HomeRoute
import com.davidegea.spotifystats.ui.library.LibraryRoute
import com.davidegea.spotifystats.ui.placeholder.PlaceholderScreen

@Composable
fun SpotifyStatsRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
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
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppDestination.Home.route) {
                HomeRoute()
            }
            composable(AppDestination.Library.route) {
                LibraryRoute()
            }
            composable(AppDestination.Insights.route) {
                PlaceholderScreen(
                    title = "Insights",
                    body = "Trends, discoveries, obsessions, habits and Wrapped reports will live here.",
                )
            }
            composable(AppDestination.You.route) {
                PlaceholderScreen(
                    title = "You",
                    body = "Backups, privacy controls and optional Spotify sync will live here.",
                )
            }
        }
    }
}
