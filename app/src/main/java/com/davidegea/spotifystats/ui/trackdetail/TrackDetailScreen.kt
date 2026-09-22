package com.davidegea.spotifystats.ui.trackdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.domain.model.TrackDetail
import com.davidegea.spotifystats.ui.components.EntityHero
import com.davidegea.spotifystats.ui.components.MetricBarRow
import com.davidegea.spotifystats.ui.components.SectionHeading
import com.davidegea.spotifystats.ui.components.StatsPage
import com.davidegea.spotifystats.ui.components.listeningTime
import java.text.NumberFormat
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
        EntityHero(
            eyebrow = stringResource(R.string.library_songs),
            title = detail.name,
            subtitle = detail.artistName,
            primaryValue = NumberFormat.getIntegerInstance().format(detail.totalPlays),
            primaryLabel = stringResource(R.string.metric_plays),
            secondaryValue = listeningTime(detail.totalListeningMs),
            secondaryLabel = stringResource(R.string.metric_listening),
            roundArtwork = false,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SectionHeading(stringResource(R.string.detail_listening_history))
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

        SectionHeading(stringResource(R.string.detail_plays_by_year))
        if (detail.playsByYear.isEmpty()) {
            Text(
                stringResource(R.string.detail_no_play_history),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val maxListening = detail.playsByYear.maxOf { it.listeningMs }.coerceAtLeast(1)
            detail.playsByYear.forEach { year ->
                MetricBarRow(
                    label = year.year.toString(),
                    value = stringResource(
                        R.string.plays_and_time,
                        year.plays,
                        listeningTime(year.listeningMs),
                    ),
                    progress = year.listeningMs.toFloat() / maxListening,
                )
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

private fun formatSkipRate(detail: TrackDetail, unknown: String): String {
    if (detail.skipKnownPlays == 0L) return unknown
    val rate = detail.skippedPlays.toDouble() * 100.0 / detail.skipKnownPlays.toDouble()
    return String.format(Locale.getDefault(), "%.1f%%", rate)
}

private fun formatDate(epochMs: Long?, unknown: String): String {
    if (epochMs == null) return unknown
    return SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(epochMs))
}
