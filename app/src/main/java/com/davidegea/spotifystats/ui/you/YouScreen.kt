package com.davidegea.spotifystats.ui.you

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.ui.components.isoDate
import com.davidegea.spotifystats.ui.importhistory.ImportHistorySection

@Composable
fun YouRoute(
    onWrapped: () -> Unit,
    onCalendar: () -> Unit,
    viewModel: YouViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingRestore by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    val export = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        uri?.let { viewModel.export(it.toString()) }
    }
    val restore = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        pendingRestore = uri?.toString()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.you_title), style = MaterialTheme.typography.headlineLarge)
        Button(onClick = onWrapped) {
            Text(stringResource(R.string.you_generate_wrapped))
        }
        OutlinedButton(onClick = onCalendar) {
            Text(stringResource(R.string.you_calendar))
        }

        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    stringResource(R.string.you_privacy_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(stringResource(R.string.you_privacy_body_1))
                Text(stringResource(R.string.you_privacy_body_2))
                Text(stringResource(R.string.you_privacy_body_3))
            }
        }

        Text(
            stringResource(R.string.you_backup_restore),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(stringResource(R.string.you_backup_restore_body))
        if (state.busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        state.message?.let { Text(it) }

        OutlinedButton(
            enabled = !state.busy,
            onClick = {
                export.launch(
                    "MusicStatsBackup_" +
                        isoDate(System.currentTimeMillis()) +
                        ".zip",
                )
            },
        ) {
            Text(stringResource(R.string.you_export_backup))
        }

        OutlinedButton(
            enabled = !state.busy,
            onClick = {
                restore.launch(
                    arrayOf("application/zip", "application/octet-stream"),
                )
            },
        ) {
            Text(stringResource(R.string.you_restore_backup))
        }

        OutlinedButton(
            enabled = !state.busy,
            onClick = { confirmDelete = true },
        ) {
            Text(
                stringResource(R.string.you_delete_history),
                color = MaterialTheme.colorScheme.error,
            )
        }

        ImportHistorySection()

        Text(
            stringResource(R.string.you_spotify_live_sync),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(stringResource(R.string.you_spotify_live_sync_body))
    }

    pendingRestore?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text(stringResource(R.string.you_restore_title)) },
            text = { Text(stringResource(R.string.you_restore_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingRestore = null
                        viewModel.restore(uri)
                    },
                ) {
                    Text(stringResource(R.string.you_restore_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.you_delete_title)) },
            text = { Text(stringResource(R.string.you_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        viewModel.delete()
                    },
                ) {
                    Text(stringResource(R.string.you_delete_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
