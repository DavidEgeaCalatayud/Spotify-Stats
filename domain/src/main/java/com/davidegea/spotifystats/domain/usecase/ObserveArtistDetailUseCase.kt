package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.ArtistDetail
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveArtistDetailUseCase(
    private val repository: ListeningHistoryRepository,
) {
    operator fun invoke(
        artistId: Long,
        topTrackLimit: Int = DEFAULT_TOP_TRACK_LIMIT,
    ): Flow<ArtistDetail?> =
        combine(
            repository.observeArtistDetail(artistId),
            repository.observeArtistTopTracks(artistId, topTrackLimit),
            repository.observeArtistYearRanks(artistId),
        ) { detail, topTracks, rankByYear ->
            detail?.copy(
                topTracks = topTracks,
                rankByYear = rankByYear,
            )
        }

    private companion object {
        const val DEFAULT_TOP_TRACK_LIMIT = 10
    }
}
