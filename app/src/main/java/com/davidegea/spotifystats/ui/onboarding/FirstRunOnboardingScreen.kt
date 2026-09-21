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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.R
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
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.onboarding_body),
            style = MaterialTheme.typography.bodyLarge,
        )

        PrincipleCard(
            title = stringResource(R.string.onboarding_import_title),
            body = stringResource(R.string.onboarding_import_body),
        )
        PrincipleCard(
            title = stringResource(R.string.onboarding_local_title),
            body = stringResource(R.string.onboarding_local_body),
        )
        PrincipleCard(
            title = stringResource(R.string.onboarding_explore_title),
            body = stringResource(R.string.onboarding_explore_body),
        )

        ImportHistorySection(
            onImportStarted = onImportStarted,
            onImportFinished = onImportFinished,
        )

        OutlinedButton(
            onClick = onContinueWithoutImport,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.onboarding_explore_without_import))
        }

        Text(
            text = stringResource(R.string.onboarding_reimport_note),
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
