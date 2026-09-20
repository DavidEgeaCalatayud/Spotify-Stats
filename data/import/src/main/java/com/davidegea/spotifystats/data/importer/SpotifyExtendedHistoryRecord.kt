package com.davidegea.spotifystats.data.importer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyExtendedHistoryRecord(
    @SerialName("ts")
    val timestamp: String? = null,
    val platform: String? = null,
    @SerialName("ms_played")
    val msPlayed: Long? = null,
    @SerialName("conn_country")
    val country: String? = null,
    @SerialName("master_metadata_track_name")
    val trackName: String? = null,
    @SerialName("master_metadata_album_artist_name")
    val artistName: String? = null,
    @SerialName("master_metadata_album_album_name")
    val albumName: String? = null,
    @SerialName("spotify_track_uri")
    val spotifyTrackUri: String? = null,
    @SerialName("episode_name")
    val episodeName: String? = null,
    @SerialName("episode_show_name")
    val episodeShowName: String? = null,
    @SerialName("spotify_episode_uri")
    val spotifyEpisodeUri: String? = null,
    @SerialName("reason_start")
    val reasonStart: String? = null,
    @SerialName("reason_end")
    val reasonEnd: String? = null,
    val shuffle: Boolean? = null,
    val skipped: Boolean? = null,
    val offline: Boolean? = null,
    @SerialName("incognito_mode")
    val privateSession: Boolean? = null,
)
