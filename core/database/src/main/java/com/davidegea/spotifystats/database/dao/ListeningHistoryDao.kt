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
    val meaningfulPlays: Long,
    val averageCompletion: Double?,
    val completedPlays: Long,
    val favouriteHour: Int?,
)

data class ArtistDetailRow(
    val id: Long,
    val name: String,
    val totalPlays: Long,
    val totalListeningMs: Long,
    val allTimeRank: Long,
    val mostActiveYear: Int?,
    val uniqueTracks: Long,
    val firstPlayedAtEpochMs: Long?,
    val lastPlayedAtEpochMs: Long?,
)

data class AlbumDetailRow(
    val id: Long,
    val name: String,
    val artistName: String?,
    val totalPlays: Long,
    val totalListeningMs: Long,
    val peakMonth: String?,
    val uniqueTracks: Long,
    val activeDays: Long,
    val meaningfulPlays: Long,
    val firstPlayedAtEpochMs: Long?,
    val lastPlayedAtEpochMs: Long?,
)

data class ListeningHistoryRow(
    val id: Long,
    val trackId: Long,
    val trackName: String,
    val artistName: String?,
    val albumName: String?,
    val playedAtEpochMs: Long,
    val listeningMs: Long,
    val skipped: Boolean?,
)

data class YearlyListeningRow(
    val year: Int,
    val plays: Long,
    val listeningMs: Long,
)

data class ArtistYearRankRow(
    val year: Int,
    val artistRank: Long,
    val plays: Long,
    val listeningMs: Long,
)

data class ArtistRangeRankRow(
    val artistRank: Long,
    val plays: Long,
    val listeningMs: Long,
    val uniqueTracks: Long,
)

data class HourlyListeningRow(
    val hour: Int,
    val plays: Long,
    val listeningMs: Long,
)

data class ListeningHeatmapRow(
    val weekday: Int,
    val hour: Int,
    val plays: Long,
    val listeningMs: Long,
)

