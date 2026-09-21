package com.davidegea.spotifystats.ui.insights

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.domain.model.AdvancedAnalytics
import com.davidegea.spotifystats.ui.components.listeningTime

@Composable
fun AdvancedInsightsSection(
    data: AdvancedAnalytics,
    onTrack: (Long) -> Unit,
    onArtist: (Long) -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]

    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.advanced_listen_definition_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                stringResource(
                    R.string.advanced_listen_definition,
                    data.quality.events,
                    data.quality.meaningful,
                ),
            )
            if (data.quality.durationKnown > 0) {
                Text(
                    stringResource(
                        R.string.advanced_completion,
                        data.quality.completed,
                        data.quality.durationKnown,
                    ),
                )
                data.quality.averageCompletion?.let {
                    Text(
                        stringResource(
                            R.string.advanced_average_completion,
                            String.format(locale, "%.1f", it * 100),
                        ),
                    )
                }
            } else {
                Text(stringResource(R.string.advanced_completion_unavailable))
            }
        }
    }

    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.advanced_sessions_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                stringResource(
                    R.string.advanced_sessions_summary,
                    data.sessions.count,
                    listeningTime(data.sessions.averageListeningMs),
                ),
            )
            Text(
                stringResource(
                    R.string.advanced_longest_listening,
                    listeningTime(data.sessions.longestListeningMs),
                ),
            )
            Text(
                stringResource(R.string.advanced_sessions_method),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    data.trend?.let { trend ->
        val change = trend.change?.let {
            String.format(locale, "%+.1f%%", it * 100)
        } ?: stringResource(R.string.advanced_no_baseline)

        Text(
            stringResource(R.string.advanced_trend, change),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            stringResource(
                R.string.advanced_trend_detail,
                listeningTime(trend.currentMs),
                listeningTime(trend.previousMs),
            ),
        )
    }

    Text(
        stringResource(R.string.advanced_highlights_title),
        style = MaterialTheme.typography.titleLarge,
    )

    data.discoveries.forEach { item ->
        Highlight(
            stringResource(R.string.advanced_new_artist, item.name),
            stringResource(R.string.advanced_first_recorded, item.plays),
        ) {
            onArtist(item.artistId)
        }
    }

    data.rediscoveries.forEach { item ->
        Highlight(
            stringResource(R.string.advanced_rediscovered, item.name),
            stringResource(R.string.advanced_gap, item.gapDays),
        ) {
            onTrack(item.trackId)
        }
    }

    data.obsessions.forEach { item ->
        val multiplier = item.multiplier?.let {
            String.format(locale, "%.1f", it)
        } ?: "—"
        Highlight(
            stringResource(R.string.advanced_on_repeat, item.name),
            stringResource(
                R.string.advanced_on_repeat_change,
                item.plays,
                item.deltaPlays,
                multiplier,
            ),
        ) {
            onArtist(item.artistId)
        }
    }

    data.forgotten.forEach { item ->
        Highlight(
            stringResource(R.string.advanced_taking_break, item.name),
            stringResource(
                R.string.advanced_taking_break_detail,
                item.previousPlays,
            ),
        ) {
            onTrack(item.trackId)
        }
    }

    if (
        data.discoveries.isEmpty() &&
        data.rediscoveries.isEmpty() &&
        data.obsessions.isEmpty() &&
        data.forgotten.isEmpty()
    ) {
        Text(stringResource(R.string.advanced_no_highlights))
    }

    Text(
        stringResource(R.string.advanced_thresholds),
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun Highlight(
    title: String,
    detail: String,
    onClick: () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(detail)
        }
    }
}
