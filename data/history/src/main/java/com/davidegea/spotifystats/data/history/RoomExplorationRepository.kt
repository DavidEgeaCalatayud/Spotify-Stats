package com.davidegea.spotifystats.data.history

import androidx.room.withTransaction
import com.davidegea.spotifystats.database.SpotifyStatsDatabase
import com.davidegea.spotifystats.domain.analytics.DateRanges
import com.davidegea.spotifystats.domain.analytics.SessionAccumulator
import com.davidegea.spotifystats.domain.model.*
import com.davidegea.spotifystats.domain.repository.ExplorationRepository
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*

class RoomExplorationRepository(
    private val database: SpotifyStatsDatabase,
    private val history: ListeningHistoryRepository,
) : ExplorationRepository {
    private val dao = database.explorationDao()

    override fun observeAnalytics(range: TimeRange): Flow<AdvancedAnalytics> = changes().mapLatest {
        database.withTransaction {
            val quality = dao.quality(range.fromInclusive, range.toInclusive)
            val accumulator = SessionAccumulator()
            dao.sessionIntervals(range.fromInclusive, range.toInclusive).use { cursor ->
                while (cursor.moveToNext()) {
                    currentCoroutineContext().ensureActive()
                    accumulator.add(cursor.getLong(0), cursor.getLong(1), cursor.getLong(2))
                }
            }
            val previous = DateRanges.previous(range)
            AdvancedAnalytics(
                quality = ListeningQuality(quality.events, quality.meaningful, quality.durationKnown, quality.completed, quality.averageCompletion),
                sessions = accumulator.result(),
                discoveries = dao.discoveries(range.fromInclusive, range.toInclusive, 10).map {
                    Discovery(it.artistId, it.name, it.firstPlayedAt, it.plays)
                },
                rediscoveries = dao.rediscoveries(range.fromInclusive, range.toInclusive).map {
                    Rediscovery(it.trackId, it.name, it.gapDays)
                },
                obsessions = if (previous == null) emptyList() else dao.obsessions(range.fromInclusive, range.toInclusive, previous.fromInclusive).map {
                    Obsession(it.artistId, it.name, it.plays, it.previousPlays)
                },
                forgotten = if (previous == null) emptyList() else dao.forgotten(range.fromInclusive, range.toInclusive, previous.fromInclusive, previous.toInclusive).map {
                    ForgottenTrack(it.trackId, it.name, it.previousPlays)
                },
                trend = previous?.let { ListeningTrend(dao.listeningMs(range.fromInclusive, range.toInclusive), dao.listeningMs(it.fromInclusive, it.toInclusive)) },
                daily = dao.daily(range.fromInclusive, range.toInclusive).map { DailyListening(it.date, it.plays, it.listeningMs) },
            )
        }
    }.flowOn(Dispatchers.IO)

    override fun observeDaily(range: TimeRange): Flow<List<DailyListening>> =
        dao.observeDaily(range.fromInclusive, range.toInclusive).map { rows -> rows.map { DailyListening(it.date, it.plays, it.listeningMs) } }

    override fun search(query: String, range: TimeRange): Flow<List<SearchResult>> {
        val terms = SearchQuery.compile(query) ?: return flowOf(emptyList())
        return dao.search(terms, range.fromInclusive, range.toInclusive).map { rows ->
            rows.map { SearchResult(it.kind, it.id, it.name, it.subtitle, it.plays) }
        }
    }

    override fun observeRecap(range: TimeRange): Flow<Recap> = changes().mapLatest {
        database.withTransaction {
            Recap(
                range,
                history.observeOverviewStats(range.fromInclusive, range.toInclusive).first(),
                history.observeTopTracks(range.fromInclusive, range.toInclusive, 5).first(),
                history.observeTopArtists(range.fromInclusive, range.toInclusive, 5).first(),
                history.observeTopAlbums(range.fromInclusive, range.toInclusive, 5).first(),
                dao.discoveryCount(range.fromInclusive, range.toInclusive),
            )
        }
    }.flowOn(Dispatchers.IO)

    private fun changes() = database.invalidationTracker.createFlow("play_events", "tracks", "track_artists", "artists", "albums")
        .debounce(250)
        .conflate()
}

object SearchQuery {
    /** Literal Unicode prefix terms only. Never pass user-entered FTS operators into MATCH. */
    fun compile(value: String): String? = Regex("[\\p{L}\\p{N}]+")
        .findAll(value.take(200)).take(8).map { "\"${it.value}\"*" }.toList()
        .takeIf { it.isNotEmpty() }?.joinToString(" AND ")
}
