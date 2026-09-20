package com.davidegea.spotifystats.ui.you

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidegea.spotifystats.ui.components.isoDate
import com.davidegea.spotifystats.ui.importhistory.ImportHistorySection

@Composable
fun YouRoute(onWrapped: () -> Unit, onCalendar: () -> Unit, viewModel: YouViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingRestore by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri -> uri?.let { viewModel.export(it.toString()) } }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> pendingRestore = uri?.toString() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("You", style = MaterialTheme.typography.headlineLarge)
        Button(onClick = onWrapped) { Text("Generate your Wrapped") }
        OutlinedButton(onClick = onCalendar) { Text("Listening calendar") }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Your data, on your device", style = MaterialTheme.typography.titleLarge)
                Text("No account, application server, advertising or telemetry. The offline app has no Internet permission. IP addresses and user agents are discarded during import.")
                Text("Android's application sandbox protects local storage. Database encryption has not been enabled. Backups are portable ZIP files containing your history; keep them in a private location.")
                Text("You choose whether to share recap images or save a backup to a cloud provider through Android's file picker. Nothing is uploaded automatically.")
            }
        }
        Text("Backup and restore", style = MaterialTheme.typography.titleLarge)
        Text("Restore replaces the current history after validating the entire backup. Export a copy first if you want to keep both datasets.")
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.message?.let { Text(it) }
        OutlinedButton(enabled = !state.busy, onClick = { export.launch("MusicStatsBackup_${isoDate(System.currentTimeMillis())}.zip") }) { Text("Export backup") }
        OutlinedButton(enabled = !state.busy, onClick = { restore.launch(arrayOf("application/zip", "application/octet-stream")) }) { Text("Restore backup") }
        OutlinedButton(enabled = !state.busy, onClick = { confirmDelete = true }) { Text("Delete listening history", color = MaterialTheme.colorScheme.error) }
        ImportHistorySection()
        Text("Spotify Live Sync", style = MaterialTheme.typography.titleLarge)
        Text("The optional Spotify adapter is not enabled in this build. Importing your Extended Streaming History works without Spotify login.")
    }
    pendingRestore?.let { uri ->
        AlertDialog(onDismissRequest = { pendingRestore = null }, title = { Text("Replace current history?") }, text = { Text("The selected backup will replace all music and listening events on this device. A damaged or unsupported backup will be rejected before replacement.") },
            confirmButton = { TextButton(onClick = { pendingRestore = null; viewModel.restore(uri) }) { Text("Restore") } },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("Cancel") } })
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Delete listening history?") }, text = { Text("This permanently removes the imported music and listening history from this device. Exported backup files are not deleted.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; viewModel.delete() }) { Text("Delete") } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
}
