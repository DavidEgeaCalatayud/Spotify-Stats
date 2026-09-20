package com.davidegea.spotifystats.ui.artistdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.ArtistDetail
import com.davidegea.spotifystats.domain.usecase.ObserveArtistDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface ArtistDetailUiState {
    data object Loading : ArtistDetailUiState
    data object NotFound : ArtistDetailUiState
    data class Content(val detail: ArtistDetail) : ArtistDetailUiState
}

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeArtistDetail: ObserveArtistDetailUseCase,
) : ViewModel() {

    private val artistId = savedStateHandle.get<String>("artistId")?.toLongOrNull()

    private val stateFlow: Flow<ArtistDetailUiState> =
        if (artistId == null) {
            flowOf(ArtistDetailUiState.NotFound)
        } else {
            observeArtistDetail(artistId).map { detail ->
                if (detail == null) {
                    ArtistDetailUiState.NotFound
                } else {
                    ArtistDetailUiState.Content(detail)
                }
            }
        }

    val uiState = stateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ArtistDetailUiState.Loading,
    )
}
