package com.davidegea.spotifystats.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.davidegea.spotifystats.R

enum class AppDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home("home", R.string.nav_home, Icons.Filled.Home),
    Library("library", R.string.nav_library, Icons.Filled.List),
    Insights("insights", R.string.nav_insights, Icons.Filled.Star),
    You("you", R.string.nav_you, Icons.Filled.Person),
}
