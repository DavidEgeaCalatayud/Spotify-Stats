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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.davidegea.spotifystats.R
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.davidegea.spotifystats.domain.model.ImportDocumentDiagnostic
import com.davidegea.spotifystats.domain.model.ImportDocumentStatus

@Composable
fun ImportHistorySection(
    onImportStarted: () -> Unit = {},
    onImportFinished: () -> Unit = {},
    viewModel: ImportHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(state) {
        when (state) {
            is ImportHistoryUiState.Complete -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onImportFinished()
            }
            is ImportHistoryUiState.Failed -> onImportFinished()
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

    Card(modifier = Modifier.fillMaxWidth().animateContentSize(tween(260))) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.import_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.import_body),
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(stringResource(R.string.import_instructions), style = MaterialTheme.typography.bodySmall)

            OutlinedButton(
                onClick = { uriHandler.openUri(SPOTIFY_ACCOUNT_PRIVACY_URL) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.import_request_history))
            }
            Text(
                text = stringResource(R.string.import_request_history_note),
                style = MaterialTheme.typography.bodySmall,
            )

            when (val current = state) {
                ImportHistoryUiState.Idle -> Unit
                is ImportHistoryUiState.Importing -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        text = if (current.queued) {
                            stringResource(R.string.import_queued)
                        } else {
                            stringResource(R.string.import_background)
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = stringResource(
                            R.string.import_progress,
                            current.progress.processedRecords,
                            current.progress.insertedEvents,
                        ),
                    )
                    current.progress.documentName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    DocumentDiagnostics(current.documents)
                }
                is ImportHistoryUiState.Complete -> {
                    Text(
                        text = stringResource(
                            R.string.import_summary,
                            current.summary.insertedEvents,
                            current.summary.duplicateEvents,
                            current.summary.skippedRecords,
                        ),
                    )
                    if (current.summary.insertedEvents == 0L && current.summary.duplicateEvents == 0L) {
                        Text(stringResource(R.string.import_no_supported))
                    }
                    if (current.summary.failedDocuments > 0) {
                        Text(
                            text = stringResource(
                                R.string.import_failed_documents,
                                current.summary.failedDocuments,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    DocumentDiagnostics(current.documents)
                }
                is ImportHistoryUiState.Failed -> {
                    Text(
                        text = current.message,
                        color = MaterialTheme.colorScheme.error,
                    )
                    DocumentDiagnostics(current.documents)
                    if (current.canRetry) {
                        TextButton(onClick = viewModel::retryImport) {
                            Text(stringResource(R.string.import_resume))
                        }
                    }
                }
            }

            if (state is ImportHistoryUiState.Importing) {
                TextButton(onClick = viewModel::cancelImport) {
                    Text(stringResource(R.string.import_cancel))
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
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.import_choose_files))
            }
        }
    }
}


@Composable
private fun DocumentDiagnostics(
    documents: List<ImportDocumentDiagnostic>,
) {
    if (documents.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.import_files),
            style = MaterialTheme.typography.labelLarge,
        )
        documents.forEach { document ->
            val name = document.displayName
                ?: stringResource(R.string.import_history_file, document.position + 1)
            Text(
                text = stringResource(
                    R.string.import_file_summary,
                    name,
                    importDocumentStatusLabel(document.status),
                    document.processedRecords,
                    document.insertedEvents,
                    document.duplicateEvents,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            document.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}


@Composable
private fun importDocumentStatusLabel(status: ImportDocumentStatus): String = stringResource(
    when (status) {
        ImportDocumentStatus.PENDING -> R.string.import_status_pending
        ImportDocumentStatus.RUNNING -> R.string.import_status_running
        ImportDocumentStatus.COMPLETED -> R.string.import_status_completed
        ImportDocumentStatus.FAILED -> R.string.import_status_failed
        ImportDocumentStatus.CANCELLED -> R.string.import_status_cancelled
    },
)

private const val SPOTIFY_ACCOUNT_PRIVACY_URL = "https://www.spotify.com/account/privacy/"
