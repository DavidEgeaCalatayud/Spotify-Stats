package com.davidegea.spotifystats.ui.artistdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.davidegea.spotifystats.domain.model.ArtistDetail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArtistDetailRoute(
    onBack: () -> Unit,
    viewModel: ArtistDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val current = state) {
        ArtistDetailUiState.Loading -> DetailMessage(
            message = "Loading artist…",
            onBack = onBack,
        )
        ArtistDetailUiState.NotFound -> DetailMessage(
            message = "Artist not found.",
            onBack = onBack,
        )
        is ArtistDetailUiState.Content -> ArtistDetailScreen(
            detail = current.detail,
            onBack = onBack,
        )
    }
}

@Composable
private fun ArtistDetailScreen(
    detail: ArtistDetail,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack) {
            Text("Back")
        }

        Text(
            text = detail.name,
            style = MaterialTheme.typography.headlineMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricCard(
                label = "Plays",
                value = detail.totalPlays.toString(),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = "Listening",
                value = formatListeningTime(detail.totalListeningMs),
                modifier = Modifier.weight(1f),
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Artist history", style = MaterialTheme.typography.titleMedium)
                Text("Different songs: " + detail.uniqueTracks)
                Text("All-time artist rank: #${detail.allTimeRank}")
                Text("Most active year: ${detail.mostActiveYear ?: "—"}")
                Text("First heard: " + formatDate(detail.firstPlayedAtEpochMs))
                Text("Last heard: " + formatDate(detail.lastPlayedAtEpochMs))
            }
        }

        Text("Top songs", style = MaterialTheme.typography.titleLarge)
        if (detail.topTracks.isEmpty()) {
            Text("No song history available.")
        } else {
            detail.topTracks.forEachIndexed { index, track ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text((index + 1).toString(), style = MaterialTheme.typography.titleMedium)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(track.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                track.plays.toString() + " plays · " +
                                    formatListeningTime(track.listeningMs),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMessage(
    message: String,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack) {
            Text("Back")
        }
        Text(message, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatDate(epochMs: Long?): String {
    if (epochMs == null) return "Unknown"
    return SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(epochMs))
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
