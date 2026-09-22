package com.davidegea.spotifystats.ui.albumdetail

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.domain.model.AlbumDetail
import com.davidegea.spotifystats.ui.components.EntityHero
import com.davidegea.spotifystats.ui.components.LocalArtwork
import com.davidegea.spotifystats.ui.components.MetricBarRow
import com.davidegea.spotifystats.ui.components.SectionHeading
import com.davidegea.spotifystats.ui.components.StatPill
import com.davidegea.spotifystats.ui.components.StatsPage
import com.davidegea.spotifystats.ui.components.listeningTime
import java.text.NumberFormat
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

    StatsPage(stringResource(R.string.library_albums), onBack) {
        EntityHero(
            eyebrow = stringResource(R.string.library_albums),
            title = detail.name,
            subtitle = detail.artistName,
            primaryValue = NumberFormat.getIntegerInstance().format(detail.totalPlays),
            primaryLabel = stringResource(R.string.metric_plays),
            secondaryValue = listeningTime(detail.totalListeningMs),
            secondaryLabel = stringResource(R.string.metric_listening),
            roundArtwork = false,
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatPill(
                label = stringResource(R.string.metric_tracks),
                value = NumberFormat.getIntegerInstance().format(detail.uniqueTracks),
                modifier = Modifier.weight(1f),
            )
            StatPill(
                label = stringResource(R.string.detail_active_days, detail.activeDays),
                value = NumberFormat.getIntegerInstance().format(detail.activeDays),
                modifier = Modifier.weight(1f),
            )
        }

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
                SectionHeading(stringResource(R.string.detail_album_history))
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

        SectionHeading(stringResource(R.string.detail_album_year_history))
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

        SectionHeading(stringResource(R.string.detail_top_songs))
        if (detail.topTracks.isEmpty()) {
            Text(
                stringResource(R.string.detail_no_song_history),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            detail.topTracks.forEachIndexed { index, track ->
                Card(
                    onClick = { onTrackClick(track.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            (index + 1).toString().padStart(2, '0'),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        LocalArtwork(track.name, size = 52.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(track.name, style = MaterialTheme.typography.titleMedium)
                            track.artistName?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                stringResource(
                                    R.string.plays_and_time,
                                    track.plays,
                                    listeningTime(track.listeningMs),
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
    StatsPage(stringResource(R.string.library_albums), onBack) {
        Text(message, style = MaterialTheme.typography.titleLarge)
    }
}

private fun formatDate(epochMs: Long?, unknown: String): String {
    if (epochMs == null) return unknown
    return SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(epochMs))
}
