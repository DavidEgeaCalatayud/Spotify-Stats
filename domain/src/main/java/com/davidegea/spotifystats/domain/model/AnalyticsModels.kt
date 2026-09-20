package com.davidegea.spotifystats.domain.model

enum class AnalyticsPeriod(
    val label: String,
) {
    LAST_7_DAYS("7 days"),
    LAST_30_DAYS("30 days"),
    THIS_YEAR("This year"),
    ALL_TIME("All time"),
}

data class TimeRange(
    val fromInclusive: Long,
    val toInclusive: Long,
)

data class TrackRanking(
    val id: Long,
    val name: String,
    val artistName: String?,
    val plays: Long,
    val listeningMs: Long,
)

data class ArtistRanking(
    val id: Long,
    val name: String,
    val plays: Long,
    val listeningMs: Long,
)

data class AlbumRanking(
    val id: Long,
    val name: String,
    val artistName: String?,
    val plays: Long,
    val listeningMs: Long,
)

data class LibraryRankings(
    val tracks: List<TrackRanking>,
    val artists: List<ArtistRanking>,
    val albums: List<AlbumRanking>,
)

data class YearlyListening(
    val year: Int,
    val plays: Long,
    val listeningMs: Long,
)

data class TrackDetail(
    val id: Long,
    val name: String,
    val artistName: String?,
    val totalPlays: Long,
    val totalListeningMs: Long,
    val firstPlayedAtEpochMs: Long?,
    val lastPlayedAtEpochMs: Long?,
    val skippedPlays: Long,
    val skipKnownPlays: Long,
    val playsByYear: List<YearlyListening> = emptyList(),
)

data class ArtistDetail(
    val id: Long,
    val name: String,
    val totalPlays: Long,
    val totalListeningMs: Long,
    val uniqueTracks: Long,
    val firstPlayedAtEpochMs: Long?,
    val lastPlayedAtEpochMs: Long?,
    val topTracks: List<TrackRanking> = emptyList(),
)

data class AlbumDetail(
    val id: Long,
    val name: String,
    val artistName: String?,
    val totalPlays: Long,
    val totalListeningMs: Long,
    val uniqueTracks: Long,
    val firstPlayedAtEpochMs: Long?,
    val lastPlayedAtEpochMs: Long?,
    val topTracks: List<TrackRanking> = emptyList(),
)

data class ListeningHistoryItem(
    val eventId: Long,
    val trackId: Long,
    val trackName: String,
    val artistName: String?,
    val albumName: String?,
    val playedAtEpochMs: Long,
    val listeningMs: Long,
    val skipped: Boolean?,
)
