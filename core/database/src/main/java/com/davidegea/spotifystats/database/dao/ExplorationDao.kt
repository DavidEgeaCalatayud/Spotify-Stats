package com.davidegea.spotifystats.database.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class QualityRow(val events: Long, val meaningful: Long, val durationKnown: Long, val completed: Long, val averageCompletion: Double?)
data class DailyRow(val date: String, val plays: Long, val listeningMs: Long)
data class DiscoveryRow(val artistId: Long, val name: String, val firstPlayedAt: Long, val plays: Long)
data class RediscoveryRow(val trackId: Long, val name: String, val gapDays: Long)
data class ObsessionRow(val artistId: Long, val name: String, val plays: Long, val previousPlays: Long)
data class ForgottenRow(val trackId: Long, val name: String, val previousPlays: Long)
data class SearchRow(val kind: String, val id: Long, val name: String, val subtitle: String?, val plays: Long)

@Dao
interface ExplorationDao {
    @Query("""
        SELECT COUNT(*) AS events,
            COALESCE(SUM(CASE WHEN pe.ms_played >= 30000 THEN 1 ELSE 0 END), 0) AS meaningful,
            COALESCE(SUM(CASE WHEN t.duration_ms > 0 THEN 1 ELSE 0 END), 0) AS durationKnown,
            COALESCE(SUM(CASE WHEN t.duration_ms > 0 AND pe.ms_played >= t.duration_ms * 0.9 THEN 1 ELSE 0 END), 0) AS completed,
            AVG(CASE WHEN t.duration_ms > 0 THEN MIN(pe.ms_played * 1.0 / t.duration_ms, 1.0) END) AS averageCompletion
        FROM play_events pe JOIN tracks t ON t.id = pe.track_id
        WHERE pe.played_at BETWEEN :from AND :to
    """)
    suspend fun quality(from: Long, to: Long): QualityRow

    @Query("""
        SELECT played_at - ms_played AS started, played_at AS ended, ms_played
        FROM play_events WHERE played_at BETWEEN :from AND :to AND ms_played > 0
        ORDER BY started, ended, id
    """)
    fun sessionIntervals(from: Long, to: Long): Cursor

    @Query("""
        SELECT date(played_at / 1000, 'unixepoch', 'localtime') AS date,
            COUNT(*) AS plays, SUM(ms_played) AS listeningMs
        FROM play_events WHERE played_at BETWEEN :from AND :to
        GROUP BY date ORDER BY date
    """)
    fun observeDaily(from: Long, to: Long): Flow<List<DailyRow>>

    @Query("""
        SELECT date(played_at / 1000, 'unixepoch', 'localtime') AS date,
            COUNT(*) AS plays, SUM(ms_played) AS listeningMs
        FROM play_events WHERE played_at BETWEEN :from AND :to GROUP BY date ORDER BY date
    """)
    suspend fun daily(from: Long, to: Long): List<DailyRow>

    @Query("""
        SELECT a.id AS artistId, a.name, MIN(pe.played_at) AS firstPlayedAt, COUNT(*) AS plays
        FROM artists a JOIN track_artists ta ON ta.artist_id = a.id
        JOIN play_events pe ON pe.track_id = ta.track_id
        WHERE pe.played_at <= :to
        GROUP BY a.id HAVING MIN(pe.played_at) >= :from
        ORDER BY plays DESC, a.name, a.id LIMIT :limit
    """)
    suspend fun discoveries(from: Long, to: Long, limit: Int): List<DiscoveryRow>

    @Query("""
        SELECT COUNT(*) FROM (
            SELECT ta.artist_id FROM track_artists ta JOIN play_events pe ON pe.track_id = ta.track_id
            WHERE pe.played_at <= :to GROUP BY ta.artist_id HAVING MIN(pe.played_at) >= :from
        )
    """)
    suspend fun discoveryCount(from: Long, to: Long): Long

