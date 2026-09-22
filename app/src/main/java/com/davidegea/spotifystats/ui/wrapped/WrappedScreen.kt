package com.davidegea.spotifystats.ui.wrapped

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.designsystem.StatsPalette
import com.davidegea.spotifystats.domain.analytics.DateRanges
import com.davidegea.spotifystats.domain.model.Recap
import com.davidegea.spotifystats.ui.components.*
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun WrappedRoute(onBack: () -> Unit, viewModel: WrappedViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var year by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var month by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var customStart by rememberSaveable { mutableStateOf<Long?>(null) }
    var customEnd by rememberSaveable { mutableStateOf<Long?>(null) }
    var dates by rememberSaveable { mutableStateOf(false) }
    var showStories by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(year, month, mode, customStart, customEnd) {
        if (mode == 2 && customStart != null && customEnd != null) {
            viewModel.selectCustom(com.davidegea.spotifystats.domain.model.TimeRange(requireNotNull(customStart), requireNotNull(customEnd)))
        }
        if (mode == 0) viewModel.selectCustom(DateRanges.dates("$year-01-01", "$year-12-31"))
        if (mode == 1) {
            val last = Calendar.getInstance().apply { clear(); set(year, month, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
            viewModel.selectCustom(DateRanges.dates(String.format(Locale.ROOT, "%04d-%02d-01", year, month + 1), String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, last)))
        }
    }
    val recap = state.recap
    if (showStories && recap != null) {
        WrappedStories(recap, onClose = { showStories = false })
    } else StatsPage(stringResource(R.string.wrapped_title), onBack) {
        HeroSurface {
            LocalArtwork("Your music story", size = 104.dp)
            Text(stringResource(R.string.wrapped_relive), style = MaterialTheme.typography.displaySmall)
            Text(stringResource(R.string.wrapped_body), style = MaterialTheme.typography.bodyLarge)
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(mode == 0, { mode = 0 }, label = { Text(stringResource(R.string.wrapped_year_recap)) })
            FilterChip(mode == 1, { mode = 1 }, label = { Text(stringResource(R.string.wrapped_month_recap)) })
            FilterChip(mode == 2, { dates = true }, label = { Text(stringResource(R.string.period_custom_dates)) })
        }
        if (mode != 2) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { year-- }, enabled = year > 1900) { Text(stringResource(R.string.action_previous)) }
            Text(year.toString(), style = MaterialTheme.typography.headlineLarge)
            TextButton(onClick = { year++ }, enabled = year < 2200) { Text(stringResource(R.string.action_next)) }
        }
        if (mode == 1) Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateFormatSymbols.getInstance().shortMonths.take(12).forEachIndexed { index, label ->
                FilterChip(month == index, { month = index }, label = { Text(label) })
            }
        }
        state.customRange?.let { Text(rangeLabel(it), style = MaterialTheme.typography.titleMedium) }
        when {
            state.error != null -> ErrorStateCard(stringResource(R.string.wrapped_read_error))
            recap == null -> LoadingStateCard(stringResource(R.string.state_loading))
            recap.overview.totalPlays == 0L -> EmptyStateCard(stringResource(R.string.home_empty_period_title), stringResource(R.string.wrapped_empty))
            else -> {
                Text(stringResource(R.string.wrapped_time_songs, listeningTime(recap.overview.totalListeningMs), recap.overview.uniqueTracks))
                Button(onClick = { showStories = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.wrapped_open_stories)) }
            }
        }
    }
    if (dates) DateRangeDialog(onDismiss = { dates = false }, onSelected = { customStart = it.fromInclusive; customEnd = it.toInclusive; mode = 2; viewModel.selectCustom(it); dates = false }, initialRange = state.customRange)
}

