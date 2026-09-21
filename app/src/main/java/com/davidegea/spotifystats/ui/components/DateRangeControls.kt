package com.davidegea.spotifystats.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.domain.analytics.DateRanges
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.TimeRange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DateRangeControls(period: AnalyticsPeriod, custom: TimeRange?, onPeriod: (AnalyticsPeriod) -> Unit, onCustom: (TimeRange) -> Unit) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AnalyticsPeriod.entries.forEach {
            FilterChip(selected = custom == null && period == it, onClick = { onPeriod(it) }, label = { Text(it.label) })
        }
        FilterChip(selected = custom != null, onClick = { showDialog = true }, label = { Text("Custom dates") })
    }
    if (custom != null) Text(rangeLabel(custom), style = MaterialTheme.typography.bodySmall)
    if (showDialog) DateRangeDialog(onDismiss = { showDialog = false }, onSelected = { onCustom(it); showDialog = false })
}

@Composable
fun DateRangeDialog(onDismiss: () -> Unit, onSelected: (TimeRange) -> Unit) {
    var from by rememberSaveable { mutableStateOf(isoDate(System.currentTimeMillis())) }
    var to by rememberSaveable { mutableStateOf(from) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Choose dates") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Both dates included, in your device's time zone.")
            OutlinedTextField(value = from, onValueChange = { from = it }, label = { Text("From · YYYY-MM-DD") }, singleLine = true)
            OutlinedTextField(value = to, onValueChange = { to = it }, label = { Text("To · YYYY-MM-DD") }, singleLine = true)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = {
        TextButton(onClick = {
            try { onSelected(DateRanges.dates(from, to)) } catch (e: IllegalArgumentException) { error = e.message }
        }) { Text("Apply") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

fun isoDate(value: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date(value))
fun rangeLabel(range: TimeRange): String = if (range.fromInclusive == Long.MIN_VALUE) "All time" else "${isoDate(range.fromInclusive)} – ${isoDate(range.toInclusive)}"
fun listeningTime(ms: Long): String = "${ms / 3_600_000}h ${(ms % 3_600_000) / 60_000}m"
