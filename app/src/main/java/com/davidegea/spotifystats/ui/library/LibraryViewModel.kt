package com.davidegea.spotifystats.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.ListeningHistoryCursor
import com.davidegea.spotifystats.domain.model.ListeningHistoryItem
import com.davidegea.spotifystats.domain.model.SearchResult
import com.davidegea.spotifystats.domain.model.TimeRange
import com.davidegea.spotifystats.domain.model.TrackRanking
import com.davidegea.spotifystats.domain.usecase.AnalyticsTimeRangeResolver
import com.davidegea.spotifystats.domain.usecase.ExploreListeningUseCase
import com.davidegea.spotifystats.domain.usecase.LoadListeningHistoryPageUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveLibraryRankingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val period: AnalyticsPeriod = AnalyticsPeriod.LAST_30_DAYS,
    val customRange: TimeRange? = null,
    val loading: Boolean = true,
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val limit: Int = 250,
    val error: String? = null,
    val tracks: List<TrackRanking> = emptyList(),
    val artists: List<ArtistRanking> = emptyList(),
    val albums: List<AlbumRanking> = emptyList(),
    val history: List<ListeningHistoryItem> = emptyList(),
    val historyLoading: Boolean = false,
    val historyHasMore: Boolean = false,
    val historyError: String? = null,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    explore: ExploreListeningUseCase,
    observeLibraryRankings: ObserveLibraryRankingsUseCase,
    private val loadListeningHistoryPage: LoadListeningHistoryPageUseCase,
) : ViewModel() {

    private val period = MutableStateFlow(AnalyticsPeriod.LAST_30_DAYS)
    private val custom = MutableStateFlow<TimeRange?>(null)
    private val query = MutableStateFlow("")
    private val rankingLimit = MutableStateFlow(DEFAULT_RANKING_LIMIT)
    private val historyState = MutableStateFlow(HistoryPagingState(loading = true))
    private var loadMoreHistoryJob: Job? = null

    private val selection = combine(
        period,
        custom,
        query.debounce(250),
        rankingLimit,
    ) { selected, range, text, count ->
        LibrarySelection(selected, range, text, count)
    }

    private val baseState = selection.flatMapLatest { selected ->
        combine(
            observeLibraryRankings(
                selected.period,
                limit = selected.limit,
                customRange = selected.range,
            ),
            explore.search(
                selected.query,
                selected.range ?: AnalyticsTimeRangeResolver().resolve(selected.period),
            ),
        ) { rankings, results ->
            LibraryUiState(
                period = selected.period,
                customRange = selected.range,
                query = selected.query,
                loading = false,
                results = results,
                limit = selected.limit,
                tracks = rankings.tracks,
                artists = rankings.artists,
                albums = rankings.albums,
            )
        }.onStart {
            emit(LibraryUiState(period = selected.period, customRange = selected.range, query = selected.query, limit = selected.limit))
        }.catch {
            emit(
                LibraryUiState(
                    period = selected.period,
                    customRange = selected.range,
                    query = selected.query,
                    limit = selected.limit,
                    loading = false,
                    error = "Unable to load this selection.",
                ),
            )
        }
    }

    val uiState = combine(baseState, historyState) { base, history ->
        base.copy(
            history = history.items,
            historyLoading = history.loading,
            historyHasMore = history.hasMore,
            historyError = history.error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(historyLoading = true),
    )

    init {
        viewModelScope.launch {
            combine(period, custom) { selected, range -> selected to range }
                .distinctUntilChanged()
                .collectLatest { (selected, range) ->
                    loadMoreHistoryJob?.cancel()
                    historyState.value = HistoryPagingState(loading = true)
                    try {
                        val page = loadListeningHistoryPage(
                            period = selected,
                            cursor = null,
                            pageSize = HISTORY_PAGE_SIZE,
                            customRange = range,
                        )
                        historyState.value = HistoryPagingState(
                            items = page.items,
                            nextCursor = page.nextCursor,
                            hasMore = page.hasMore,
                            loading = false,
                        )
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        historyState.value = HistoryPagingState(
                            loading = false,
                            hasMore = false,
                            error = "Unable to load listening history.",
                        )
                    }
                }
        }
    }

    fun selectCustom(range: TimeRange) {
        custom.value = range
        rankingLimit.value = DEFAULT_RANKING_LIMIT
    }

    fun search(text: String) {
        query.value = text
    }

    fun loadMoreRankings() {
        rankingLimit.value += RANKING_PAGE_SIZE
    }

    fun loadMoreHistory() {
        val snapshot = historyState.value
        val cursor = snapshot.nextCursor ?: return
        if (snapshot.loading || !snapshot.hasMore) return

        val selectedPeriod = period.value
        val selectedRange = custom.value

        loadMoreHistoryJob?.cancel()
        loadMoreHistoryJob = viewModelScope.launch {
            historyState.update { it.copy(loading = true, error = null) }
            try {
                val page = loadListeningHistoryPage(
                    period = selectedPeriod,
                    cursor = cursor,
                    pageSize = HISTORY_PAGE_SIZE,
                    customRange = selectedRange,
                )

                if (selectedPeriod != period.value || selectedRange != custom.value) return@launch

                historyState.update { current ->
                    current.copy(
                        items = (current.items + page.items).distinctBy(ListeningHistoryItem::eventId),
                        nextCursor = page.nextCursor,
                        hasMore = page.hasMore,
                        loading = false,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                historyState.update {
                    it.copy(
                        loading = false,
                        error = "Unable to load more listening history.",
                    )
                }
            }
        }
    }

    fun selectPeriod(period: AnalyticsPeriod) {
        custom.value = null
        rankingLimit.value = DEFAULT_RANKING_LIMIT
        this.period.value = period
    }

    private data class HistoryPagingState(
        val items: List<ListeningHistoryItem> = emptyList(),
        val nextCursor: ListeningHistoryCursor? = null,
        val hasMore: Boolean = false,
        val loading: Boolean = false,
        val error: String? = null,
    )

    private companion object {
        const val DEFAULT_RANKING_LIMIT = 250
        const val RANKING_PAGE_SIZE = 250
        const val HISTORY_PAGE_SIZE = 100
    }
}

private data class LibrarySelection(
    val period: AnalyticsPeriod,
    val range: TimeRange?,
    val query: String,
    val limit: Int,
)
