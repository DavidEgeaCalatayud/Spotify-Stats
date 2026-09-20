package com.davidegea.spotifystats.domain.model

data class OverviewStats(
    val totalPlays: Long,
    val totalListeningMs: Long,
    val uniqueTracks: Long,
    val uniqueArtists: Long,
)
