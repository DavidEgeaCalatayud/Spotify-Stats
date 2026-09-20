package com.davidegea.spotifystats.data.importer

internal data class NormalizedPlay(
    val artistName: String,
    val artistNormalizedName: String,
    val artistIdentity: String,
    val albumName: String?,
    val albumIdentity: String?,
    val trackName: String,
    val trackIdentity: String,
    val spotifyTrackUri: String?,
    val playedAtEpochMs: Long,
    val msPlayed: Long,
    val platform: String?,
    val country: String?,
    val reasonStart: String?,
    val reasonEnd: String?,
    val shuffle: Boolean?,
    val skipped: Boolean?,
    val offline: Boolean?,
    val privateSession: Boolean?,
    val eventHash: String,
)
