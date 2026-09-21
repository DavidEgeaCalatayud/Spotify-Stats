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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidegea.spotifystats.R
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
            message = stringResource(R.string.detail_loading_artist),
            onBack = onBack,
        )
        ArtistDetailUiState.NotFound -> DetailMessage(
            message = stringResource(R.string.detail_artist_not_found),
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
                    stringResource(R.string.detail_artist_history),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(stringResource(R.string.detail_different_songs, detail.uniqueTracks))
                Text(stringResource(R.string.detail_all_time_artist_rank, detail.allTimeRank))
                Text(
                    stringResource(
                        R.string.detail_most_active_year,
                        detail.mostActiveYear?.toString() ?: "—",
                    ),
                )
                Text(
                    stringResource(
                        R.string.detail_first_heard,
                        formatDate(detail.firstPlayedAtEpochMs, unknown),
                    ),
                )
                Text(
                    stringResource(
                        R.string.detail_last_heard,
                        formatDate(detail.lastPlayedAtEpochMs, unknown),
                    ),
                )
            }
        }

        Text(
            stringResource(R.string.detail_rank_history),
            style = MaterialTheme.typography.titleLarge,
        )
        if (detail.rankByYear.isEmpty()) {
            Text(stringResource(R.string.detail_no_rank_history))
        } else {
            detail.rankByYear.forEach { year ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(
                            R.string.detail_rank_year,
                            year.year,
                            year.rank,
                            year.plays,
                            formatListeningTime(year.listeningMs),
                        ),
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
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
