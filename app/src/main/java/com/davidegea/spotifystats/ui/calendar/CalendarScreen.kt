package com.davidegea.spotifystats.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import com.davidegea.spotifystats.R
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.davidegea.spotifystats.domain.model.DailyListening
import com.davidegea.spotifystats.ui.components.listeningTime
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarRoute(onBack: () -> Unit, onTrack: (Long) -> Unit, onArtist: (Long) -> Unit, viewModel: CalendarViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selected by viewModel.selectedDay.collectAsStateWithLifecycle()
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val byDate = remember(state.days) { state.days.associateBy { it.date } }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }; Text(stringResource(R.string.calendar_title), style = MaterialTheme.typography.headlineMedium) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { viewModel.changeYear(-1) }) { Text(stringResource(R.string.action_previous)) }
                Text(state.year.toString(), style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = { viewModel.changeYear(1) }) { Text(stringResource(R.string.action_next)) }
            }
            Text(
                stringResource(
                    R.string.calendar_year_summary,
                    state.days.sumOf { it.plays },
                    listeningTime(state.days.sumOf { it.listeningMs }),
                ),
            )
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        items(12) { month -> MonthGrid(state.year, month, byDate, viewModel::selectDay) }
    }
    selected?.let { date ->
        AlertDialog(onDismissRequest = { viewModel.selectDay(null) }, title = { Text(date) }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val recap = detail
                if (recap == null) Text(stringResource(R.string.calendar_loading_day)) else {
                    Text(
                        stringResource(
                            R.string.calendar_day_summary,
                            recap.overview.totalPlays,
                            recap.overview.uniqueTracks,
                            recap.overview.uniqueArtists,
                        ),
                    )
                    Text(listeningTime(recap.overview.totalListeningMs))
                    recap.artists.firstOrNull()?.let { artist -> TextButton(onClick = { viewModel.selectDay(null); onArtist(artist.id) }) {
                        Text(stringResource(R.string.calendar_top_artist, artist.name))
                    } }
                    recap.tracks.firstOrNull()?.let { track -> TextButton(onClick = { viewModel.selectDay(null); onTrack(track.id) }) {
                        Text(stringResource(R.string.calendar_top_song, track.name))
                    } }
                }
            }
        }, confirmButton = { TextButton(onClick = { viewModel.selectDay(null) }) { Text(stringResource(R.string.action_close)) } })
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
        val locale = Locale.getDefault()
        val weekdayNames = remember(locale) {
            val names = DateFormatSymbols.getInstance(locale).shortWeekdays
            listOf(
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY,
                Calendar.SUNDAY,
            ).map { names[it].take(2) }
        }
        Row {
            weekdayNames.forEach { Text(it, Modifier.weight(1f)) }
        }
        for (week in 0 until (offset + count + 6) / 7) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (weekday in 0..6) {
                    val number = week * 7 + weekday - offset + 1
                    if (number !in 1..count) Spacer(Modifier.weight(1f).height(48.dp)) else {
                        val date = String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, number)
                        val value = days[date]
                        val color = MaterialTheme.colorScheme.primary.copy(alpha = if (value == null) 0.04f else 0.15f + 0.65f * value.listeningMs / max)
                        val dayDescription = stringResource(
                            R.string.calendar_day_accessibility,
                            date,
                            value?.plays ?: 0,
                            listeningTime(value?.listeningMs ?: 0),
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(color, MaterialTheme.shapes.small)
                                .clickable { onDay(date) }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = dayDescription
                                }
                                .padding(6.dp),
                        ) { Text(number.toString()) }
                    }
                }
            }
        }
    }
}
