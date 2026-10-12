package com.davidegea.spotifystats.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.designsystem.StatsPalette
import com.davidegea.spotifystats.domain.model.*
import com.davidegea.spotifystats.ui.components.*
import com.davidegea.spotifystats.ui.importhistory.ImportHistorySection
import com.davidegea.spotifystats.ui.settings.AppPreferences
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date

@Composable
fun HomeRoute(
    onTrackClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onImport: () -> Unit,
    demoMode: Boolean = false,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val realState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var onlineArtwork by remember { mutableStateOf(AppPreferences.onlineArtwork(context)) }
    val state = if (demoMode) remember { DemoDashboard.sample() } else realState
    HomeScreen(
        state = state,
        onPeriodSelected = viewModel::selectPeriod,
        onCustom = viewModel::selectCustom,
        onTrackClick = onTrackClick,
        onArtistClick = onArtistClick,
        onAlbumClick = onAlbumClick,
        onImport = onImport,
        demoMode = demoMode,
        onlineArtwork = onlineArtwork,
        onOnlineArtworkChange = {
            onlineArtwork = it
            AppPreferences.setOnlineArtwork(context, it)
        },
    )
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onPeriodSelected: (AnalyticsPeriod) -> Unit,
    onCustom: (TimeRange) -> Unit,
    onTrackClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onImport: () -> Unit,
    demoMode: Boolean = false,
    onlineArtwork: Boolean = false,
    onOnlineArtworkChange: (Boolean) -> Unit = {},
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
        if (demoMode) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.demo_badge), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.demo_disclaimer), style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.demo_import_action))
                    }
                }
            }
        } else {
            DateRangeControls(state.period, state.customRange, onPeriodSelected, onCustom)
        }
        if (state.loading) { LoadingStateCard(stringResource(R.string.home_loading)); return@StatsPage }
        if (state.error != null) { ErrorStateCard(stringResource(R.string.home_read_error)); return@StatsPage }
        if (state.totalPlays == 0L) {
            EmptyStateCard(stringResource(R.string.home_empty_period_title), stringResource(R.string.home_empty_period_body))
            ImportHistorySection()
            return@StatsPage
        }

        // The explicit choice is OFF by default. Only Spotify track IDs are requested.
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.artwork_online_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.artwork_online_explanation),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.width(12.dp))
                Switch(checked = onlineArtwork, onCheckedChange = onOnlineArtworkChange)
            }
        }

        val largeText = LocalConfiguration.current.fontScale > 1.3f
        val spotlight: @Composable () -> Unit = {
            state.topArtist?.let { item ->
                ArtistSpotlight(
                    item = item,
                    artworkEnabled = onlineArtwork,
                    onClick = { if (!demoMode) onArtistClick(item.id) },
                )
            }
        }
        val summary: @Composable () -> Unit = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HeroSurface {
                    Text(stringResource(R.string.home_in_rotation), style = MaterialTheme.typography.labelLarge, color = StatsPalette.mint)
                    Text(state.customRange?.let { rangeLabel(it) } ?: stringResource(periodLabelRes(state.period)),
                        style = MaterialTheme.typography.titleMedium)
                    AnimatedMetric(state.totalPlays)
                    Text(stringResource(R.string.metric_plays), style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider(color = StatsPalette.mint.copy(alpha = 0.25f))
                    Text(listeningTime(state.totalListeningMs), style = MaterialTheme.typography.headlineMedium, color = StatsPalette.mint)
                    state.previousListeningMs?.let { previous ->
                        if (previous > 0) {
                            val percent = NumberFormat.getPercentInstance().apply { maximumFractionDigits = 1 }
                                .format((state.totalListeningMs - previous).toDouble() / previous)
                            Text(stringResource(R.string.home_period_change, percent), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatPill(stringResource(R.string.metric_tracks), NumberFormat.getIntegerInstance().format(state.uniqueTracks), Modifier.weight(1f))
                    StatPill(stringResource(R.string.metric_artists), NumberFormat.getIntegerInstance().format(state.uniqueArtists), Modifier.weight(1f))
                }
            }
        }
        val favourites: @Composable () -> Unit = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionHeading(stringResource(R.string.home_heavy_rotation))
                state.topTrack?.let { item ->
                    TopEntityCard(
                        stringResource(R.string.home_top_song), item.name, item.artistName,
                        item.plays, item.listeningMs, item.spotifyUri, onlineArtwork,
                    ) { if (!demoMode) onTrackClick(item.id) }
                }
                state.topAlbum?.let { item ->
                    TopEntityCard(
                        stringResource(R.string.home_top_album), item.name, item.artistName,
                        item.plays, item.listeningMs, item.representativeTrackUri, onlineArtwork,
                    ) { if (!demoMode) onAlbumClick(item.id) }
                }
            }
        }
        if (LocalConfiguration.current.screenWidthDp >= 720 && !largeText) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(Modifier.weight(1.15f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    spotlight()
                    ActivityChart(state.daily, state.range, state.previousDaily, state.previousRange)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    summary()
                    favourites()
                }
            }
        } else {
            spotlight()
            summary()
            ActivityChart(state.daily, state.range, state.previousDaily, state.previousRange)
            favourites()
        }
        if (!demoMode && state.recentActivity.isNotEmpty()) {
            SectionHeading(stringResource(R.string.home_recent_activity))
            state.recentActivity.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().clickable { onTrackClick(item.trackId) }.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LocalArtwork(item.trackName)
                    Column(Modifier.weight(1f)) {
                        Text(item.trackName, style = MaterialTheme.typography.titleMedium)
                        item.artistName?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.playedAtEpochMs)), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        if (!demoMode) TextButton(onClick = onImport) { Text(stringResource(R.string.home_import_more)) }
    }
}

@Composable
private fun ArtistSpotlight(item: ArtistRanking, artworkEnabled: Boolean, onClick: () -> Unit) {
    HeroSurface {
        Text(stringResource(R.string.home_top_artist), style = MaterialTheme.typography.labelLarge, color = StatsPalette.mint)
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick), horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically) {
            MusicArtwork(item.name, item.representativeTrackUri, artworkEnabled, size = 116.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.name, style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.plays_and_time, item.plays, listeningTime(item.listeningMs)),
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
        SpotifyCredit(item.representativeTrackUri, artworkEnabled)
    }
}

@Composable
private fun TopEntityCard(
    eyebrow: String,
    title: String,
    subtitle: String?,
    plays: Long,
    listeningMs: Long,
    spotifyUri: String?,
    artworkEnabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MusicArtwork(title, spotifyUri, artworkEnabled, size = 84.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(eyebrow, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleLarge)
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                Text(stringResource(R.string.plays_and_time, plays, listeningTime(listeningMs)),
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        SpotifyCredit(spotifyUri, artworkEnabled)
    }
}

@Composable
private fun SpotifyCredit(uri: String?, enabled: Boolean) {
    val id = remember(uri) { SpotifyArtworkSource.trackId(uri) }
    val context = LocalContext.current
    if (enabled && id != null) {
        TextButton(
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SpotifyArtworkSource.trackUrl(id))))
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            Text(stringResource(R.string.artwork_spotify_credit))
        }
    }
}
