package com.davidegea.spotifystats.data.importer

class SpotifyPlayNormalizer(
    private val identityKeys: IdentityKeyFactory = IdentityKeyFactory(),
    private val timestampParser: SpotifyTimestampParser = SpotifyTimestampParser(),
) {

    internal fun normalize(record: SpotifyExtendedHistoryRecord): NormalizedPlay? {
        val trackName = record.trackName?.trim()?.takeIf(String::isNotEmpty) ?: return null
        val artistName = record.artistName?.trim()?.takeIf(String::isNotEmpty) ?: return null
        val timestamp = record.timestamp?.trim()?.takeIf(String::isNotEmpty) ?: return null
        val playedAt = timestampParser.parseEpochMillis(timestamp)?.takeIf { it in 0..7_289_654_399_999L } ?: return null
        val msPlayed = record.msPlayed?.takeIf { it in 0..86_400_000L } ?: return null

        val artistIdentity = identityKeys.artist(artistName)
        val albumName = record.albumName?.trim()?.takeIf(String::isNotEmpty)
        val albumIdentity = albumName?.let { identityKeys.album(artistIdentity, it) }
        val trackIdentity = identityKeys.track(
            spotifyUri = record.spotifyTrackUri,
            artistIdentity = artistIdentity,
            albumIdentity = albumIdentity,
            trackName = trackName,
        )

        return NormalizedPlay(
            artistName = artistName,
            artistNormalizedName = identityKeys.normalize(artistName),
            artistIdentity = artistIdentity,
            albumName = albumName,
            albumIdentity = albumIdentity,
            trackName = trackName,
            trackIdentity = trackIdentity,
            spotifyTrackUri = record.spotifyTrackUri?.trim()?.takeIf(String::isNotEmpty),
            playedAtEpochMs = playedAt,
            msPlayed = msPlayed,
            platform = record.platform,
            country = record.country,
            reasonStart = record.reasonStart,
            reasonEnd = record.reasonEnd,
            shuffle = record.shuffle,
            skipped = record.skipped,
            offline = record.offline,
            privateSession = record.privateSession,
            eventHash = identityKeys.event(trackIdentity, playedAt, msPlayed),
        )
    }
}