data class PlaybackBehaviorRow(
    val skippedEvents: Long,
    val skipKnownEvents: Long,
    val shuffleEvents: Long,
    val shuffleKnownEvents: Long,
    val offlineEvents: Long,
    val offlineKnownEvents: Long,
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
                FROM play_events artist_pe
                INNER JOIN track_artists ta ON ta.track_id = artist_pe.track_id
                WHERE artist_pe.played_at >= :fromInclusive
                  AND artist_pe.played_at <= :toInclusive
            ) AS uniqueArtists
        FROM play_events
        WHERE played_at >= :fromInclusive
          AND played_at <= :toInclusive
        """,
    )
    fun observeOverviewStats(
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<OverviewStatsRow>

    @Query(
        """
        SELECT
            CAST(strftime('%H', played_at / 1000, 'unixepoch', 'localtime') AS INTEGER) AS hour,
            COUNT(*) AS plays,
            COALESCE(SUM(ms_played), 0) AS listeningMs
        FROM play_events
        WHERE played_at >= :fromInclusive
          AND played_at <= :toInclusive
        GROUP BY hour
        ORDER BY hour ASC
        """,
    )
    fun observeHourlyListening(
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<List<HourlyListeningRow>>

    @Query(
        """
        SELECT
            CAST(strftime('%w', played_at / 1000, 'unixepoch', 'localtime') AS INTEGER) AS weekday,
            CAST(strftime('%H', played_at / 1000, 'unixepoch', 'localtime') AS INTEGER) AS hour,
            COUNT(*) AS plays,
            COALESCE(SUM(ms_played), 0) AS listeningMs
        FROM play_events
        WHERE played_at >= :fromInclusive
          AND played_at <= :toInclusive
        GROUP BY weekday, hour
        ORDER BY weekday ASC, hour ASC
        """,
    )
    fun observeListeningHeatmap(
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<List<ListeningHeatmapRow>>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN skipped = 1 THEN 1 ELSE 0 END), 0) AS skippedEvents,
            COALESCE(SUM(CASE WHEN skipped IS NOT NULL THEN 1 ELSE 0 END), 0) AS skipKnownEvents,
            COALESCE(SUM(CASE WHEN shuffle = 1 THEN 1 ELSE 0 END), 0) AS shuffleEvents,
            COALESCE(SUM(CASE WHEN shuffle IS NOT NULL THEN 1 ELSE 0 END), 0) AS shuffleKnownEvents,
            COALESCE(SUM(CASE WHEN offline = 1 THEN 1 ELSE 0 END), 0) AS offlineEvents,
            COALESCE(SUM(CASE WHEN offline IS NOT NULL THEN 1 ELSE 0 END), 0) AS offlineKnownEvents
        FROM play_events
        WHERE played_at >= :fromInclusive
          AND played_at <= :toInclusive
        """,
    )
    fun observePlaybackBehavior(
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<PlaybackBehaviorRow>

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
            COALESCE(SUM(CASE WHEN pe.skipped IS NOT NULL THEN 1 ELSE 0 END), 0) AS skipKnownPlays,
            COALESCE(SUM(CASE WHEN pe.ms_played >= 30000 THEN 1 ELSE 0 END), 0) AS meaningfulPlays,
            AVG(CASE WHEN t.duration_ms > 0 THEN MIN(pe.ms_played * 1.0 / t.duration_ms, 1.0) END) AS averageCompletion,
            COALESCE(SUM(CASE WHEN t.duration_ms > 0 AND pe.ms_played >= t.duration_ms * 0.9 THEN 1 ELSE 0 END), 0) AS completedPlays,
            (SELECT CAST(strftime('%H', h.played_at / 1000, 'unixepoch', 'localtime') AS INTEGER)
                FROM play_events h WHERE h.track_id = t.id GROUP BY strftime('%H', h.played_at / 1000, 'unixepoch', 'localtime')
                ORDER BY SUM(h.ms_played) DESC, COUNT(*) DESC, strftime('%H', h.played_at / 1000, 'unixepoch', 'localtime') LIMIT 1) AS favouriteHour
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
            CAST(strftime('%Y', played_at / 1000, 'unixepoch', 'localtime') AS INTEGER) AS year,
            COUNT(*) AS plays,
            COALESCE(SUM(ms_played), 0) AS listeningMs
        FROM play_events
        WHERE track_id = :trackId
        GROUP BY year
        ORDER BY year ASC
        """,
    )
    fun observeTrackListeningByYear(trackId: Long): Flow<List<YearlyListeningRow>>

    @Query("SELECT DISTINCT date(played_at / 1000, 'unixepoch', 'localtime') FROM play_events WHERE track_id = :trackId ORDER BY 1")
    fun observeTrackDays(trackId: Long): Flow<List<String>>

    @Query(
        """
        SELECT
            a.id AS id,
            a.name AS name,
            COUNT(pe.id) AS totalPlays,
            COALESCE(SUM(pe.ms_played), 0) AS totalListeningMs,
            COUNT(DISTINCT CASE WHEN pe.id IS NOT NULL THEN ta.track_id END) AS uniqueTracks,
            (SELECT 1 + COUNT(*) FROM (
                SELECT ranked.artist_id, COUNT(*) AS plays FROM track_artists ranked
                JOIN play_events ranked_pe ON ranked_pe.track_id = ranked.track_id GROUP BY ranked.artist_id
            ) ranks WHERE ranks.plays > (SELECT COUNT(*) FROM track_artists mine JOIN play_events mine_pe ON mine_pe.track_id = mine.track_id WHERE mine.artist_id = :artistId)) AS allTimeRank,
            (SELECT CAST(strftime('%Y', yearly.played_at / 1000, 'unixepoch', 'localtime') AS INTEGER)
                FROM play_events yearly JOIN track_artists yearly_ta ON yearly_ta.track_id = yearly.track_id WHERE yearly_ta.artist_id = :artistId
                GROUP BY strftime('%Y', yearly.played_at / 1000, 'unixepoch', 'localtime') ORDER BY COUNT(*) DESC, strftime('%Y', yearly.played_at / 1000, 'unixepoch', 'localtime') DESC LIMIT 1) AS mostActiveYear,
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

    @Query(
        """
        SELECT
            mine.year AS year,
            (
                1 + (
                    SELECT COUNT(*)
                    FROM (
                        SELECT other_ta.artist_id, COUNT(*) AS otherPlays
                        FROM play_events other_pe
                        INNER JOIN track_artists other_ta
                            ON other_ta.track_id = other_pe.track_id
                        WHERE CAST(
                            strftime(
                                '%Y',
                                other_pe.played_at / 1000,
                                'unixepoch',
                                'localtime'
                            ) AS INTEGER
                        ) = mine.year
                        GROUP BY other_ta.artist_id
                        HAVING COUNT(*) > mine.plays
                    )
                )
            ) AS artistRank,
            mine.plays AS plays,
            mine.listeningMs AS listeningMs
        FROM (
            SELECT
                CAST(
                    strftime(
                        '%Y',
                        pe.played_at / 1000,
                        'unixepoch',
                        'localtime'
                    ) AS INTEGER
                ) AS year,
                COUNT(*) AS plays,
                COALESCE(SUM(pe.ms_played), 0) AS listeningMs
            FROM play_events pe
            INNER JOIN track_artists ta ON ta.track_id = pe.track_id
            WHERE ta.artist_id = :artistId
            GROUP BY year
        ) mine
        ORDER BY mine.year ASC
        """,
    )
    fun observeArtistYearRanks(
        artistId: Long,
    ): Flow<List<ArtistYearRankRow>>

    @Query(
        """
        SELECT
            (
                1 + (
                    SELECT COUNT(*)
                    FROM (
                        SELECT other_ta.artist_id, COUNT(*) AS otherPlays
                        FROM play_events other_pe
                        INNER JOIN track_artists other_ta
                            ON other_ta.track_id = other_pe.track_id
                        WHERE other_pe.played_at >= :fromInclusive
                          AND other_pe.played_at <= :toInclusive
                        GROUP BY other_ta.artist_id
                        HAVING COUNT(*) > (
                            SELECT COUNT(*)
                            FROM play_events mine_pe
                            INNER JOIN track_artists mine_ta
                                ON mine_ta.track_id = mine_pe.track_id
                            WHERE mine_ta.artist_id = :artistId
                              AND mine_pe.played_at >= :fromInclusive
                              AND mine_pe.played_at <= :toInclusive
                        )
                    )
                )
            ) AS artistRank,
            COUNT(pe.id) AS plays,
            COALESCE(SUM(pe.ms_played), 0) AS listeningMs,
            COUNT(DISTINCT pe.track_id) AS uniqueTracks
        FROM play_events pe
        INNER JOIN track_artists ta ON ta.track_id = pe.track_id
        WHERE ta.artist_id = :artistId
          AND pe.played_at >= :fromInclusive
          AND pe.played_at <= :toInclusive
        GROUP BY ta.artist_id
        HAVING COUNT(pe.id) > 0
        """,
    )
    fun observeArtistRangeRank(
        artistId: Long,
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<ArtistRangeRankRow?>

    @Query(
        """
        SELECT
            al.id AS id,
            al.name AS name,
            (
                SELECT a.name
                FROM tracks candidate
                INNER JOIN track_artists ta ON ta.track_id = candidate.id
                INNER JOIN artists a ON a.id = ta.artist_id
                WHERE candidate.album_id = al.id
                ORDER BY ta.position ASC, a.name COLLATE NOCASE ASC
                LIMIT 1
            ) AS artistName,
            COUNT(pe.id) AS totalPlays,
            COALESCE(SUM(pe.ms_played), 0) AS totalListeningMs,
            COUNT(DISTINCT CASE WHEN pe.id IS NOT NULL THEN t.id END) AS uniqueTracks,
            COUNT(
                DISTINCT CASE
                    WHEN pe.id IS NOT NULL
                    THEN date(pe.played_at / 1000, 'unixepoch', 'localtime')
                END
            ) AS activeDays,
            COALESCE(
                SUM(CASE WHEN pe.ms_played >= 30000 THEN 1 ELSE 0 END),
                0
            ) AS meaningfulPlays,
            (SELECT strftime('%Y-%m', monthly.played_at / 1000, 'unixepoch', 'localtime') FROM play_events monthly
                JOIN tracks month_tracks ON month_tracks.id = monthly.track_id WHERE month_tracks.album_id = :albumId
                GROUP BY strftime('%Y-%m', monthly.played_at / 1000, 'unixepoch', 'localtime') ORDER BY COUNT(*) DESC, strftime('%Y-%m', monthly.played_at / 1000, 'unixepoch', 'localtime') DESC LIMIT 1) AS peakMonth,
            MIN(pe.played_at) AS firstPlayedAtEpochMs,
            MAX(pe.played_at) AS lastPlayedAtEpochMs
        FROM albums al
        LEFT JOIN tracks t ON t.album_id = al.id
        LEFT JOIN play_events pe ON pe.track_id = t.id
        WHERE al.id = :albumId
        GROUP BY al.id, al.name
        """,
    )
    fun observeAlbumDetail(albumId: Long): Flow<AlbumDetailRow?>

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
        FROM tracks t
        INNER JOIN play_events pe ON pe.track_id = t.id
        WHERE t.album_id = :albumId
        GROUP BY t.id, t.name
        ORDER BY plays DESC, listeningMs DESC, t.name COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    fun observeAlbumTopTracks(
        albumId: Long,
        limit: Int,
    ): Flow<List<TrackRankingRow>>

    @Query(
        """
        SELECT
            CAST(
                strftime(
                    '%Y',
                    pe.played_at / 1000,
                    'unixepoch',
                    'localtime'
                ) AS INTEGER
            ) AS year,
            COUNT(*) AS plays,
            COALESCE(SUM(pe.ms_played), 0) AS listeningMs
        FROM play_events pe
        INNER JOIN tracks t ON t.id = pe.track_id
        WHERE t.album_id = :albumId
        GROUP BY year
        ORDER BY year ASC
        """,
    )
    fun observeAlbumListeningByYear(
        albumId: Long,
    ): Flow<List<YearlyListeningRow>>

    @Query(
        """
        SELECT
            pe.id AS id,
            t.id AS trackId,
            t.name AS trackName,
            (
                SELECT a.name
                FROM track_artists ta
                INNER JOIN artists a ON a.id = ta.artist_id
                WHERE ta.track_id = t.id
                ORDER BY ta.position ASC
                LIMIT 1
            ) AS artistName,
            al.name AS albumName,
            pe.played_at AS playedAtEpochMs,
            pe.ms_played AS listeningMs,
            pe.skipped AS skipped
        FROM play_events pe
        INNER JOIN tracks t ON t.id = pe.track_id
        LEFT JOIN albums al ON al.id = t.album_id
        WHERE pe.played_at >= :fromInclusive
          AND pe.played_at <= :toInclusive
        ORDER BY pe.played_at DESC, pe.id DESC
        LIMIT :limit
        """,
    )
    fun observeListeningHistory(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<ListeningHistoryRow>>

    @Query(
        """
        SELECT
            pe.id AS id,
            t.id AS trackId,
            t.name AS trackName,
            (
                SELECT a.name
                FROM track_artists ta
                INNER JOIN artists a ON a.id = ta.artist_id
                WHERE ta.track_id = t.id
                ORDER BY ta.position ASC
                LIMIT 1
            ) AS artistName,
            al.name AS albumName,
            pe.played_at AS playedAtEpochMs,
            pe.ms_played AS listeningMs,
            pe.skipped AS skipped
        FROM play_events pe
        INNER JOIN tracks t ON t.id = pe.track_id
        LEFT JOIN albums al ON al.id = t.album_id
        WHERE pe.played_at >= :fromInclusive
          AND pe.played_at <= :toInclusive
          AND (
              :cursorPlayedAt IS NULL
              OR pe.played_at < :cursorPlayedAt
              OR (
                  pe.played_at = :cursorPlayedAt
                  AND pe.id < :cursorEventId
              )
          )
        ORDER BY pe.played_at DESC, pe.id DESC
        LIMIT :limit
        """,
    )
    suspend fun loadListeningHistoryPage(
        fromInclusive: Long,
        toInclusive: Long,
        cursorPlayedAt: Long?,
        cursorEventId: Long?,
        limit: Int,
    ): List<ListeningHistoryRow>
}
