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
