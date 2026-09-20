package com.davidegea.spotifystats.data.history

import com.davidegea.spotifystats.database.dao.ListeningHistoryDao
import com.davidegea.spotifystats.domain.model.OverviewStats
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
}
