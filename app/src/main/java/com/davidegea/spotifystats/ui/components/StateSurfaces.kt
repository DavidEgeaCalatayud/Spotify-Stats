package com.davidegea.spotifystats.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.R

@Composable
fun LoadingStateCard(message: String, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite; progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate }) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(message, style = MaterialTheme.typography.titleMedium)
            listOf(0.55f, 0.9f, 0.7f).forEachIndexed { index, width ->
                Box(Modifier.fillMaxWidth(width).height(if (index == 0) 48.dp else 16.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), MaterialTheme.shapes.small))
            }
        }
    }
}

@Composable
fun EmptyStateCard(title: String, body: String, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LocalArtwork("♪", size = 72.dp, round = true)
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ErrorStateCard(message: String, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Info, null)
            Text(stringResource(R.string.state_error_title), style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
