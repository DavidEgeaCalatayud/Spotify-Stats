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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.domain.model.AlbumDetail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlbumDetailRoute(
    onBack: () -> Unit,
    onTrackClick: (Long) -> Unit,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val current = state) {
        AlbumDetailUiState.Loading -> DetailMessage(
            message = stringResource(R.string.detail_loading_album),
            onBack = onBack,
        )
        AlbumDetailUiState.NotFound -> DetailMessage(
            message = stringResource(R.string.detail_album_not_found),
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
    val unknown = stringResource(R.string.detail_unknown)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.action_back))
        }

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
                    stringResource(R.string.detail_album_history),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(stringResource(R.string.detail_different_songs, detail.uniqueTracks))
                Text(stringResource(R.string.detail_active_days, detail.activeDays))
                Text(
                    stringResource(
                        R.string.detail_meaningful_album_listens,
                        detail.meaningfulPlays,
                    ),
                )
                Text(
                    stringResource(
                        R.string.detail_peak_month,
                        detail.peakMonth ?: "—",
                    ),
                )
                Text(
                    stringResource(
                        R.string.detail_first_listened,
                        formatDate(detail.firstPlayedAtEpochMs, unknown),
                    ),
                )
                Text(
                    stringResource(
                        R.string.detail_last_listened,
                        formatDate(detail.lastPlayedAtEpochMs, unknown),
                    ),
                )
            }
        }

        Text(
            stringResource(R.string.detail_album_year_history),
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
                        Text(
                            year.year.toString(),
                            style = MaterialTheme.typography.titleMedium,
                        )
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

        Text(
            stringResource(R.string.detail_top_songs),
            style = MaterialTheme.typography.titleLarge,
        )
        if (detail.topTracks.isEmpty()) {
            Text(stringResource(R.string.detail_no_song_history))
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
                                stringResource(
                                    R.string.plays_and_time,
                                    track.plays,
                                    formatListeningTime(track.listeningMs),
                                ),
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
private fun DetailMessage(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.action_back))
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

private fun formatDate(epochMs: Long?, unknown: String): String {
    if (epochMs == null) return unknown
    return SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(epochMs))
}

private fun formatListeningTime(milliseconds: Long): String {
    val totalMinutes = milliseconds / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) hours.toString() + "h " + minutes.toString() + "m"
    else minutes.toString() + "m"
}
