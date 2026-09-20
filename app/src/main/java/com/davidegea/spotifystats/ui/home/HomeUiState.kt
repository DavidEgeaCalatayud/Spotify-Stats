package com.davidegea.spotifystats.ui.home

data class HomeUiState(
    val totalPlays: Long = 0,
    val totalListeningMs: Long = 0,
    val uniqueTracks: Long = 0,
    val uniqueArtists: Long = 0,
)
