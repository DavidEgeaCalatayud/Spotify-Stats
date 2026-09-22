package com.davidegea.spotifystats.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.domain.model.*
import com.davidegea.spotifystats.ui.components.*
import java.text.DateFormat
import java.util.Date

private enum class LibrarySection { Songs, Artists, Albums, History }
private data class RankingItem(val id: Long, val name: String, val subtitle: String?, val plays: Long, val ms: Long)

@Composable
fun LibraryRoute(onTrackClick: (Long) -> Unit, onArtistClick: (Long) -> Unit, onAlbumClick: (Long) -> Unit, initialRange: TimeRange? = null, initialHistory: Boolean = false, viewModel: LibraryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(initialRange) { initialRange?.let(viewModel::selectCustom) }
    LibraryScreen(state, viewModel::selectPeriod, viewModel::selectCustom, viewModel::search,
        viewModel::loadMoreHistory, viewModel::loadMoreRankings, onTrackClick, onArtistClick, onAlbumClick, initialHistory)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreen(
    state: LibraryUiState, onPeriod: (AnalyticsPeriod) -> Unit, onCustom: (TimeRange) -> Unit,
    onSearch: (String) -> Unit, onMoreHistory: () -> Unit, onMoreRankings: () -> Unit,
    onTrackClick: (Long) -> Unit, onArtistClick: (Long) -> Unit, onAlbumClick: (Long) -> Unit, initialHistory: Boolean = false,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var section by rememberSaveable { mutableStateOf(if (initialHistory) LibrarySection.History else LibrarySection.Songs) }
    val focus = LocalFocusManager.current
    LaunchedEffect(query) { onSearch(query) }
    BackHandler(expanded) { expanded = false; focus.clearFocus() }
    val resultClick: (SearchResult) -> Unit = { result ->
        expanded = false; focus.clearFocus()
        when (result.kind) { "track" -> onTrackClick(result.id); "artist" -> onArtistClick(result.id); "album" -> onAlbumClick(result.id) }
    }
    ContentContainer {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!expanded) {
                Text(stringResource(R.string.library_title), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 20.dp))
                DateRangeControls(state.period, state.customRange, onPeriod, onCustom)
            }
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query, onQueryChange = { query = it }, onSearch = { expanded = false; focus.clearFocus() },
                        expanded = expanded, onExpandedChange = { expanded = it },
                        placeholder = { Text(stringResource(R.string.library_search_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = { if (query.isNotEmpty() || expanded) IconButton(onClick = { query = ""; expanded = false; focus.clearFocus() }) {
                            Icon(Icons.Default.Close, stringResource(R.string.library_clear_search))
                        } },
                    )
                },
                expanded = expanded, onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth(), windowInsets = WindowInsets(0, 0, 0, 0),
            ) { SearchResults(state, query, resultClick) }
            if (!expanded) {
                if (query.isNotBlank()) {
                    SearchResults(state, query, resultClick)
                } else {
                    if (state.error != null) ErrorStateCard(stringResource(R.string.library_read_error))
                    ScrollableTabRow(selectedTabIndex = section.ordinal, edgePadding = 0.dp, containerColor = MaterialTheme.colorScheme.background) {
                        LibrarySection.entries.forEach { candidate ->
                            Tab(selected = section == candidate, onClick = { section = candidate }, text = { Text(sectionLabel(candidate)) })
                        }
                    }
                    AnimatedContent(section, Modifier.weight(1f), transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) }, label = "library section") { selected ->
                        val rankings = when (selected) {
                            LibrarySection.Songs -> state.tracks.map { RankingItem(it.id, it.name, it.artistName, it.plays, it.listeningMs) }
                            LibrarySection.Artists -> state.artists.map { RankingItem(it.id, it.name, null, it.plays, it.listeningMs) }
                            LibrarySection.Albums -> state.albums.map { RankingItem(it.id, it.name, it.artistName, it.plays, it.listeningMs) }
                            LibrarySection.History -> emptyList()
                        }
                        val open: (Long) -> Unit = when (selected) {
                            LibrarySection.Artists -> onArtistClick
                            LibrarySection.Albums -> onAlbumClick
                            else -> onTrackClick
                        }
                        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (selected == LibrarySection.History) {
                                item { Text(stringResource(R.string.library_loaded_events, state.history.size), style = MaterialTheme.typography.bodySmall) }
                                items(state.history, key = { it.eventId }) { item ->
                                    ListItem(
                                        headlineContent = { Text(item.trackName) },
                                        supportingContent = { Column {
                                            item.artistName?.let { Text(it) }
                                            Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(item.playedAtEpochMs)))
                                            if (item.skipped == true) Text(stringResource(R.string.library_skipped))
                                        } },
                                        leadingContent = { LocalArtwork(item.trackName, size = 48.dp) },
                                        modifier = Modifier.clickable { onTrackClick(item.trackId) },
                                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                                    )
                                }
                                if (state.historyLoading) item { LoadingStateCard(stringResource(R.string.library_loading_more)) }
                                if (state.historyError != null) item { ErrorStateCard(stringResource(R.string.library_history_error)) }
                                if (!state.historyLoading && state.historyHasMore) item { TextButton(onClick = onMoreHistory) { Text(stringResource(R.string.library_load_more)) } }
                                if (!state.historyLoading && state.history.isEmpty()) item { EmptyStateCard(stringResource(R.string.state_no_data), stringResource(R.string.library_no_data_period)) }
                            } else {
                                if (rankings.isEmpty()) item { EmptyStateCard(stringResource(R.string.state_no_data), stringResource(R.string.library_no_data_period)) }
                                if (rankings.isNotEmpty()) item { Podium(rankings.take(3), selected == LibrarySection.Artists, open) }
                                itemsIndexed(rankings.drop(3), key = { _, item -> item.id }) { index, item -> RankingRow(index + 4, item, selected == LibrarySection.Artists) { open(item.id) } }
                                if (rankings.size >= state.limit) item { TextButton(onClick = onMoreRankings) { Text(stringResource(R.string.library_load_more)) } }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResults(state: LibraryUiState, query: String, onClick: (SearchResult) -> Unit) {
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 12.dp)) {
        item { Text(stringResource(R.string.library_search_limit), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp)) }
        if (state.query != query) item { LoadingStateCard(stringResource(R.string.state_loading)) }
        else {
            if (query.isNotBlank() && state.results.isEmpty()) item { Text(stringResource(R.string.library_no_matches), Modifier.padding(16.dp)) }
            items(state.results, key = { "${it.kind}:${it.id}" }) { result ->
                ListItem(
                    headlineContent = { Text(result.name) },
                    supportingContent = { Text(stringResource(R.string.search_result_summary, stringResource(when (result.kind) {
                        "artist" -> R.string.library_artists; "album" -> R.string.library_albums; else -> R.string.library_songs
                    }), result.plays, result.subtitle?.let { " · $it" }.orEmpty())) },
                    leadingContent = { LocalArtwork(result.name, round = result.kind == "artist") },
                    modifier = Modifier.clickable { onClick(result) },
                )
            }
        }
    }
}

