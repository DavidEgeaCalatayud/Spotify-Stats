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
import com.davidegea.spotifystats.ui.components.StatsPage
import com.davidegea.spotifystats.ui.components.LocalArtwork
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidegea.spotifystats.R
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
            message = stringResource(R.string.detail_loading_track),
            onBack = onBack,
        )
        TrackDetailUiState.NotFound -> DetailMessage(
            message = stringResource(R.string.detail_track_not_found),
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
    val unknown = stringResource(R.string.detail_unknown)

    StatsPage(stringResource(R.string.library_songs), onBack) {
        LocalArtwork(detail.name, size = 128.dp, round = false)

        Text(detail.name, style = MaterialTheme.typography.headlineMedium)
        detail.artistName?.let {
            Text(it, style = MaterialTheme.typography.titleMedium)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricCard(
                label = stringResource(R.string.metric_plays),
                value = detail.totalPlays.toString(),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = stringResource(R.string.metric_listening),
                value = formatListeningTime(detail.totalListeningMs),
                modifier = Modifier.weight(1f),
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(R.string.detail_listening_history),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(
                        R.string.detail_first_played,
                        formatDate(detail.firstPlayedAtEpochMs, unknown),
                    ),
                )
                Text(
                    stringResource(
                        R.string.detail_last_played,
                        formatDate(detail.lastPlayedAtEpochMs, unknown),
                    ),
                )
                Text(
                    stringResource(
                        R.string.detail_skip_rate,
                        formatSkipRate(detail, unknown),
                    ),
                )
                Text(
                    stringResource(
                        R.string.detail_meaningful_listens,
                        detail.meaningfulPlays,
                    ),
                )
                val completion = detail.averageCompletion?.let {
                    String.format(Locale.getDefault(), "%.1f%%", it * 100)
                } ?: stringResource(R.string.detail_unknown_duration)
                Text(stringResource(R.string.detail_average_completion, completion))
                if (detail.averageCompletion != null) {
                    Text(stringResource(R.string.detail_completed, detail.completedPlays))
                }
                val favouriteHour = detail.favouriteHour?.let {
                    String.format(Locale.getDefault(), "%02d:00", it)
                } ?: "—"
                Text(stringResource(R.string.detail_favourite_hour, favouriteHour))
                Text(
                    stringResource(
                        R.string.detail_longest_streak,
                        detail.longestStreakDays,
                    ),
                )
            }
        }

        Text(
            stringResource(R.string.detail_plays_by_year),
            style = MaterialTheme.typography.titleLarge,
        )
        if (detail.playsByYear.isEmpty()) {
            Text(stringResource(R.string.detail_no_play_history))
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
                            stringResource(
                                R.string.plays_and_time,
                                year.plays,
                                formatListeningTime(year.listeningMs),
                            ),
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
    StatsPage(stringResource(R.string.library_songs), onBack) {
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

private fun formatSkipRate(detail: TrackDetail, unknown: String): String {
    if (detail.skipKnownPlays == 0L) return unknown
    val rate = detail.skippedPlays.toDouble() * 100.0 / detail.skipKnownPlays.toDouble()
    return String.format(Locale.getDefault(), "%.1f%%", rate)
}

private fun formatDate(epochMs: Long?, unknown: String): String {
    if (epochMs == null) return unknown
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
