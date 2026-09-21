package com.davidegea.spotifystats.domain.model

data class MetadataLookup(
    val trackId: Long,
    val spotifyUri: String,
    val trackName: String,
    val artistName: String?,
    val albumId: Long?,
    val albumName: String?,
)

data class ProviderTrackMetadata(
    val providerTrackId: String?,
    val durationMs: Long?,
    val albumReleaseDate: String?,
    val artworkBytes: ByteArray?,
    val artworkMimeType: String?,
)

data class TrackMetadata(
    val trackId: Long,
    val provider: String,
    val providerTrackId: String?,
    val durationMs: Long?,
    val artworkPath: String?,
    val refreshedAtEpochMs: Long,
)

data class AlbumMetadata(
    val albumId: Long,
    val provider: String,
    val releaseDate: String?,
    val artworkPath: String?,
    val refreshedAtEpochMs: Long,
)

data class MetadataRefreshResult(
    val attempted: Int,
    val updated: Int,
    val unavailable: Int,
    val failed: Int,
    val providerConfigured: Boolean,
)
