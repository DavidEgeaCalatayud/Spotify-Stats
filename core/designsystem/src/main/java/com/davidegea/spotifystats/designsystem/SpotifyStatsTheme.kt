package com.davidegea.spotifystats.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    surfaceContainer = Color(0xFFEBEFE8),
    surfaceContainerLow = Color(0xFFF0F3EC),
    surfaceContainerHigh = Color(0xFFE3EAE1),
    tertiary = Color(0xFF655492),
    tertiaryContainer = Color(0xFFEADDFF),
    onTertiaryContainer = Color(0xFF251244),
    primary = Color(0xFF116B5B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F1DF),
    onPrimaryContainer = Color(0xFF002019),
    secondary = Color(0xFF4D635D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E8DF),
    onSecondaryContainer = Color(0xFF0A1F1A),
    background = Color(0xFFF6F7F2),
    onBackground = Color(0xFF171D1B),
    surface = Color(0xFFF6F7F2),
    onSurface = Color(0xFF171D1B),
    surfaceVariant = Color(0xFFDDE5E1),
    onSurfaceVariant = Color(0xFF414946),
)

private val DarkColors = darkColorScheme(
    surfaceContainer = Color(0xFF1C2824),
    surfaceContainerLow = Color(0xFF18221F),
    surfaceContainerHigh = Color(0xFF27352F),
    tertiary = Color(0xFFD2C5FF),
    tertiaryContainer = Color(0xFF483B67),
    onTertiaryContainer = Color(0xFFEDE5FF),
    primary = Color(0xFF63D7BD),
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF005143),
    onPrimaryContainer = Color(0xFF82F4D8),
    secondary = Color(0xFFB4CCC3),
    onSecondary = Color(0xFF20352F),
    secondaryContainer = Color(0xFF374B45),
    onSecondaryContainer = Color(0xFFD0E8DF),
    background = Color(0xFF101817),
    onBackground = Color(0xFFDDE5E1),
    surface = Color(0xFF101817),
    onSurface = Color(0xFFDDE5E1),
    surfaceVariant = Color(0xFF414946),
    onSurfaceVariant = Color(0xFFC1C9C5),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
)

@Composable
fun SpotifyStatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
