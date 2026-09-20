package com.davidegea.spotifystats.ui

enum class AppDestination(
    val route: String,
    val label: String,
) {
    Home("home", "Home"),
    Library("library", "Library"),
    Insights("insights", "Insights"),
    You("you", "You"),
}
