package com.davidegea.spotifystats.ui.library

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod

private enum class LibrarySection(
    val label: String,
) {
    Songs("Songs"),
    Artists("Artists"),
    Albums("Albums"),
}

@Composable
fun LibraryRoute(
    viewModel: LibraryViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnalyticsPeriod.entries.forEach { period ->
                FilterChip(
                    selected = state.period == period,
                    onClick = { viewModel.selectPeriod(period) },
                    label = { Text(period.label) },
                )
            }
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
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
                    )
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
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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

private fun formatListeningTime(milliseconds: Long): String {
    val totalMinutes = milliseconds / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        hours.toString() + "h " + minutes.toString() + "m"
    } else {
        minutes.toString() + "m"
    }
}
