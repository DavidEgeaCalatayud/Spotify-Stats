package com.davidegea.spotifystats.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidegea.spotifystats.domain.model.DailyListening
import com.davidegea.spotifystats.ui.components.listeningTime
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarRoute(onBack: () -> Unit, onTrack: (Long) -> Unit, onArtist: (Long) -> Unit, viewModel: CalendarViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selected by viewModel.selectedDay.collectAsStateWithLifecycle()
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val byDate = remember(state.days) { state.days.associateBy { it.date } }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { TextButton(onClick = onBack) { Text("Back") }; Text("Listening calendar", style = MaterialTheme.typography.headlineMedium) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { viewModel.changeYear(-1) }) { Text("Previous") }
                Text(state.year.toString(), style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = { viewModel.changeYear(1) }) { Text("Next") }
            }
            Text("${state.days.sumOf { it.plays }} events · ${listeningTime(state.days.sumOf { it.listeningMs })} · local dates")
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        items(12) { month -> MonthGrid(state.year, month, byDate, viewModel::selectDay) }
    }
    selected?.let { date ->
        AlertDialog(onDismissRequest = { viewModel.selectDay(null) }, title = { Text(date) }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val recap = detail
                if (recap == null) Text("Loading day…") else {
                    Text("${recap.overview.totalPlays} events · ${recap.overview.uniqueTracks} songs · ${recap.overview.uniqueArtists} artists")
                    Text(listeningTime(recap.overview.totalListeningMs))
                    recap.artists.firstOrNull()?.let { artist -> TextButton(onClick = { viewModel.selectDay(null); onArtist(artist.id) }) { Text("Top artist: ${artist.name}") } }
                    recap.tracks.firstOrNull()?.let { track -> TextButton(onClick = { viewModel.selectDay(null); onTrack(track.id) }) { Text("Top song: ${track.name}") } }
                }
            }
        }, confirmButton = { TextButton(onClick = { viewModel.selectDay(null) }) { Text("Close") } })
    }
}

@Composable
private fun MonthGrid(year: Int, month: Int, days: Map<String, DailyListening>, onDay: (String) -> Unit) {
    val cal = remember(year, month) { Calendar.getInstance().apply { clear(); set(year, month, 1) } }
    val offset = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val count = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val max = days.values.maxOfOrNull { it.listeningMs }?.coerceAtLeast(1) ?: 1
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(DateFormatSymbols.getInstance().months[month], style = MaterialTheme.typography.titleLarge)
        Row { listOf("M", "T", "W", "T", "F", "S", "S").forEach { Text(it, Modifier.weight(1f)) } }
        for (week in 0 until (offset + count + 6) / 7) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (weekday in 0..6) {
                    val number = week * 7 + weekday - offset + 1
                    if (number !in 1..count) Spacer(Modifier.weight(1f).height(48.dp)) else {
                        val date = String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, number)
                        val value = days[date]
                        val color = MaterialTheme.colorScheme.primary.copy(alpha = if (value == null) 0.04f else 0.15f + 0.65f * value.listeningMs / max)
                        Box(Modifier.weight(1f).height(48.dp).background(color, MaterialTheme.shapes.small).clickable { onDay(date) }.semantics {
                            contentDescription = "$date: ${value?.plays ?: 0} events, ${listeningTime(value?.listeningMs ?: 0)}"
                        }.padding(6.dp)) { Text(number.toString()) }
                    }
                }
            }
        }
    }
}
