package com.davidegea.spotifystats.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Shared rhythm and illustration palette. Data remains legible in both app themes. */
object StatsSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val contentWidth = 960.dp
}

object StatsPalette {
    val ink = Color(0xFF102A2A)
    val forest = Color(0xFF215B50)
    val mint = Color(0xFFB5F5D8)
    val lilac = Color(0xFFD2C5FF)
    val peach = Color(0xFFFFD5AD)
    val artwork = listOf(
        Color(0xFF275A54), Color(0xFF4D4679), Color(0xFF784457),
        Color(0xFF375C80), Color(0xFF805831), Color(0xFF53623D),
    )
}
