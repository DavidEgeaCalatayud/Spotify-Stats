package com.davidegea.spotifystats.ui.insights

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ListeningHeatmapCell
import java.text.DateFormatSymbols
import java.util.Locale

@Composable
fun InsightsRoute(
    viewModel: InsightsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    InsightsScreen(
        state = state,
        onPeriodSelected = viewModel::selectPeriod,
    )
}

@Composable
private fun InsightsScreen(
    state: InsightsUiState,
    onPeriodSelected: (AnalyticsPeriod) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Insights",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Listening habits calculated locally from your Spotify history.",
            style = MaterialTheme.typography.bodyLarge,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnalyticsPeriod.entries.forEach { period ->
                FilterChip(
                    selected = state.period == period,
                    onClick = { onPeriodSelected(period) },
                    label = { Text(period.label) },
                )
            }
        }

        if (state.heatmap.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No listening data in this period.",
                    modifier = Modifier.padding(20.dp),
                )
            }
            return@Column
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InsightCard(
                label = "Favourite hour",
                value = state.favouriteHour?.let(::formatHour) ?: "—",
                modifier = Modifier.weight(1f),
            )
            InsightCard(
                label = "Favourite day",
                value = state.favouriteWeekday?.let(::weekdayLabel) ?: "—",
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InsightCard(
                label = "Morning",
                value = formatPercent(state.morningShare),
                modifier = Modifier.weight(1f),
            )
            InsightCard(
                label = "Night",
                value = formatPercent(state.nightShare),
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            text = "Playback behaviour",
            style = MaterialTheme.typography.titleLarge,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InsightCard(
                label = "Skipped",
                value = formatPercent(state.skipRate),
                modifier = Modifier.weight(1f),
            )
            InsightCard(
                label = "Shuffle",
                value = formatPercent(state.shuffleRate),
                modifier = Modifier.weight(1f),
            )
            InsightCard(
                label = "Offline",
                value = formatPercent(state.offlineRate),
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            text = "Listening heatmap",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "Local device time · darker blocks mean more listening time.",
            style = MaterialTheme.typography.bodySmall,
        )
        ListeningHeatmap(state.heatmap)
    }
}

@Composable
private fun InsightCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ListeningHeatmap(
    cells: List<ListeningHeatmapCell>,
) {
    val values = cells.associateBy { it.weekday to it.hour }
    val maxListening = cells.maxOfOrNull(ListeningHeatmapCell::listeningMs) ?: 0L

    Column(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "    ",
                style = MaterialTheme.typography.labelSmall,
            )
            (0..23).forEach { hour ->
                Text(
                    text = if (hour % 3 == 0) hour.toString().padStart(2, '0') else "  ",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        (0..6).forEach { weekday ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = weekdayLabel(weekday).take(3).padEnd(3),
                    style = MaterialTheme.typography.labelSmall,
                )
                (0..23).forEach { hour ->
                    val value = values[weekday to hour]?.listeningMs ?: 0L
                    Text(
                        text = heatmapSymbol(value, maxListening),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

private fun heatmapSymbol(
    value: Long,
    max: Long,
): String {
    if (value <= 0L || max <= 0L) return "· "
    val ratio = value.toDouble() / max.toDouble()
    return when {
        ratio <= 0.25 -> "░ "
        ratio <= 0.50 -> "▒ "
        ratio <= 0.75 -> "▓ "
        else -> "█ "
    }
}

private fun formatHour(hour: Int): String =
    hour.toString().padStart(2, '0') + ":00"

private fun weekdayLabel(weekday: Int): String {
    val names = DateFormatSymbols.getInstance(Locale.getDefault()).shortWeekdays
    return names.getOrNull(weekday + 1)?.takeIf(String::isNotBlank)
        ?: weekday.toString()
}

private fun formatPercent(value: Double?): String =
    value?.let {
        String.format(Locale.getDefault(), "%.1f%%", it * 100.0)
    } ?: "—"
