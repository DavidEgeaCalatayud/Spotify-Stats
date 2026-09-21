package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AlbumDetail
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveAlbumDetailUseCase(
    private val repository: ListeningHistoryRepository,
) {
    operator fun invoke(
        albumId: Long,
        topTrackLimit: Int = DEFAULT_TOP_TRACK_LIMIT,
    ): Flow<AlbumDetail?> =
        combine(
            repository.observeAlbumDetail(albumId),
            repository.observeAlbumTopTracks(albumId, topTrackLimit),
            repository.observeAlbumListeningByYear(albumId),
        ) { detail, topTracks, playsByYear ->
            detail?.copy(
                topTracks = topTracks,
                playsByYear = playsByYear,
            )
        }

    private companion object {
        const val DEFAULT_TOP_TRACK_LIMIT = 20
    }
}
