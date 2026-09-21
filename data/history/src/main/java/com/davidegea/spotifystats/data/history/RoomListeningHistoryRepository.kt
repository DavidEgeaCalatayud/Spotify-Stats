package com.davidegea.spotifystats.data.history

import com.davidegea.spotifystats.database.dao.ListeningHistoryDao
import com.davidegea.spotifystats.domain.model.AlbumDetail
import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.ArtistDetail
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.HourlyListening
import com.davidegea.spotifystats.domain.model.ListeningHeatmapCell
import com.davidegea.spotifystats.domain.model.ListeningHistoryCursor
import com.davidegea.spotifystats.domain.model.ListeningHistoryItem
import com.davidegea.spotifystats.domain.model.ListeningHistoryPage
import com.davidegea.spotifystats.domain.model.OverviewStats
import com.davidegea.spotifystats.domain.model.PlaybackBehaviorStats
import com.davidegea.spotifystats.domain.model.TrackDetail
import com.davidegea.spotifystats.domain.model.TrackRanking
import com.davidegea.spotifystats.domain.model.YearlyListening
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.combine
import com.davidegea.spotifystats.domain.analytics.DateRanges
import java.util.TimeZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomListeningHistoryRepository(
    private val dao: ListeningHistoryDao,
) : ListeningHistoryRepository {

    override fun observeOverviewStats(): Flow<OverviewStats> =
        observeOverviewStats(
            fromInclusive = Long.MIN_VALUE,
            toInclusive = Long.MAX_VALUE,
        )

    override fun observeOverviewStats(
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<OverviewStats> =
        dao.observeOverviewStats(fromInclusive, toInclusive).map { row ->
            OverviewStats(
                totalPlays = row.totalPlays,
                totalListeningMs = row.totalListeningMs,
                uniqueTracks = row.uniqueTracks,
                uniqueArtists = row.uniqueArtists,
            )
        }

    override fun observeHourlyListening(
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<List<HourlyListening>> =
        dao.observeHourlyListening(fromInclusive, toInclusive).map { rows ->
            rows.map { row ->
                HourlyListening(
                    hour = row.hour,
                    plays = row.plays,
                    listeningMs = row.listeningMs,
                )
            }
        }

    override fun observeListeningHeatmap(
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<List<ListeningHeatmapCell>> =
        dao.observeListeningHeatmap(fromInclusive, toInclusive).map { rows ->
            rows.map { row ->
                ListeningHeatmapCell(
                    weekday = row.weekday,
                    hour = row.hour,
                    plays = row.plays,
                    listeningMs = row.listeningMs,
                )
            }
        }

    override fun observePlaybackBehavior(
        fromInclusive: Long,
        toInclusive: Long,
    ): Flow<PlaybackBehaviorStats> =
        dao.observePlaybackBehavior(fromInclusive, toInclusive).map { row ->
            PlaybackBehaviorStats(
                skippedEvents = row.skippedEvents,
                skipKnownEvents = row.skipKnownEvents,
                shuffleEvents = row.shuffleEvents,
                shuffleKnownEvents = row.shuffleKnownEvents,
                offlineEvents = row.offlineEvents,
                offlineKnownEvents = row.offlineKnownEvents,
            )
        }

    override fun observeTopTracks(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<TrackRanking>> =
        dao.observeTopTracks(fromInclusive, toInclusive, limit).map { rows ->
            rows.map { row -> row.toDomain() }
        }

    override fun observeTopArtists(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<ArtistRanking>> =
        dao.observeTopArtists(fromInclusive, toInclusive, limit).map { rows ->
            rows.map { row ->
                ArtistRanking(
                    id = row.id,
                    name = row.name,
                    plays = row.plays,
                    listeningMs = row.listeningMs,
                )
            }
        }

    override fun observeTopAlbums(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<AlbumRanking>> =
        dao.observeTopAlbums(fromInclusive, toInclusive, limit).map { rows ->
            rows.map { row ->
                AlbumRanking(
                    id = row.id,
                    name = row.name,
                    artistName = row.artistName,
                    plays = row.plays,
                    listeningMs = row.listeningMs,
                )
            }
        }

    override fun observeTrackDetail(trackId: Long): Flow<TrackDetail?> =
        combine(dao.observeTrackDetail(trackId), dao.observeTrackDays(trackId)) { row, days ->
            row?.let {
                TrackDetail(
                    id = it.id,
                    name = it.name,
                    artistName = it.artistName,
                    totalPlays = it.totalPlays,
                    totalListeningMs = it.totalListeningMs,
                    firstPlayedAtEpochMs = it.firstPlayedAtEpochMs,
                    lastPlayedAtEpochMs = it.lastPlayedAtEpochMs,
                    skippedPlays = it.skippedPlays,
                    skipKnownPlays = it.skipKnownPlays,
                    meaningfulPlays = it.meaningfulPlays,
                    averageCompletion = it.averageCompletion,
                    completedPlays = it.completedPlays,
                    favouriteHour = it.favouriteHour,
                    longestStreakDays = longestStreak(days),
                )
            }
        }.flowOn(Dispatchers.Default)

    override fun observeTrackListeningByYear(trackId: Long): Flow<List<YearlyListening>> =
        dao.observeTrackListeningByYear(trackId).map { rows ->
            rows.map { row ->
                YearlyListening(
                    year = row.year,
                    plays = row.plays,
                    listeningMs = row.listeningMs,
                )
            }
        }

    override fun observeArtistDetail(artistId: Long): Flow<ArtistDetail?> =
        dao.observeArtistDetail(artistId).map { row ->
            row?.let {
                ArtistDetail(
                    id = it.id,
                    name = it.name,
                    allTimeRank = it.allTimeRank, mostActiveYear = it.mostActiveYear,
                    totalPlays = it.totalPlays,
                    totalListeningMs = it.totalListeningMs,
                    uniqueTracks = it.uniqueTracks,
                    firstPlayedAtEpochMs = it.firstPlayedAtEpochMs,
                    lastPlayedAtEpochMs = it.lastPlayedAtEpochMs,
                )
            }
        }

    override fun observeArtistTopTracks(
        artistId: Long,
        limit: Int,
    ): Flow<List<TrackRanking>> =
        dao.observeArtistTopTracks(artistId, limit).map { rows ->
            rows.map { row -> row.toDomain() }
        }

    override fun observeAlbumDetail(albumId: Long): Flow<AlbumDetail?> =
        dao.observeAlbumDetail(albumId).map { row ->
            row?.let {
                AlbumDetail(
                    id = it.id,
                    name = it.name,
                    peakMonth = it.peakMonth,
                    artistName = it.artistName,
                    totalPlays = it.totalPlays,
                    totalListeningMs = it.totalListeningMs,
                    uniqueTracks = it.uniqueTracks,
                    firstPlayedAtEpochMs = it.firstPlayedAtEpochMs,
                    lastPlayedAtEpochMs = it.lastPlayedAtEpochMs,
                )
            }
        }

    override fun observeAlbumTopTracks(
        albumId: Long,
        limit: Int,
    ): Flow<List<TrackRanking>> =
        dao.observeAlbumTopTracks(albumId, limit).map { rows ->
            rows.map { row -> row.toDomain() }
        }

    override fun observeListeningHistory(
        fromInclusive: Long,
        toInclusive: Long,
        limit: Int,
    ): Flow<List<ListeningHistoryItem>> =
        dao.observeListeningHistory(fromInclusive, toInclusive, limit).map { rows ->
            rows.map { row -> row.toHistoryDomain() }
        }

    override suspend fun loadListeningHistoryPage(
        fromInclusive: Long,
        toInclusive: Long,
        cursor: ListeningHistoryCursor?,
        pageSize: Int,
    ): ListeningHistoryPage {
        require(pageSize in 1..500) { "pageSize must be between 1 and 500" }
        val rows = dao.loadListeningHistoryPage(
            fromInclusive = fromInclusive,
            toInclusive = toInclusive,
            cursorPlayedAt = cursor?.playedAtEpochMs,
            cursorEventId = cursor?.eventId,
            limit = pageSize + 1,
        )
        val visible = rows.take(pageSize)
        val items = visible.map { row -> row.toHistoryDomain() }
        val hasMore = rows.size > pageSize
        val nextCursor = if (hasMore) {
            visible.lastOrNull()?.let { row ->
                ListeningHistoryCursor(
                    playedAtEpochMs = row.playedAtEpochMs,
                    eventId = row.id,
                )
            }
        } else {
            null
        }
        return ListeningHistoryPage(
            items = items,
            nextCursor = nextCursor,
            hasMore = hasMore,
        )
    }

    private fun com.davidegea.spotifystats.database.dao.ListeningHistoryRow.toHistoryDomain() =
        ListeningHistoryItem(
            eventId = id,
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            albumName = albumName,
            playedAtEpochMs = playedAtEpochMs,
            listeningMs = listeningMs,
            skipped = skipped,
        )

    private fun com.davidegea.spotifystats.database.dao.TrackRankingRow.toDomain(): TrackRanking =
        TrackRanking(
            id = id,
            name = name,
            artistName = artistName,
            plays = plays,
            listeningMs = listeningMs,
        )
}

private fun longestStreak(days: List<String>): Int {
    var previous: Long? = null
    var current = 0
    var longest = 0
    for (date in days) {
        val day = DateRanges.parse(date, TimeZone.getTimeZone("UTC")) / 86_400_000L
        current = if (previous != null && day == previous + 1) current + 1 else 1
        longest = maxOf(longest, current)
        previous = day
    }
    return longest
}
