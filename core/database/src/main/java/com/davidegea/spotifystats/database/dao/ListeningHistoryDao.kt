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

data class TrackDetailRow(
    val id: Long,
    val name: String,
    val artistName: String?,
    val totalPlays: Long,
    val totalListeningMs: Long,
    val firstPlayedAtEpochMs: Long?,
    val lastPlayedAtEpochMs: Long?,
    val skippedPlays: Long,
    val skipKnownPlays: Long,
)

data class ArtistDetailRow(
    val id: Long,
    val name: String,
    val totalPlays: Long,
    val totalListeningMs: Long,
    val uniqueTracks: Long,
    val firstPlayedAtEpochMs: Long?,
    val lastPlayedAtEpochMs: Long?,
)

data class YearlyListeningRow(
    val year: Int,
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
            COUNT(pe.id) AS totalPlays,
            COALESCE(SUM(pe.ms_played), 0) AS totalListeningMs,
            MIN(pe.played_at) AS firstPlayedAtEpochMs,
            MAX(pe.played_at) AS lastPlayedAtEpochMs,
            COALESCE(SUM(CASE WHEN pe.skipped = 1 THEN 1 ELSE 0 END), 0) AS skippedPlays,
            COALESCE(SUM(CASE WHEN pe.skipped IS NOT NULL THEN 1 ELSE 0 END), 0) AS skipKnownPlays
        FROM tracks t
        LEFT JOIN play_events pe ON pe.track_id = t.id
        WHERE t.id = :trackId
        GROUP BY t.id, t.name
        """,
    )
    fun observeTrackDetail(trackId: Long): Flow<TrackDetailRow?>

    @Query(
        """
        SELECT
            CAST(strftime('%Y', played_at / 1000, 'unixepoch') AS INTEGER) AS year,
            COUNT(*) AS plays,
            COALESCE(SUM(ms_played), 0) AS listeningMs
        FROM play_events
        WHERE track_id = :trackId
        GROUP BY year
        ORDER BY year ASC
        """,
    )
    fun observeTrackListeningByYear(trackId: Long): Flow<List<YearlyListeningRow>>

    @Query(
        """
        SELECT
            a.id AS id,
            a.name AS name,
            COUNT(pe.id) AS totalPlays,
            COALESCE(SUM(pe.ms_played), 0) AS totalListeningMs,
            COUNT(DISTINCT CASE WHEN pe.id IS NOT NULL THEN ta.track_id END) AS uniqueTracks,
            MIN(pe.played_at) AS firstPlayedAtEpochMs,
            MAX(pe.played_at) AS lastPlayedAtEpochMs
        FROM artists a
        LEFT JOIN track_artists ta ON ta.artist_id = a.id
        LEFT JOIN play_events pe ON pe.track_id = ta.track_id
        WHERE a.id = :artistId
        GROUP BY a.id, a.name
        """,
    )
    fun observeArtistDetail(artistId: Long): Flow<ArtistDetailRow?>

    @Query(
        """
        SELECT
            t.id AS id,
            t.name AS name,
            a.name AS artistName,
            COUNT(pe.id) AS plays,
            COALESCE(SUM(pe.ms_played), 0) AS listeningMs
        FROM track_artists selected
        INNER JOIN artists a ON a.id = selected.artist_id
        INNER JOIN tracks t ON t.id = selected.track_id
        INNER JOIN play_events pe ON pe.track_id = t.id
        WHERE selected.artist_id = :artistId
        GROUP BY t.id, t.name, a.name
        ORDER BY plays DESC, listeningMs DESC, t.name COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    fun observeArtistTopTracks(
        artistId: Long,
        limit: Int,
    ): Flow<List<TrackRankingRow>>
}
