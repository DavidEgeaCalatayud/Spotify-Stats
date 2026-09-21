package com.davidegea.spotifystats.ui.library

import com.davidegea.spotifystats.ui.components.DateRangeControls
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ListeningHistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class LibrarySection(
    val label: String,
) {
    Songs("Songs"),
    Artists("Artists"),
    Albums("Albums"),
    History("History"),
}

@Composable
fun LibraryRoute(
    onTrackClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var section by remember { mutableStateOf(LibrarySection.Songs) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Library",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
        )

        DateRangeControls(state.period, state.customRange, viewModel::selectPeriod, viewModel::selectCustom)
        OutlinedTextField(query, { query = it; viewModel.search(it) }, label = { Text("Search songs, artists and albums") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (query.isNotBlank()) {
            Text("Up to 100 matching results in this period", style = MaterialTheme.typography.bodySmall)
            LazyColumn {
                if (state.results.isEmpty()) item { Text("No matches. Try another name or date range.", Modifier.padding(16.dp)) }
                items(state.results, key = { "${it.kind}:${it.id}" }) { result ->
                    Column(Modifier.fillMaxWidth().clickable {
                        when (result.kind) { "track" -> onTrackClick(result.id); "artist" -> onArtistClick(result.id); "album" -> onAlbumClick(result.id) }
                    }.padding(vertical = 12.dp)) {
                        Text(result.name, style = MaterialTheme.typography.titleMedium)
                        Text("${result.kind} · ${result.plays} events" + (result.subtitle?.let { " · $it" } ?: ""))
                    }
                }
            }
            return@Column
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LibrarySection.entries.forEach { candidate ->
                FilterChip(
                    selected = section == candidate,
                    onClick = { section = candidate },
                    label = { Text(candidate.label) },
                )
            }
        }

        if (section == LibrarySection.History) {
            Text(
                text = "Loaded ${state.history.size} events in the selected period",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val currentCount = when (section) {
                LibrarySection.Songs -> state.tracks.size
                LibrarySection.Artists -> state.artists.size
                LibrarySection.Albums -> state.albums.size
                LibrarySection.History -> state.history.size
            }
            if (currentCount == 0 && !(section == LibrarySection.History && state.historyLoading)) {
                item { Text("No listening data in this period.", Modifier.padding(16.dp)) }
            }
            when (section) {
                LibrarySection.Songs -> itemsIndexed(
                    items = state.tracks,
                    key = { _, item -> item.id },
                ) { index, item ->
                    RankingRow(
                        rank = index + 1,
                        title = item.name,
                        subtitle = item.artistName,
                        plays = item.plays,
                        listeningMs = item.listeningMs,
                        onClick = { onTrackClick(item.id) },
                    )
                }

                LibrarySection.Artists -> itemsIndexed(
                    items = state.artists,
                    key = { _, item -> item.id },
                ) { index, item ->
                    RankingRow(
                        rank = index + 1,
                        title = item.name,
                        subtitle = null,
                        plays = item.plays,
                        listeningMs = item.listeningMs,
                        onClick = { onArtistClick(item.id) },
                    )
                }

                LibrarySection.Albums -> itemsIndexed(
                    items = state.albums,
                    key = { _, item -> item.id },
                ) { index, item ->
                    RankingRow(
                        rank = index + 1,
                        title = item.name,
                        subtitle = item.artistName,
                        plays = item.plays,
                        listeningMs = item.listeningMs,
                        onClick = { onAlbumClick(item.id) },
                    )
                }

                LibrarySection.History -> itemsIndexed(
                    items = state.history,
                    key = { _, item -> item.eventId },
                ) { _, item ->
                    HistoryRow(
                        item = item,
                        onClick = { onTrackClick(item.trackId) },
                    )
                }
            }
            if (section == LibrarySection.History) {
                state.historyError?.let { message ->
                    item {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                if (state.historyLoading) {
                    item {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                } else if (state.historyHasMore) {
                    item {
                        TextButton(onClick = viewModel::loadMoreHistory) {
                            Text("Load more")
                        }
                    }
                }
            } else if (currentCount >= state.limit) {
                item {
                    TextButton(onClick = viewModel::loadMoreRankings) {
                        Text("Load more")
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingRow(
    rank: Int,
    title: String,
    subtitle: String?,
    plays: Long,
    listeningMs: Long,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 2.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Column {
            Text(
                text = plays.toString() + " plays",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = formatListeningTime(listeningMs),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun HistoryRow(
    item: ListeningHistoryItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.trackName,
                style = MaterialTheme.typography.titleMedium,
            )
            val context = listOfNotNull(item.artistName, item.albumName)
                .joinToString(" · ")
            if (context.isNotBlank()) {
                Text(
                    text = context,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = formatHistoryTimestamp(item.playedAtEpochMs),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Column {
            Text(
                text = formatListeningTime(item.listeningMs),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (item.skipped == true) {
                Text(
                    text = "Skipped",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun formatHistoryTimestamp(epochMs: Long): String =
    SimpleDateFormat("d MMM · HH:mm", Locale.getDefault()).format(Date(epochMs))

private fun formatListeningTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1_000
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> hours.toString() + "h " + minutes.toString() + "m"
        minutes > 0 -> minutes.toString() + "m " + seconds.toString() + "s"
        else -> seconds.toString() + "s"
    }
}
