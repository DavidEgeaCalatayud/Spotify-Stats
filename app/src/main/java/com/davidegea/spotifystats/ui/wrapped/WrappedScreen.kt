package com.davidegea.spotifystats.ui.wrapped

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.davidegea.spotifystats.domain.analytics.DateRanges
import com.davidegea.spotifystats.ui.components.*
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun WrappedRoute(onBack: () -> Unit, viewModel: WrappedViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var year by rememberSaveable { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
    var month by rememberSaveable { mutableStateOf((Calendar.getInstance().get(Calendar.MONTH) + 1).toString()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var sharing by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Your Wrapped", style = MaterialTheme.typography.headlineLarge)
        Text("A recap for any period, generated entirely on this device.")
        DateRangeControls(state.period, state.customRange, viewModel::selectPeriod, viewModel::selectCustom)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(year, { year = it.take(4) }, label = { Text("Year") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(month, { month = it.take(2) }, label = { Text("Month (1–12)") }, singleLine = true, modifier = Modifier.weight(1f))
        }
        Row {
            TextButton(onClick = {
                try { viewModel.selectCustom(DateRanges.dates("$year-01-01", "$year-12-31")); error = null }
                catch (_: IllegalArgumentException) { error = "Enter a valid year." }
            }) { Text("Year recap") }
            TextButton(onClick = {
                try {
                    val y = requireNotNull(year.toIntOrNull()); val m = requireNotNull(month.toIntOrNull()); require(y in 1900..2200 && m in 1..12)
                    val calendar = Calendar.getInstance().apply { clear(); set(y, m - 1, 1) }
                    val from = String.format(Locale.ROOT, "%04d-%02d-01", y, m)
                    val to = String.format(Locale.ROOT, "%04d-%02d-%02d", y, m, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    viewModel.selectCustom(DateRanges.dates(from, to)); error = null
                } catch (_: IllegalArgumentException) { error = "Enter a valid year and month." }
            }) { Text("Month recap") }
        }
        (error ?: state.error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        val recap = state.recap
        if (recap == null && state.error == null) LinearProgressIndicator(Modifier.fillMaxWidth())
        recap?.let {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(rangeLabel(it.range), style = MaterialTheme.typography.titleLarge)
                    Text("${it.overview.totalPlays} events", style = MaterialTheme.typography.headlineLarge)
                    Text("${listeningTime(it.overview.totalListeningMs)} · ${it.overview.uniqueTracks} different songs")
                    Text("${it.overview.uniqueArtists} artists · ${it.discoveries} first recorded in this period")
                    Text("Top song: ${it.tracks.firstOrNull()?.name ?: "—"}")
                    Text("Top artist: ${it.artists.firstOrNull()?.name ?: "—"}")
                    Text("Top album: ${it.albums.firstOrNull()?.name ?: "—"}")
                }
            }
            Button(enabled = !sharing && it.overview.totalPlays > 0, onClick = {
                sharing = true
                scope.launch {
                    try { RecapCardRenderer.share(context, it) }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { error = "Could not create the share card. Please try again." }
                    finally { sharing = false }
                }
            }) { Text(if (sharing) "Creating card…" else "Share 9:16 card") }
            Text("Only the recap shown here is included in the card. Sharing opens Android's chooser.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
