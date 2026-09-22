package com.davidegea.spotifystats.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.designsystem.StatsPalette
import com.davidegea.spotifystats.domain.model.*
import com.davidegea.spotifystats.ui.components.*
import com.davidegea.spotifystats.ui.importhistory.ImportHistorySection
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date

@Composable
fun HomeRoute(
    onTrackClick: (Long) -> Unit, onArtistClick: (Long) -> Unit, onAlbumClick: (Long) -> Unit,
    onImport: () -> Unit, viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(state, viewModel::selectPeriod, viewModel::selectCustom, onTrackClick, onArtistClick, onAlbumClick, onImport)
}

@Composable
internal fun HomeScreen(
    state: HomeUiState, onPeriodSelected: (AnalyticsPeriod) -> Unit, onCustom: (TimeRange) -> Unit,
    onTrackClick: (Long) -> Unit, onArtistClick: (Long) -> Unit, onAlbumClick: (Long) -> Unit, onImport: () -> Unit,
) {
    StatsPage {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                    in 5..11 -> R.string.home_morning
                    in 12..19 -> R.string.home_afternoon
                    else -> R.string.home_evening
                }
                Text(stringResource(greeting), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineLarge)
            }
            LocalArtwork("Spotify Stats", size = 48.dp, round = true)
        }
        DateRangeControls(state.period, state.customRange, onPeriodSelected, onCustom)
        if (state.loading) { LoadingStateCard(stringResource(R.string.home_loading)); return@StatsPage }
        if (state.error != null) { ErrorStateCard(stringResource(R.string.home_read_error)); return@StatsPage }
        if (state.totalPlays == 0L) {
            EmptyStateCard(stringResource(R.string.home_empty_period_title), stringResource(R.string.home_empty_period_body))
            ImportHistorySection()
            return@StatsPage
        }
        val largeText = LocalConfiguration.current.fontScale > 1.3f
        BoxWithConstraints {
            val overview: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    HeroSurface {
                        Text(stringResource(R.string.home_in_rotation), style = MaterialTheme.typography.labelLarge, color = StatsPalette.mint)
                        Text(state.customRange?.let { rangeLabel(it) } ?: stringResource(periodLabelRes(state.period)), style = MaterialTheme.typography.titleMedium)
                        AnimatedMetric(state.totalPlays)
                        Text(stringResource(R.string.metric_plays), style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider(color = StatsPalette.mint.copy(alpha = 0.25f))
                        Text(listeningTime(state.totalListeningMs), style = MaterialTheme.typography.headlineMedium, color = StatsPalette.mint)
                        state.previousListeningMs?.let { previous ->
                            if (previous > 0) {
                                val percent = NumberFormat.getPercentInstance().apply { maximumFractionDigits = 1 }.format((state.totalListeningMs - previous).toDouble() / previous)
                                Text(stringResource(R.string.home_period_change, percent), style = MaterialTheme.typography.bodyMedium)
                            } else Text(stringResource(R.string.advanced_no_baseline), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatPill(stringResource(R.string.metric_tracks), NumberFormat.getIntegerInstance().format(state.uniqueTracks), Modifier.weight(1f))
                        StatPill(stringResource(R.string.metric_artists), NumberFormat.getIntegerInstance().format(state.uniqueArtists), Modifier.weight(1f))
                    }
                    ActivityChart(state.daily, state.range, state.previousDaily, state.previousRange)
                }
            }
            val favourites: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeading(stringResource(R.string.home_heavy_rotation))
                    state.topTrack?.let { item -> TopEntityCard(stringResource(R.string.home_top_song), item.name, item.artistName, item.plays, item.listeningMs, true) { onTrackClick(item.id) } }
                    state.topArtist?.let { item -> TopEntityCard(stringResource(R.string.home_top_artist), item.name, null, item.plays, item.listeningMs, false) { onArtistClick(item.id) } }
                    state.topAlbum?.let { item -> TopEntityCard(stringResource(R.string.home_top_album), item.name, item.artistName, item.plays, item.listeningMs, false) { onAlbumClick(item.id) } }
                }
            }
            if (maxWidth >= 720.dp && !largeText) Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Box(Modifier.weight(1.15f)) { overview() }
                Box(Modifier.weight(1f)) { favourites() }
            } else Column(verticalArrangement = Arrangement.spacedBy(24.dp)) { overview(); favourites() }
        }
        if (state.recentActivity.isNotEmpty()) {
            SectionHeading(stringResource(R.string.home_recent_activity))
            state.recentActivity.forEach { item ->
                Row(Modifier.fillMaxWidth().clickable { onTrackClick(item.trackId) }.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    LocalArtwork(item.trackName)
                    Column(Modifier.weight(1f)) {
                        Text(item.trackName, style = MaterialTheme.typography.titleMedium)
                        item.artistName?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.playedAtEpochMs)), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        TextButton(onClick = onImport) { Text(stringResource(R.string.home_import_more)) }
    }
}

@Composable
private fun TopEntityCard(eyebrow: String, title: String, subtitle: String?, plays: Long, listeningMs: Long, featured: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
        containerColor = if (featured) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = if (featured) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
    )) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(eyebrow, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Text("01", style = MaterialTheme.typography.headlineMedium)
            }
            LocalArtwork(title, size = if (featured) 104.dp else 64.dp)
            Text(title, style = if (featured) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
            Text(stringResource(R.string.plays_and_time, plays, listeningTime(listeningMs)), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
