package com.davidegea.spotifystats.ui.importhistory

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ImportHistorySection(
    onImportStarted: () -> Unit = {},
    onImportFinished: () -> Unit = {},
    viewModel: ImportHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (state) {
            is ImportHistoryUiState.Complete,
            is ImportHistoryUiState.Failed,
            -> onImportFinished()
            else -> Unit
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportStarted()
            viewModel.importDocuments(uris.map { it.toString() })
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Import Spotify history",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Select one or more JSON files, or a ZIP containing your Extended Streaming History. Processing stays on this device.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text("Request Extended streaming history from the Privacy settings of your Spotify account, then import the downloaded ZIP or audio JSON files. Regular account exports and podcasts are not supported.", style = MaterialTheme.typography.bodySmall)

            when (val current = state) {
                ImportHistoryUiState.Idle -> Unit
                is ImportHistoryUiState.Importing -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        text = "Processed " +
                            current.progress.processedRecords +
                            " records · " +
                            current.progress.insertedEvents +
                            " new plays",
                    )
                    current.progress.documentName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                is ImportHistoryUiState.Complete -> {
                    Text(
                        text = "Imported " + current.summary.insertedEvents +
                            " plays · " + current.summary.duplicateEvents +
                            " duplicates ignored · " + current.summary.skippedRecords +
                            " unsupported/invalid rows",
                    )
                    if (current.summary.insertedEvents == 0L && current.summary.duplicateEvents == 0L) {
                        Text("No supported music events found. Check that you selected Extended Streaming History audio files.")
                    }
                    if (current.summary.failedDocuments > 0) {
                        Text(
                            text = current.summary.failedDocuments.toString() +
                                " document(s) could not be fully processed.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                is ImportHistoryUiState.Failed -> {
                    Text(
                        text = current.message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (state is ImportHistoryUiState.Importing) TextButton(onClick = viewModel::cancelImport) { Text("Cancel import") }
            Button(
                onClick = {
                    launcher.launch(
                        arrayOf(
                            "application/json",
                            "application/zip",
                            "application/octet-stream",
                        ),
                    )
                },
                enabled = state !is ImportHistoryUiState.Importing,
            ) {
                Text("Choose history files")
            }
        }
    }
}
