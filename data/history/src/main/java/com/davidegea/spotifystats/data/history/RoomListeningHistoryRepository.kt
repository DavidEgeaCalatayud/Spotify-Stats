package com.davidegea.spotifystats.data.history

import com.davidegea.spotifystats.database.dao.ListeningHistoryDao
import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.OverviewStats
import com.davidegea.spotifystats.domain.model.TrackRanking
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
            rows.map { row ->
                TrackRanking(
                    id = row.id,
                    name = row.name,
                    artistName = row.artistName,
                    plays = row.plays,
                    listeningMs = row.listeningMs,
                )
            }
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
}
