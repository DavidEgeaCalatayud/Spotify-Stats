package com.davidegea.spotifystats.data.history

import com.davidegea.spotifystats.database.dao.ListeningHistoryDao
import com.davidegea.spotifystats.domain.model.AlbumDetail
import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.ArtistDetail
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.ListeningHistoryItem
import com.davidegea.spotifystats.domain.model.OverviewStats
import com.davidegea.spotifystats.domain.model.TrackDetail
import com.davidegea.spotifystats.domain.model.TrackRanking
import com.davidegea.spotifystats.domain.model.YearlyListening
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomListeningHistoryRepository(
    private val dao: ListeningHistoryDao,
) : ListeningHistoryRepository {

    override fun observeOverviewStats(): Flow<OverviewStats> =
        dao.observeOverviewStats().map { row ->
            OverviewStats(
                totalPlays = row.totalPlays,
                totalListeningMs = row.totalListeningMs,
                uniqueTracks = row.uniqueTracks,
                uniqueArtists = row.uniqueArtists,
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
        dao.observeTrackDetail(trackId).map { row ->
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
                )
            }
        }

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
            rows.map { row ->
                ListeningHistoryItem(
                    eventId = row.id,
                    trackId = row.trackId,
                    trackName = row.trackName,
                    artistName = row.artistName,
                    albumName = row.albumName,
                    playedAtEpochMs = row.playedAtEpochMs,
                    listeningMs = row.listeningMs,
                    skipped = row.skipped,
                )
            }
        }

    private fun com.davidegea.spotifystats.database.dao.TrackRankingRow.toDomain(): TrackRanking =
        TrackRanking(
            id = id,
            name = name,
            artistName = artistName,
            plays = plays,
            listeningMs = listeningMs,
        )
}
