package com.davidegea.spotifystats.domain.model

data class ListeningQuality(
    val events: Long = 0,
    val meaningful: Long = 0,
    val durationKnown: Long = 0,
    val completed: Long = 0,
    val averageCompletion: Double? = null,
)

data class SessionStats(
    val count: Long = 0,
    val averageListeningMs: Long = 0,
    val longestListeningMs: Long = 0,
    val longestSpanMs: Long = 0,
)

data class DailyListening(val date: String, val plays: Long, val listeningMs: Long)
data class Discovery(val artistId: Long, val name: String, val firstPlayedAt: Long, val plays: Long)
data class Rediscovery(val trackId: Long, val name: String, val gapDays: Long)
data class Obsession(val artistId: Long, val name: String, val plays: Long, val previousPlays: Long)
data class ForgottenTrack(val trackId: Long, val name: String, val previousPlays: Long)

data class ListeningTrend(val currentMs: Long = 0, val previousMs: Long = 0) {
    val change: Double? get() = if (previousMs > 0) (currentMs - previousMs).toDouble() / previousMs else null
}

data class AdvancedAnalytics(
    val quality: ListeningQuality = ListeningQuality(),
    val sessions: SessionStats = SessionStats(),
    val discoveries: List<Discovery> = emptyList(),
    val rediscoveries: List<Rediscovery> = emptyList(),
    val obsessions: List<Obsession> = emptyList(),
    val forgotten: List<ForgottenTrack> = emptyList(),
    val trend: ListeningTrend? = null,
    val daily: List<DailyListening> = emptyList(),
)

data class SearchResult(val kind: String, val id: Long, val name: String, val subtitle: String?, val plays: Long)

data class Recap(
    val range: TimeRange,
    val overview: OverviewStats,
    val tracks: List<TrackRanking>,
    val artists: List<ArtistRanking>,
    val albums: List<AlbumRanking>,
    val discoveries: Long,
)
