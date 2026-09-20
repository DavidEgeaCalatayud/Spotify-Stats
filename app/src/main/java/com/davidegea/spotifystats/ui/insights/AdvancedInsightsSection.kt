package com.davidegea.spotifystats.ui.insights

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.domain.model.AdvancedAnalytics
import com.davidegea.spotifystats.ui.components.listeningTime
import java.util.Locale

@Composable
fun AdvancedInsightsSection(data: AdvancedAnalytics, onTrack: (Long) -> Unit, onArtist: (Long) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("What counts as a listen?", style = MaterialTheme.typography.titleLarge)
            Text("${data.quality.events} recorded events · ${data.quality.meaningful} listens of at least 30 seconds")
            if (data.quality.durationKnown > 0) {
                Text("${data.quality.completed} completed (at least 90%) / ${data.quality.durationKnown} events with known track duration")
                data.quality.averageCompletion?.let { Text("${String.format(Locale.getDefault(), "%.1f", it * 100)}% average completion") }
            } else Text("Completion unavailable: the export does not contain track duration.")
        }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Listening sessions", style = MaterialTheme.typography.titleLarge)
            Text("${data.sessions.count} sessions · ${listeningTime(data.sessions.averageListeningMs)} average listening")
            Text("Longest listening: ${listeningTime(data.sessions.longestListeningMs)}")
            Text("Inferred from playback intervals. A break longer than 30 minutes starts a session. Overlapping devices can increase summed listening time.", style = MaterialTheme.typography.bodySmall)
        }
    }
    data.trend?.let { trend ->
        val change = trend.change?.let { String.format(Locale.getDefault(), "%+.1f%%", it * 100) } ?: "No previous listening baseline"
        Text("Trend: $change", style = MaterialTheme.typography.titleMedium)
        Text("${listeningTime(trend.currentMs)} versus ${listeningTime(trend.previousMs)} in the preceding interval of equal length. Missing imports can affect comparisons.")
    }
    Text("Highlights from imported history", style = MaterialTheme.typography.titleLarge)
    data.discoveries.forEach { item -> Highlight("New artist · ${item.name}", "First recorded in this period · ${item.plays} events") { onArtist(item.artistId) } }
    data.rediscoveries.forEach { item -> Highlight("Rediscovered · ${item.name}", "${item.gapDays} days since the last recorded play before this period") { onTrack(item.trackId) } }
    data.obsessions.forEach { item -> Highlight("On repeat · ${item.name}", "${item.plays} events, up from ${item.previousPlays} in the preceding interval") { onArtist(item.artistId) } }
    data.forgotten.forEach { item -> Highlight("Taking a break · ${item.name}", "${item.previousPlays} events in the preceding interval; none recorded in this one") { onTrack(item.trackId) } }
    if (data.discoveries.isEmpty() && data.rediscoveries.isEmpty() && data.obsessions.isEmpty() && data.forgotten.isEmpty()) Text("No highlights meet the thresholds in this period.")
    Text("On repeat requires 10 events and twice the preceding count. Rediscovery requires a gap of 90 days. These describe the imported records, not your complete life unless you imported it all.", style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun Highlight(title: String, detail: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(detail) }
    }
}
