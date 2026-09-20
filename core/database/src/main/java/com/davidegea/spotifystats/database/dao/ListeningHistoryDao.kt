package com.davidegea.spotifystats.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class OverviewStatsRow(
    val totalPlays: Long,
    val totalListeningMs: Long,
    val uniqueTracks: Long,
    val uniqueArtists: Long,
)

@Dao
interface ListeningHistoryDao {

    @Query(
        """
        SELECT
            COUNT(*) AS totalPlays,
            COALESCE(SUM(ms_played), 0) AS totalListeningMs,
            COUNT(DISTINCT track_id) AS uniqueTracks,
            (
                SELECT COUNT(DISTINCT ta.artist_id)
                FROM play_events pe
                INNER JOIN track_artists ta ON ta.track_id = pe.track_id
            ) AS uniqueArtists
        FROM play_events
        """,
    )
    fun observeOverviewStats(): Flow<OverviewStatsRow>
}