@Composable
internal fun WrappedStories(recap: Recap, onClose: () -> Unit) {
    val pager = rememberPagerState { 4 }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var busy by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf(false) }
    BackHandler { onClose() }
    val palettes = listOf(
        listOf(Color(0xFF0A1E24), Color(0xFF20463E)),
        listOf(Color(0xFF161831), Color(0xFF352A5C)),
        listOf(Color(0xFF2B1425), Color(0xFF5B2645)),
        listOf(Color(0xFF2F1F0C), Color(0xFF61451B)),
    )
    val pageLabel = stringResource(R.string.wrapped_page, pager.currentPage + 1, 4)
    Surface(Modifier.fillMaxSize(), color = palettes[pager.currentPage].first(), contentColor = Color.White) {
        Column(Modifier.background(Brush.linearGradient(palettes[pager.currentPage])).padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { index -> LinearProgressIndicator(progress = { if (index <= pager.currentPage) 1f else 0f },
                    modifier = Modifier.weight(1f), color = StatsPalette.mint, trackColor = Color.White.copy(alpha = 0.2f)) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(pageLabel, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, stringResource(R.string.action_close), tint = Color.White) }
            }
            HorizontalPager(state = pager, modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) { page ->
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    when (page) {
                        0 -> {
                            Text(stringResource(R.string.wrapped_card_overview), color = StatsPalette.mint, style = MaterialTheme.typography.labelLarge)
                            Text(rangeLabel(recap.range, stringResource(R.string.period_all_time)))
                            AnimatedMetric(recap.overview.totalPlays)
                            Text(stringResource(R.string.wrapped_card_recorded_events), style = MaterialTheme.typography.titleLarge)
                            Text(listeningTime(recap.overview.totalListeningMs), style = MaterialTheme.typography.displaySmall, color = StatsPalette.mint)
                            Text(stringResource(R.string.wrapped_card_songs_artists, recap.overview.uniqueTracks, recap.overview.uniqueArtists))
                            Text(stringResource(R.string.wrapped_card_discoveries_count, recap.discoveries))
                        }
                        1 -> {
                            val track = recap.tracks.firstOrNull()
                            Text(stringResource(R.string.wrapped_card_top_song_story), color = StatsPalette.mint)
                            LocalArtwork(track?.name.orEmpty(), size = 160.dp)
                            Text(track?.name ?: "—", style = MaterialTheme.typography.displaySmall)
                            track?.artistName?.let { Text(it, style = MaterialTheme.typography.titleLarge, color = StatsPalette.lilac) }
                            Text(stringResource(R.string.plays_and_time, track?.plays ?: 0L, listeningTime(track?.listeningMs ?: 0)), style = MaterialTheme.typography.titleMedium)
                        }
                        2 -> {
                            val artist = recap.artists.firstOrNull()
                            Text(stringResource(R.string.wrapped_card_top_artist_story), color = StatsPalette.mint)
                            LocalArtwork(artist?.name.orEmpty(), size = 160.dp, round = true)
                            Text(artist?.name ?: "—", style = MaterialTheme.typography.displaySmall)
                            Text(stringResource(R.string.plays_and_time, artist?.plays ?: 0L, listeningTime(artist?.listeningMs ?: 0)), style = MaterialTheme.typography.titleMedium)
                        }
                        else -> {
                            Text(stringResource(R.string.wrapped_card_discovery_story), color = StatsPalette.mint)
                            AnimatedMetric(recap.discoveries)
                            Text(stringResource(R.string.wrapped_card_discoveries_label), style = MaterialTheme.typography.titleLarge)
                            Text(stringResource(R.string.wrapped_card_top_album_story, recap.albums.firstOrNull()?.name ?: "—"), style = MaterialTheme.typography.headlineMedium)
                            recap.albums.firstOrNull()?.artistName?.let { Text(it) }
                            Button(enabled = busy == 0, modifier = Modifier.fillMaxWidth(), onClick = {
                                busy = 1; error = false
                                scope.launch {
                                    try { RecapCardRenderer.shareSequence(context, recap) }
                                    catch (cancelled: CancellationException) { throw cancelled }
                                    catch (_: Exception) { error = true }
                                    finally { busy = 0 }
                                }
                            }) { Text(stringResource(if (busy == 1) R.string.wrapped_creating_sequence else R.string.wrapped_share_sequence)) }
                            OutlinedButton(enabled = busy == 0, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), onClick = {
                                busy = 2; error = false
                                scope.launch {
                                    try { RecapCardRenderer.share(context, recap) }
                                    catch (cancelled: CancellationException) { throw cancelled }
                                    catch (_: Exception) { error = true }
                                    finally { busy = 0 }
                                }
                            }) { Text(stringResource(if (busy == 2) R.string.wrapped_creating else R.string.wrapped_share_single)) }
                            if (error) Text(stringResource(R.string.wrapped_share_error))
                            Text(stringResource(R.string.wrapped_share_sequence_note), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(stringResource(R.string.wrapped_card_brand), color = StatsPalette.mint, style = MaterialTheme.typography.labelSmall)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(enabled = pager.currentPage > 0, onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) {
                    Text(stringResource(R.string.action_previous))
                }
                TextButton(enabled = pager.currentPage < 3, onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) {
                    Text(stringResource(R.string.action_next))
                }
            }
        }
    }
}
