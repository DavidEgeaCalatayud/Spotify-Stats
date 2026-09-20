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
import com.davidegea.spotifystats.domain.model.TimeRange
import com.davidegea.spotifystats.domain.model.SearchResult
import com.davidegea.spotifystats.domain.usecase.ExploreListeningUseCase
import com.davidegea.spotifystats.domain.usecase.AnalyticsTimeRangeResolver
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class LibraryUiState(
    val period: AnalyticsPeriod = AnalyticsPeriod.LAST_30_DAYS,
    val customRange: TimeRange? = null,
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val limit: Int = 250,
    val error: String? = null,
    val tracks: List<TrackRanking> = emptyList(),
    val artists: List<ArtistRanking> = emptyList(),
    val albums: List<AlbumRanking> = emptyList(),
    val history: List<ListeningHistoryItem> = emptyList(),
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    explore: ExploreListeningUseCase,
    observeLibraryRankings: ObserveLibraryRankingsUseCase,
    observeListeningHistory: ObserveListeningHistoryUseCase,
) : ViewModel() {

    private val period = MutableStateFlow(AnalyticsPeriod.LAST_30_DAYS)

    private val custom = MutableStateFlow<TimeRange?>(null)
    private val query = MutableStateFlow("")
    private val limit = MutableStateFlow(250)
    val uiState = combine(period, custom, query.debounce(250), limit) { selected, range, text, count ->
        LibrarySelection(selected, range, text, count)
    }.flatMapLatest { (selected, range, text, count) ->
            combine(
                observeLibraryRankings(selected, limit = count, customRange = range),
                observeListeningHistory(selected, limit = count, customRange = range),
                explore.search(text, range ?: AnalyticsTimeRangeResolver().resolve(selected)),
            ) { rankings, history, results ->
                LibraryUiState(
                    period = selected,
                    customRange = range, query = text, results = results, limit = count,
                    tracks = rankings.tracks,
                    artists = rankings.artists,
                    albums = rankings.albums,
                    history = history,
                )
            }.catch { emit(LibraryUiState(period = selected, customRange = range, query = text, error = "Unable to load this selection.")) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState(),
        )

    fun selectCustom(range: TimeRange) { custom.value = range; limit.value = 250 }
    fun search(text: String) { query.value = text }
    fun loadMore() { limit.value += 250 }

    fun selectPeriod(period: AnalyticsPeriod) {
        custom.value = null
        limit.value = 250
        this.period.value = period
    }
}

private data class LibrarySelection(val period: AnalyticsPeriod, val range: TimeRange?, val query: String, val limit: Int)
