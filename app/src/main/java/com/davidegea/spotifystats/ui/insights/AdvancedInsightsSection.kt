package com.davidegea.spotifystats.ui.insights

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.domain.model.AdvancedAnalytics
import com.davidegea.spotifystats.ui.components.*

@Composable
fun AdvancedInsightsSection(data: AdvancedAnalytics, onTrack: (Long) -> Unit, onArtist: (Long) -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    SectionHeading(stringResource(R.string.advanced_highlights_title))
    data.obsessions.forEach { item ->
        val multiplier = item.multiplier?.let { String.format(locale, "%.1f", it) } ?: "—"
        InsightStory(stringResource(R.string.story_repeat), item.name,
            stringResource(R.string.advanced_on_repeat_change, item.plays, item.deltaPlays, multiplier)) { onArtist(item.artistId) }
    }
    data.rediscoveries.forEach { item ->
        InsightStory(stringResource(R.string.story_rediscovered), item.name, stringResource(R.string.advanced_gap, item.gapDays)) { onTrack(item.trackId) }
    }
    data.discoveries.forEach { item ->
        InsightStory(stringResource(R.string.story_discovery), item.name, stringResource(R.string.advanced_first_recorded, item.plays)) { onArtist(item.artistId) }
    }
    data.forgotten.forEach { item ->
        InsightStory(stringResource(R.string.story_break), item.name, stringResource(R.string.advanced_taking_break_detail, item.previousPlays)) { onTrack(item.trackId) }
    }
    if (data.discoveries.isEmpty() && data.rediscoveries.isEmpty() && data.obsessions.isEmpty() && data.forgotten.isEmpty()) {
        Text(stringResource(R.string.advanced_no_highlights), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    data.trend?.let { trend ->
        val change = trend.change?.let { String.format(locale, "%+.1f%%", it * 100) } ?: stringResource(R.string.advanced_no_baseline)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.advanced_trend, change), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.advanced_trend_detail, listeningTime(trend.currentMs), listeningTime(trend.previousMs)))
            }
        }
    }
    SectionHeading(stringResource(R.string.advanced_sessions_title))
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(data.sessions.count.toString(), style = MaterialTheme.typography.displaySmall)
            Text(stringResource(R.string.advanced_sessions_summary, data.sessions.count, listeningTime(data.sessions.averageListeningMs)))
            Text(stringResource(R.string.advanced_longest_listening, listeningTime(data.sessions.longestListeningMs)), style = MaterialTheme.typography.titleMedium)
        }
    }
    var showMethod by rememberSaveable { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { showMethod = !showMethod }) { Text(stringResource(R.string.advanced_listen_definition_title)) }
            AnimatedVisibility(showMethod) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.advanced_listen_definition, data.quality.events, data.quality.meaningful))
                    if (data.quality.durationKnown > 0) {
                        Text(stringResource(R.string.advanced_completion, data.quality.completed, data.quality.durationKnown))
                        data.quality.averageCompletion?.let { Text(stringResource(R.string.advanced_average_completion, String.format(locale, "%.1f", it * 100))) }
                    } else Text(stringResource(R.string.advanced_completion_unavailable))
                    Text(stringResource(R.string.advanced_sessions_method), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.advanced_thresholds), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun InsightStory(label: String, name: String, detail: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            LocalArtwork(name, size = 64.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(name, style = MaterialTheme.typography.titleLarge)
                Text(detail, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
