package com.davidegea.spotifystats.ui.albumdetail

import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidegea.spotifystats.domain.model.AlbumDetail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlbumDetailRoute(
    onBack: () -> Unit,
    onTrackClick: (Long) -> Unit,
    viewModel: AlbumDetailViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val current = state) {
        AlbumDetailUiState.Loading -> DetailMessage(
            message = "Loading album…",
            onBack = onBack,
        )
        AlbumDetailUiState.NotFound -> DetailMessage(
            message = "Album not found.",
            onBack = onBack,
        )
        is AlbumDetailUiState.Content -> AlbumDetailScreen(
            detail = current.detail,
            onBack = onBack,
            onTrackClick = onTrackClick,
        )
    }
}

@Composable
private fun AlbumDetailScreen(
    detail: AlbumDetail,
    onBack: () -> Unit,
    onTrackClick: (Long) -> Unit,
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
        detail.artistName?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
            )
        }

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
                Text("Album history", style = MaterialTheme.typography.titleMedium)
                Text("Different songs: " + detail.uniqueTracks)
                Text("First listened: " + formatDate(detail.firstPlayedAtEpochMs))
                Text("Last listened: " + formatDate(detail.lastPlayedAtEpochMs))
            }
        }

        Text("Top songs", style = MaterialTheme.typography.titleLarge)
        if (detail.topTracks.isEmpty()) {
            Text("No song history available.")
        } else {
            detail.topTracks.forEachIndexed { index, track ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTrackClick(track.id) },
                ) {
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