    @Query("""
        SELECT t.id AS trackId, t.name,
            (MIN(pe.played_at) - (SELECT MAX(old.played_at) FROM play_events old
                WHERE old.track_id = t.id AND old.played_at < :from)) / 86400000 AS gapDays
        FROM play_events pe JOIN tracks t ON t.id = pe.track_id
        WHERE pe.played_at BETWEEN :from AND :to
        GROUP BY t.id HAVING gapDays >= 90
        ORDER BY gapDays DESC, t.id LIMIT 10
    """)
    suspend fun rediscoveries(from: Long, to: Long): List<RediscoveryRow>

    @Query("""
        SELECT a.id AS artistId, a.name,
            SUM(CASE WHEN pe.played_at >= :from THEN 1 ELSE 0 END) AS plays,
            SUM(CASE WHEN pe.played_at < :from THEN 1 ELSE 0 END) AS previousPlays
        FROM artists a JOIN track_artists ta ON ta.artist_id = a.id
        JOIN play_events pe ON pe.track_id = ta.track_id
        WHERE pe.played_at BETWEEN :previousFrom AND :to
        GROUP BY a.id HAVING plays >= 10 AND previousPlays > 0 AND plays >= previousPlays * 2
        ORDER BY plays DESC, a.id LIMIT 10
    """)
    suspend fun obsessions(from: Long, to: Long, previousFrom: Long): List<ObsessionRow>

    @Query("""
        SELECT t.id AS trackId, t.name, COUNT(*) AS previousPlays
        FROM tracks t JOIN play_events pe ON pe.track_id = t.id
        WHERE pe.played_at BETWEEN :previousFrom AND :previousTo
            AND NOT EXISTS (SELECT 1 FROM play_events recent WHERE recent.track_id = t.id
                AND recent.played_at BETWEEN :from AND :to)
        GROUP BY t.id HAVING COUNT(*) >= 10 ORDER BY previousPlays DESC, t.id LIMIT 10
    """)
    suspend fun forgotten(from: Long, to: Long, previousFrom: Long, previousTo: Long): List<ForgottenRow>

    @Query("SELECT COALESCE(SUM(ms_played), 0) FROM play_events WHERE played_at BETWEEN :from AND :to")
    suspend fun listeningMs(from: Long, to: Long): Long

    @Query("""
        SELECT 'track' AS kind, t.id, t.name,
            (SELECT a.name FROM artists a JOIN track_artists ta ON ta.artist_id = a.id
                WHERE ta.track_id = t.id ORDER BY ta.position, a.id LIMIT 1) AS subtitle,
            COUNT(pe.id) AS plays
        FROM tracks t JOIN play_events pe ON pe.track_id = t.id
        WHERE pe.played_at BETWEEN :from AND :to AND (
            t.id IN (SELECT rowid FROM tracks_fts WHERE tracks_fts MATCH :query)
            OR t.id IN (SELECT ta.track_id FROM track_artists ta WHERE ta.artist_id IN
                (SELECT rowid FROM artists_fts WHERE artists_fts MATCH :query)))
        GROUP BY t.id
        UNION ALL
        SELECT 'artist' AS kind, a.id, a.name, NULL AS subtitle, COUNT(pe.id) AS plays
        FROM artists a JOIN track_artists ta ON ta.artist_id = a.id JOIN play_events pe ON pe.track_id = ta.track_id
        WHERE a.id IN (SELECT rowid FROM artists_fts WHERE artists_fts MATCH :query)
            AND pe.played_at BETWEEN :from AND :to GROUP BY a.id
        UNION ALL
        SELECT 'album' AS kind, al.id, al.name, NULL AS subtitle, COUNT(pe.id) AS plays
        FROM albums al JOIN tracks t ON t.album_id = al.id JOIN play_events pe ON pe.track_id = t.id
        WHERE pe.played_at BETWEEN :from AND :to AND (
            al.id IN (SELECT rowid FROM albums_fts WHERE albums_fts MATCH :query)
            OR t.id IN (SELECT ta.track_id FROM track_artists ta WHERE ta.artist_id IN
                (SELECT rowid FROM artists_fts WHERE artists_fts MATCH :query)))
        GROUP BY al.id ORDER BY plays DESC, name, kind, id LIMIT 100
    """)
    fun search(query: String, from: Long, to: Long): Flow<List<SearchRow>>
}
