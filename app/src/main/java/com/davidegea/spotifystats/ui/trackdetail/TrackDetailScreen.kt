package com.davidegea.spotifystats.ui.trackdetail

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
import com.davidegea.spotifystats.domain.model.TrackDetail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrackDetailRoute(
    onBack: () -> Unit,
    viewModel: TrackDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val current = state) {
        TrackDetailUiState.Loading -> DetailMessage(
            message = "Loading track…",
            onBack = onBack,
        )
        TrackDetailUiState.NotFound -> DetailMessage(
            message = "Track not found.",
            onBack = onBack,
        )
        is TrackDetailUiState.Content -> TrackDetailScreen(
            detail = current.detail,
            onBack = onBack,
        )
    }
}

@Composable
private fun TrackDetailScreen(
    detail: TrackDetail,
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
                Text("Listening history", style = MaterialTheme.typography.titleMedium)
                Text("First played: " + formatDate(detail.firstPlayedAtEpochMs))
                Text("Last played: " + formatDate(detail.lastPlayedAtEpochMs))
                Text("Skip rate: " + formatSkipRate(detail))
                Text("Meaningful listens (≥30 s): ${detail.meaningfulPlays}")
                Text("Average completion: " + (detail.averageCompletion?.let { String.format(Locale.getDefault(), "%.1f%%", it * 100) } ?: "Unknown track duration"))
                if (detail.averageCompletion != null) Text("Completed (≥90%): ${detail.completedPlays}")
                Text("Favourite local hour: " + (detail.favouriteHour?.let { String.format(Locale.getDefault(), "%02d:00", it) } ?: "—"))
                Text("Longest listening streak: ${detail.longestStreakDays} consecutive days")
            }
        }

        Text("Plays by year", style = MaterialTheme.typography.titleLarge)
        if (detail.playsByYear.isEmpty()) {
            Text("No play history available.")
        } else {
            detail.playsByYear.forEach { year ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(year.year.toString(), style = MaterialTheme.typography.titleMedium)
                        Text(
                            year.plays.toString() + " plays · " + formatListeningTime(year.listeningMs),
                            style = MaterialTheme.typography.bodyMedium,
                        )
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

private fun formatSkipRate(detail: TrackDetail): String {
    if (detail.skipKnownPlays == 0L) return "Unknown"
    val rate = detail.skippedPlays.toDouble() * 100.0 / detail.skipKnownPlays.toDouble()
    return String.format(Locale.getDefault(), "%.1f%%", rate)
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
