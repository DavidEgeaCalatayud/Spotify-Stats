package com.davidegea.spotifystats.ui.importhistory

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ImportHistorySection(
    viewModel: ImportHistoryViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        viewModel.importDocuments(uris.map { it.toString() })
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
