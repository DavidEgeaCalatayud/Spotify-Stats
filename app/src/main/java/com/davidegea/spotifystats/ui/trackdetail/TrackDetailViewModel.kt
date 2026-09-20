package com.davidegea.spotifystats.ui.trackdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.TrackDetail
import com.davidegea.spotifystats.domain.usecase.ObserveTrackDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface TrackDetailUiState {
    data object Loading : TrackDetailUiState
    data object NotFound : TrackDetailUiState
    data class Content(val detail: TrackDetail) : TrackDetailUiState
}

@HiltViewModel
class TrackDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTrackDetail: ObserveTrackDetailUseCase,
) : ViewModel() {

    private val trackId = savedStateHandle.get<String>("trackId")?.toLongOrNull()

    private val stateFlow: Flow<TrackDetailUiState> =
        if (trackId == null) {
            flowOf(TrackDetailUiState.NotFound)
        } else {
            observeTrackDetail(trackId).map { detail ->
                if (detail == null) {
                    TrackDetailUiState.NotFound
                } else {
                    TrackDetailUiState.Content(detail)
                }
            }
        }

    val uiState = stateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrackDetailUiState.Loading,
    )
}
