package com.davidegea.spotifystats.ui.home

import com.davidegea.spotifystats.ui.components.DateRangeControls
import com.davidegea.spotifystats.ui.components.ActivityChart
import com.davidegea.spotifystats.domain.model.TimeRange
import androidx.compose.foundation.clickable
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.ListeningHistoryItem
import com.davidegea.spotifystats.domain.model.TrackRanking
import com.davidegea.spotifystats.ui.components.EmptyStateCard
import com.davidegea.spotifystats.ui.components.ErrorStateCard
import com.davidegea.spotifystats.ui.components.LoadingStateCard
import com.davidegea.spotifystats.ui.importhistory.ImportHistorySection
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeRoute(
    onTrackClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onPeriodSelected = viewModel::selectPeriod,
        onCustom = viewModel::selectCustom,
        onTrackClick = onTrackClick,
        onArtistClick = onArtistClick,
        onAlbumClick = onAlbumClick,
    )
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onPeriodSelected: (AnalyticsPeriod) -> Unit,
    onCustom: (TimeRange) -> Unit,
    onTrackClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Your listening",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Private, offline analytics from the history stored on this device.",
            style = MaterialTheme.typography.bodyLarge,
        )

        DateRangeControls(state.period, state.customRange, onPeriodSelected, onCustom)
        if (state.loading) {
            LoadingStateCard(message = "Loading your listening history…")
            return@Column
        }
        state.error?.let {
            ErrorStateCard(message = it)
            return@Column
        }

        if (state.totalPlays == 0L) {
            EmptyStateCard(
                title = "No listening data in this period",
                body = "Choose another period or import your Spotify Extended Streaming History. Your files are processed locally on this device.",
            )
            ImportHistorySection()
            return@Column
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                label = "Plays",
                value = state.totalPlays.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Listening",
                value = formatListeningTime(state.totalListeningMs),
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                label = "Tracks",
                value = state.uniqueTracks.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Artists",
                value = state.uniqueArtists.toString(),
                modifier = Modifier.weight(1f),
            )
        }

        ActivityChart(state.daily)

        state.topTrack?.let { track ->
            TrackTopCard(
                item = track,
                onClick = { onTrackClick(track.id) },
            )
        }

        state.topArtist?.let { artist ->
            ArtistTopCard(
                item = artist,
                onClick = { onArtistClick(artist.id) },
            )
        }

        state.topAlbum?.let { album ->
            AlbumTopCard(
                item = album,
                onClick = { onAlbumClick(album.id) },
            )
        }

        if (state.recentActivity.isNotEmpty()) {
            Text(
                text = "Recent activity",
                style = MaterialTheme.typography.titleLarge,
            )
            state.recentActivity.forEach { item ->
                RecentActivityRow(
                    item = item,
                    onClick = { onTrackClick(item.trackId) },
                )
            }
        }

        Text(
            text = "Import more history",
            style = MaterialTheme.typography.titleLarge,
        )
        ImportHistorySection()
    }
}

@Composable
private fun TrackTopCard(
    item: TrackRanking,
    onClick: () -> Unit,
) {
    TopEntityCard(
        eyebrow = "Top song",
        title = item.name,
        subtitle = item.artistName,
        footer = item.plays.toString() + " plays · " + formatListeningTime(item.listeningMs),
        onClick = onClick,
    )
}

@Composable
private fun ArtistTopCard(
    item: ArtistRanking,
    onClick: () -> Unit,
) {
    TopEntityCard(
        eyebrow = "Top artist",
        title = item.name,
        subtitle = null,
        footer = item.plays.toString() + " plays · " + formatListeningTime(item.listeningMs),
        onClick = onClick,
    )
}

@Composable
private fun AlbumTopCard(
    item: AlbumRanking,
    onClick: () -> Unit,
) {
    TopEntityCard(
        eyebrow = "Top album",
        title = item.name,
        subtitle = item.artistName,
        footer = item.plays.toString() + " plays · " + formatListeningTime(item.listeningMs),
        onClick = onClick,
    )
}

@Composable
private fun TopEntityCard(
    eyebrow: String,
    title: String,
    subtitle: String?,
    footer: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Text(
                text = footer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun RecentActivityRow(
    item: ListeningHistoryItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.trackName,
                style = MaterialTheme.typography.titleMedium,
            )
            item.artistName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Column {
            Text(
                text = formatEventDate(item.playedAtEpochMs),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = formatListeningTime(item.listeningMs),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatListeningTime(milliseconds: Long): String {
    val totalMinutes = milliseconds / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        hours.toString() + "h " + minutes.toString() + "m"
    } else {
        minutes.toString() + "m"
    }
}

private fun formatEventDate(epochMs: Long): String =
    DateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT,
    ).format(Date(epochMs))
