package com.davidegea.spotifystats.ui.albumdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.AlbumDetail
import com.davidegea.spotifystats.domain.usecase.ObserveAlbumDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface AlbumDetailUiState {
    data object Loading : AlbumDetailUiState
    data object NotFound : AlbumDetailUiState
    data class Content(val detail: AlbumDetail) : AlbumDetailUiState
}

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeAlbumDetail: ObserveAlbumDetailUseCase,
) : ViewModel() {

    private val albumId = savedStateHandle.get<String>("albumId")?.toLongOrNull()

    private val stateFlow: Flow<AlbumDetailUiState> =
        if (albumId == null) {
            flowOf(AlbumDetailUiState.NotFound)
        } else {
            observeAlbumDetail(albumId).map { detail ->
                if (detail == null) {
                    AlbumDetailUiState.NotFound
                } else {
                    AlbumDetailUiState.Content(detail)
                }
            }
        }

    val uiState = stateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlbumDetailUiState.Loading,
    )
}
