package com.davidegea.spotifystats.ui.insights

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.domain.model.ListeningHeatmapCell
import com.davidegea.spotifystats.ui.components.listeningTime

private val weekdayOrder = listOf(1, 2, 3, 4, 5, 6, 0)

@Composable
internal fun ListeningHeatmap(cells: List<ListeningHeatmapCell>) {
    val values = remember(cells) { cells.associateBy { it.weekday to it.hour } }
    val peak = cells.maxByOrNull { it.listeningMs }
    val maximum = peak?.listeningMs?.coerceAtLeast(1) ?: 1
    var weekday by rememberSaveable(cells) { mutableIntStateOf(peak?.weekday ?: 1) }
    var hour by rememberSaveable(cells) { mutableIntStateOf(peak?.hour ?: 12) }
    var entered by remember(cells) { mutableStateOf(false) }
    LaunchedEffect(cells) { entered = true }
    val reveal by animateFloatAsState(if (entered) 1f else 0f, tween(280), label = "heatmap")
    val low = MaterialTheme.colorScheme.surfaceContainerHigh
    val high = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.onSurface
    val description = stringResource(R.string.heatmap_grid_description)
    val selected = values[weekday to hour]
    val selectedLabel = stringResource(R.string.heatmap_selection, weekdayLabel(weekday), hourLabel(hour), listeningTime(selected?.listeningMs ?: 0), selected?.plays ?: 0)
    val hourDescription = stringResource(R.string.heatmap_hour)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth().padding(start = 36.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(0, 6, 12, 18, 23).forEach { Text(hourLabel(it).take(2), style = MaterialTheme.typography.labelSmall) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.width(28.dp)) {
                    weekdayOrder.forEach { Text(weekdayLabel(it).take(2), Modifier.height(28.dp), style = MaterialTheme.typography.labelSmall) }
                }
                Canvas(Modifier.weight(1f).height(196.dp).semantics { contentDescription = description }
                    .pointerInput(cells) { detectTapGestures { position ->
                        hour = (position.x / size.width * 24).toInt().coerceIn(0, 23)
                        weekday = weekdayOrder[(position.y / size.height * 7).toInt().coerceIn(0, 6)]
                    } }) {
                    val w = size.width / 24
                    val h = size.height / 7
                    weekdayOrder.forEachIndexed { row, day ->
                        repeat(24) { column ->
                            val ratio = (values[day to column]?.listeningMs ?: 0).toFloat() / maximum
                            val pos = Offset(column * w + 1.dp.toPx(), row * h + 2.dp.toPx())
                            val cellSize = Size((w - 2.dp.toPx()).coerceAtLeast(1f), h - 4.dp.toPx())
                            drawRoundRect(lerp(low, high, ratio * reveal), pos, cellSize, CornerRadius(3.dp.toPx()))
                            if (day == weekday && column == hour) drawRoundRect(outline, pos, cellSize, CornerRadius(3.dp.toPx()), style = Stroke(2.dp.toPx()))
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.heatmap_less), style = MaterialTheme.typography.labelSmall)
                repeat(5) { Box(Modifier.size(16.dp).background(lerp(low, high, it / 4f), MaterialTheme.shapes.small)) }
                Text(stringResource(R.string.heatmap_more), style = MaterialTheme.typography.labelSmall)
            }
            Text(selectedLabel, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            // Full-size controls provide the same selection to TalkBack, keyboard and large-text users.
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                weekdayOrder.forEach { day -> FilterChip(selected = weekday == day, onClick = { weekday = day }, label = { Text(weekdayLabel(day)) }) }
            }
            Slider(value = hour.toFloat(), onValueChange = { hour = it.toInt() }, valueRange = 0f..23f, steps = 22,
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = hourDescription; stateDescription = hourLabel(hour) })
        }
    }
}
