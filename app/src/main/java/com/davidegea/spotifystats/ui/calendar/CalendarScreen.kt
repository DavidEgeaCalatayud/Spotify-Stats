package com.davidegea.spotifystats.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.domain.model.DailyListening
import com.davidegea.spotifystats.ui.components.*
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarRoute(onBack: () -> Unit, onTrack: (Long) -> Unit, onArtist: (Long) -> Unit, onDay: (String) -> Unit, viewModel: CalendarViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selected by viewModel.selectedDay.collectAsStateWithLifecycle()
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    var month by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    val byDate = remember(state.days) { state.days.associateBy { it.date } }
    StatsPage(stringResource(R.string.calendar_title), onBack) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { viewModel.changeYear(-1) }, enabled = state.year > 1900) { Text(stringResource(R.string.action_previous)) }
            Text(state.year.toString(), style = MaterialTheme.typography.headlineLarge)
            TextButton(onClick = { viewModel.changeYear(1) }, enabled = state.year < 2200) { Text(stringResource(R.string.action_next)) }
        }
        HeroSurface {
            Text(stringResource(R.string.calendar_your_year), style = MaterialTheme.typography.labelLarge)
            Text(listeningTime(state.days.sumOf { it.listeningMs }), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.wrapped_card_plays, state.days.sumOf { it.plays }))
        }
        ScrollableTabRow(month, edgePadding = 0.dp, containerColor = MaterialTheme.colorScheme.background) {
            DateFormatSymbols.getInstance().shortMonths.take(12).forEachIndexed { index, label ->
                Tab(selected = index == month, onClick = { month = index }, text = { Text(label) })
            }
        }
        if (state.loading) LoadingStateCard(stringResource(R.string.state_loading))
        else if (state.error != null) ErrorStateCard(stringResource(R.string.calendar_read_error))
        else {
            SectionHeading(DateFormatSymbols.getInstance().months[month], stringResource(R.string.calendar_tap_day))
            MonthGrid(state.year, month, byDate, viewModel::selectDay)
            Text(stringResource(R.string.calendar_color_note), style = MaterialTheme.typography.bodySmall)
        }
    }
    selected?.let { date ->
        ModalBottomSheet(onDismissRequest = { viewModel.selectDay(null) }) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(date, style = MaterialTheme.typography.headlineMedium)
                val recap = detail.recap.takeIf { detail.date == date }
                if (detail.error && detail.date == date) ErrorStateCard(stringResource(R.string.calendar_day_error))
                else if (recap == null) LoadingStateCard(stringResource(R.string.calendar_loading_day))
                else {
                    Text(listeningTime(recap.overview.totalListeningMs), style = MaterialTheme.typography.displaySmall)
                    Text(stringResource(R.string.calendar_day_summary, recap.overview.totalPlays, recap.overview.uniqueTracks, recap.overview.uniqueArtists))
                    recap.artists.firstOrNull()?.let { artist ->
                        ListItem(headlineContent = { Text(artist.name) }, overlineContent = { Text(stringResource(R.string.home_top_artist)) },
                            leadingContent = { LocalArtwork(artist.name, round = true) }, modifier = Modifier.clickable { viewModel.selectDay(null); onArtist(artist.id) })
                    }
                    recap.tracks.firstOrNull()?.let { track ->
                        ListItem(headlineContent = { Text(track.name) }, overlineContent = { Text(stringResource(R.string.home_top_song)) },
                            leadingContent = { LocalArtwork(track.name) }, modifier = Modifier.clickable { viewModel.selectDay(null); onTrack(track.id) })
                    }
                    Button(onClick = { viewModel.selectDay(null); onDay(date) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.calendar_full_day)) }
                }
            }
        }
    }
}

@Composable
internal fun MonthGrid(year: Int, month: Int, days: Map<String, DailyListening>, onDay: (String) -> Unit) {
    val calendar = remember(year, month) { Calendar.getInstance().apply { clear(); set(year, month, 1) } }
    val offset = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val count = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val max = days.values.maxOfOrNull { it.listeningMs }?.coerceAtLeast(1) ?: 1
    val names = DateFormatSymbols.getInstance().shortWeekdays
    BoxWithConstraints {
        val gridWidth = maxOf(maxWidth, 336.dp)
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            Column(Modifier.width(gridWidth), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row {
                    listOf(2, 3, 4, 5, 6, 7, 1).forEach { day -> Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(names[day].take(2), style = MaterialTheme.typography.labelSmall) } }
                }
                repeat((offset + count + 6) / 7) { week ->
                    Row {
                        repeat(7) { weekday ->
                            val number = week * 7 + weekday - offset + 1
                            if (number !in 1..count) Spacer(Modifier.weight(1f).height(56.dp))
                            else {
                                val date = String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, number)
                                val value = days[date]
                                val ratio = (value?.listeningMs ?: 0).toFloat() / max
                                val color = lerp(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.primaryContainer, ratio)
                                val label = stringResource(R.string.calendar_day_accessibility, date, value?.plays ?: 0, listeningTime(value?.listeningMs ?: 0))
                                Box(Modifier.weight(1f).height(56.dp).clickable(role = Role.Button) { onDay(date) }
                                    .semantics { contentDescription = label }.padding(2.dp)
                                    .background(color, MaterialTheme.shapes.small), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(number.toString(), color = MaterialTheme.colorScheme.onSurface)
                                        if ((value?.plays ?: 0) > 0) Box(Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
