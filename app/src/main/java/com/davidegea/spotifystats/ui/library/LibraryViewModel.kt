package com.davidegea.spotifystats.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.TrackRanking
import com.davidegea.spotifystats.domain.usecase.ObserveLibraryRankingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class LibraryUiState(
    val period: AnalyticsPeriod = AnalyticsPeriod.LAST_30_DAYS,
    val tracks: List<TrackRanking> = emptyList(),
    val artists: List<ArtistRanking> = emptyList(),
    val albums: List<AlbumRanking> = emptyList(),
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    observeLibraryRankings: ObserveLibraryRankingsUseCase,
) : ViewModel() {

    private val period = MutableStateFlow(AnalyticsPeriod.LAST_30_DAYS)

    val uiState = period
        .flatMapLatest { selected ->
            observeLibraryRankings(selected).map { rankings ->
                LibraryUiState(
                    period = selected,
                    tracks = rankings.tracks,
                    artists = rankings.artists,
                    albums = rankings.albums,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState(),
        )

    fun selectPeriod(period: AnalyticsPeriod) {
        this.period.value = period
    }
}