@Composable
private fun Podium(items: List<RankingItem>, artists: Boolean, onClick: (Long) -> Unit) {
    if (LocalConfiguration.current.fontScale > 1.3f) {
        Column { items.forEachIndexed { index, item -> RankingRow(index + 1, item, artists) { onClick(item.id) } } }
    } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { index, item ->
            Card(onClick = { onClick(item.id) }, modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(
                containerColor = if (index == 0) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            )) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("0${index + 1}", style = MaterialTheme.typography.headlineSmall)
                    LocalArtwork(item.name, size = 56.dp, round = artists)
                    Text(item.name, style = MaterialTheme.typography.titleSmall, maxLines = 3, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { contentDescription = item.name })
                    Text(stringResource(R.string.wrapped_card_plays, item.plays), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun RankingRow(rank: Int, item: RankingItem, artist: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(rank.toString().padStart(2, '0'), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LocalArtwork(item.name, size = 48.dp, round = artist)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            item.subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(stringResource(R.string.plays_and_time, item.plays, listeningTime(item.ms)), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun sectionLabel(section: LibrarySection): String = stringResource(when (section) {
    LibrarySection.Songs -> R.string.library_songs; LibrarySection.Artists -> R.string.library_artists
    LibrarySection.Albums -> R.string.library_albums; LibrarySection.History -> R.string.library_history
})
