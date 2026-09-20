package com.davidegea.spotifystats.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.ListeningHistoryItem
import com.davidegea.spotifystats.domain.model.TrackRanking
import com.davidegea.spotifystats.domain.usecase.ObserveLibraryRankingsUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveListeningHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class LibraryUiState(
    val period: AnalyticsPeriod = AnalyticsPeriod.LAST_30_DAYS,
    val tracks: List<TrackRanking> = emptyList(),
    val artists: List<ArtistRanking> = emptyList(),
    val albums: List<AlbumRanking> = emptyList(),
    val history: List<ListeningHistoryItem> = emptyList(),
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    observeLibraryRankings: ObserveLibraryRankingsUseCase,
    observeListeningHistory: ObserveListeningHistoryUseCase,
) : ViewModel() {

    private val period = MutableStateFlow(AnalyticsPeriod.LAST_30_DAYS)

    val uiState = period
        .flatMapLatest { selected ->
            combine(
                observeLibraryRankings(selected),
                observeListeningHistory(selected),
            ) { rankings, history ->
                LibraryUiState(
                    period = selected,
                    tracks = rankings.tracks,
                    artists = rankings.artists,
                    albums = rankings.albums,
                    history = history,
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
