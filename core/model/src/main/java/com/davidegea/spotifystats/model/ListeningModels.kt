package com.davidegea.spotifystats.model

data class Track(
    val id: Long,
    val spotifyUri: String?,
    val name: String,
    val albumId: Long?,
    val durationMs: Long?,
)

data class Artist(
    val id: Long,
    val spotifyId: String?,
    val name: String,
)

data class Album(
    val id: Long,
    val spotifyId: String?,
    val name: String,
    val releaseDate: String?,
)

enum class PlaySource {
    SPOTIFY_EXPORT,
    SPOTIFY_API,
    ANDROID_CAPTURE,
}

data class PlayEvent(
    val id: Long,
    val trackId: Long,
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
    val source: PlaySource,
)
