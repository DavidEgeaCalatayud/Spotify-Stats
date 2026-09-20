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

data class TrackRankingRow(
    val id: Long,
    val name: String,
    val artistName: String?,
    val plays: Long,
    val listeningMs: Long,
)

data class ArtistRankingRow(
    val id: Long,
    val name: String,
    val plays: Long,
    val listeningMs: Long,
)

data class AlbumRankingRow(
    val id: Long,
    val name: String,
    val artistName: String?,
    val plays: Long,
    val listeningMs: Long,
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

    @Query(
        """
        SELECT
            t.id AS id,
            t.name AS name,
            (
                SELECT a.name
                FROM track_artists ta
                INNER JOIN artists a ON a.id = ta.artist_id
                WHERE ta.track_id = t.id
                ORDER BY ta.position ASC
                LIMIT 1
            ) AS artistName,
            COUNT(pe.id) AS plays,
            COALESCE(SUM(pe.ms_played), 0) AS listeningMs
        FROM play_events pe
        INNER JOIN tracks t ON t.id = pe.track_id
        WHERE pe.played_at >= :fromInclusive
          AND pe.played_at <= :toInclusive
        GROUP BY t.id, t.name
        ORDER BY plays DESC, listeningMs DESC, t.name COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    fun observeTopTracks(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<TrackRankingRow>>

    @Query(
        """
        SELECT
            a.id AS id,
            a.name AS name,
            COUNT(pe.id) AS plays,
            COALESCE(SUM(pe.ms_played), 0) AS listeningMs
        FROM play_events pe
        INNER JOIN track_artists ta ON ta.track_id = pe.track_id
        INNER JOIN artists a ON a.id = ta.artist_id
        WHERE pe.played_at >= :fromInclusive
          AND pe.played_at <= :toInclusive
        GROUP BY a.id, a.name
        ORDER BY plays DESC, listeningMs DESC, a.name COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    fun observeTopArtists(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<ArtistRankingRow>>

    @Query(
        """
        SELECT
            al.id AS id,
            al.name AS name,
            (
                SELECT a.name
                FROM tracks at
                INNER JOIN track_artists ta ON ta.track_id = at.id
                INNER JOIN artists a ON a.id = ta.artist_id
                WHERE at.album_id = al.id
                ORDER BY ta.position ASC, a.name COLLATE NOCASE ASC
                LIMIT 1
            ) AS artistName,
            COUNT(pe.id) AS plays,
            COALESCE(SUM(pe.ms_played), 0) AS listeningMs
        FROM play_events pe
        INNER JOIN tracks t ON t.id = pe.track_id
        INNER JOIN albums al ON al.id = t.album_id
        WHERE pe.played_at >= :fromInclusive
          AND pe.played_at <= :toInclusive
        GROUP BY al.id, al.name
        ORDER BY plays DESC, listeningMs DESC, al.name COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    fun observeTopAlbums(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<AlbumRankingRow>>
}
