package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.TrackDetail
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveTrackDetailUseCase(
    private val repository: ListeningHistoryRepository,
) {
    operator fun invoke(trackId: Long): Flow<TrackDetail?> =
        combine(
            repository.observeTrackDetail(trackId),
            repository.observeTrackListeningByYear(trackId),
        ) { detail, yearly ->
            detail?.copy(playsByYear = yearly)
        }
}
