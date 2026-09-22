package com.davidegea.spotifystats.ui.insights

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.designsystem.StatsPalette
import com.davidegea.spotifystats.domain.model.*
import com.davidegea.spotifystats.ui.components.*
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.util.Locale

@Composable
fun InsightsRoute(onTrack: (Long) -> Unit, onArtist: (Long) -> Unit, onCalendar: () -> Unit, viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InsightsScreen(state, viewModel::selectPeriod, viewModel::selectCustom, onTrack, onArtist, onCalendar)
}

@Composable
internal fun InsightsScreen(state: InsightsUiState, onPeriodSelected: (AnalyticsPeriod) -> Unit, onCustom: (TimeRange) -> Unit,
    onTrack: (Long) -> Unit, onArtist: (Long) -> Unit, onCalendar: () -> Unit) {
    StatsPage {
        SectionHeading(stringResource(R.string.insights_title), stringResource(R.string.insights_story_subtitle))
        DateRangeControls(state.period, state.customRange, onPeriodSelected, onCustom)
        TextButton(onClick = onCalendar) { Text(stringResource(R.string.insights_open_calendar)) }
        if (state.loading) { LoadingStateCard(stringResource(R.string.state_loading)); return@StatsPage }
        if (state.error != null) { ErrorStateCard(stringResource(R.string.insights_read_error)); return@StatsPage }
        if (state.heatmap.isEmpty()) { EmptyStateCard(stringResource(R.string.insights_empty), stringResource(R.string.state_no_data_body)); return@StatsPage }
        val night = (state.nightShare ?: 0.0) >= (state.morningShare ?: 0.0)
        HeroSurface {
            Text(stringResource(R.string.insights_your_rhythm), style = MaterialTheme.typography.labelLarge, color = StatsPalette.mint)
            Text(stringResource(if (night) R.string.insights_after_hours else R.string.insights_sunrise), style = MaterialTheme.typography.headlineLarge)
            Text(percent(if (night) state.nightShare else state.morningShare), style = MaterialTheme.typography.displayMedium, color = StatsPalette.mint)
            Text(stringResource(if (night) R.string.insights_night_story else R.string.insights_morning_story))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatPill(stringResource(R.string.insights_favourite_hour), state.favouriteHour?.let(::hourLabel) ?: "—", Modifier.weight(1f))
            StatPill(stringResource(R.string.insights_favourite_day), state.favouriteWeekday?.let(::weekdayLabel) ?: "—", Modifier.weight(1f))
        }
        SectionHeading(stringResource(R.string.insights_heatmap), stringResource(R.string.heatmap_hint))
        ListeningHeatmap(state.heatmap)
        SectionHeading(stringResource(R.string.insights_playback_behaviour), stringResource(R.string.insights_known_fields))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                BehaviourMeter(stringResource(R.string.insights_skipped), state.skipRate)
                BehaviourMeter(stringResource(R.string.insights_shuffle), state.shuffleRate)
                BehaviourMeter(stringResource(R.string.insights_offline), state.offlineRate)
            }
        }
        AdvancedInsightsSection(state.advanced, onTrack, onArtist)
    }
}

@Composable
private fun BehaviourMeter(label: String, value: Double?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text(percent(value), style = MaterialTheme.typography.titleLarge)
        }
        if (value != null) LinearProgressIndicator(progress = { value.toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp))
        else Text(stringResource(R.string.detail_unknown), style = MaterialTheme.typography.bodySmall)
    }
}

internal fun hourLabel(hour: Int): String = String.format(Locale.getDefault(), "%02d:00", hour)
internal fun weekdayLabel(weekday: Int): String = DateFormatSymbols.getInstance().shortWeekdays[weekday + 1]
internal fun percent(value: Double?): String = value?.let { NumberFormat.getPercentInstance().apply { maximumFractionDigits = 1 }.format(it) } ?: "—"
