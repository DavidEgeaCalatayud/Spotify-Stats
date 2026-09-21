package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.ArtistRangeRank
import com.davidegea.spotifystats.domain.model.TimeRange
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveArtistRangeRankUseCase(
    private val repository: ListeningHistoryRepository,
) {
    operator fun invoke(
        artistId: Long,
        range: TimeRange,
    ): Flow<ArtistRangeRank?> =
        repository.observeArtistRangeRank(
            artistId = artistId,
            fromInclusive = range.fromInclusive,
            toInclusive = range.toInclusive,
        )
}
