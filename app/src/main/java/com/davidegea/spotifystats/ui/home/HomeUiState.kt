package com.davidegea.spotifystats.ui.home

import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.ListeningHistoryItem
import com.davidegea.spotifystats.domain.model.TrackRanking

data class HomeUiState(
    val period: AnalyticsPeriod = AnalyticsPeriod.LAST_30_DAYS,
    val customRange: com.davidegea.spotifystats.domain.model.TimeRange? = null,
    val daily: List<com.davidegea.spotifystats.domain.model.DailyListening> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val totalPlays: Long = 0,
    val totalListeningMs: Long = 0,
    val uniqueTracks: Long = 0,
    val uniqueArtists: Long = 0,
    val topTrack: TrackRanking? = null,
    val topArtist: ArtistRanking? = null,
    val topAlbum: AlbumRanking? = null,
    val recentActivity: List<ListeningHistoryItem> = emptyList(),
)
