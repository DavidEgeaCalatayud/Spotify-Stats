package com.davidegea.spotifystats.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.ui.importhistory.ImportHistorySection

@Composable
fun FirstRunOnboardingScreen(
    onImportStarted: () -> Unit,
    onImportFinished: () -> Unit,
    onContinueWithoutImport: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Your complete Spotify history, privately",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Spotify Stats turns your official Extended Streaming History into detailed analytics without uploading it to a server.",
            style = MaterialTheme.typography.bodyLarge,
        )

        PrincipleCard(
            title = "1 · Import",
            body = "Choose the ZIP or JSON files from Spotify's Extended Streaming History export.",
        )
        PrincipleCard(
            title = "2 · Analyse locally",
            body = "Room/SQLite stores your listening events on this device. IP addresses and user-agent fields are ignored.",
        )
        PrincipleCard(
            title = "3 · Explore offline",
            body = "Home, Library, Insights, Calendar, search and Wrapped continue working without an account or network connection.",
        )

        ImportHistorySection(
            onImportStarted = onImportStarted,
            onImportFinished = onImportFinished,
        )

        OutlinedButton(
            onClick = onContinueWithoutImport,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Explore without importing")
        }

        Text(
            text = "You can safely import the same export again later: duplicate play events are ignored. Exploring without data only skips onboarding for this app session.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PrincipleCard(
    title: String,
    body: String,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
