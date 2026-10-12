package com.davidegea.spotifystats.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.domain.model.DailyListening
import com.davidegea.spotifystats.domain.model.TimeRange
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ActivityChart(
    days: List<DailyListening>,
    range: TimeRange? = null,
    previousDays: List<DailyListening>? = null,
    previousRange: TimeRange? = null,
) {
    val visible = remember(days, range) { chartDays(days, range) }
    if (visible.isEmpty()) return
    val previous = remember(previousDays, previousRange) { chartDays(previousDays.orEmpty(), previousRange) }
    var compare by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable(visible.first().date, visible.last().date) { mutableIntStateOf(visible.lastIndex) }
    var entered by remember(visible) { mutableStateOf(false) }
    LaunchedEffect(visible) { entered = true }
    val reveal by animateFloatAsState(if (entered) 1f else 0f, tween(300), label = "activity")
    val primary = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val comparison = MaterialTheme.colorScheme.tertiary
    val maximum = (visible + if (compare) previous else emptyList()).maxOf { it.listeningMs }.coerceAtLeast(1)
    val selectedDay = visible[selected.coerceIn(visible.indices)]
    val selectionLabel = stringResource(R.string.chart_selection, selectedDay.date, selectedDay.plays, listeningTime(selectedDay.listeningMs))
    val comparisonLabel = stringResource(R.string.chart_compare)
    val chartLabel = stringResource(R.string.chart_accessibility, visible.first().date, visible.last().date)
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading(stringResource(R.string.chart_title), stringResource(R.string.chart_days, visible.size))
            if (previous.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = compare, onCheckedChange = { compare = it }, modifier = Modifier.semantics { contentDescription = comparisonLabel })
                    Text(stringResource(R.string.chart_compare), Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (compare) {
                Text(stringResource(R.string.chart_comparison_legend), style = MaterialTheme.typography.bodySmall)
                previous.getOrNull(previous.size - visible.size + selected)?.let { prior ->
                    Text(stringResource(R.string.chart_previous_value, prior.date, listeningTime(prior.listeningMs)), style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(selectionLabel, style = MaterialTheme.typography.labelLarge, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            Text(stringResource(R.string.chart_daily_average, listeningTime(visible.sumOf { it.listeningMs } / visible.size)),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Canvas(Modifier.fillMaxWidth().height(148.dp).semantics { contentDescription = chartLabel }
                .pointerInput(visible) { detectTapGestures { offset -> selected = (offset.x / size.width * visible.size).toInt().coerceIn(visible.indices) } }) {
                val height = size.height - 6.dp.toPx()
                repeat(4) { line ->
                    val y = height * line / 3f
                    drawLine(grid.copy(alpha = 0.5f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
                val step = size.width / visible.size
                val average = visible.sumOf { it.listeningMs }.toFloat() / visible.size
                val averageY = height - height * (average / maximum) * reveal
                drawLine(
                    grid, Offset(0f, averageY), Offset(size.width, averageY),
                    1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx())),
                )
                val pulse = Path()
                visible.forEachIndexed { index, day ->
                    val point = Offset((index + 0.5f) * step, height - height * day.listeningMs / maximum.toFloat() * reveal)
                    if (index == 0) pulse.moveTo(point.x, point.y) else pulse.lineTo(point.x, point.y)
                }
                visible.forEachIndexed { index, day ->
                    val barHeight = (height * day.listeningMs.toFloat() / maximum * reveal).coerceAtLeast(2.dp.toPx())
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            0f to primary.copy(alpha = if (index == selected) 0.90f else 0.45f),
                            1f to primary.copy(alpha = if (index == selected) 0.48f else 0.14f),
                            startY = height - barHeight, endY = height,
                        ),
                        topLeft = Offset(index * step + step * 0.20f, height - barHeight),
                        size = Size(step * 0.60f, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )
                    if (compare) {
                        val prior = previous.getOrNull(previous.size - visible.size + index)?.listeningMs ?: 0
                        val y = height - height * prior.toFloat() / maximum * reveal
                        drawLine(comparison, Offset(index * step + step * 0.15f, y), Offset(index * step + step * 0.85f, y), 2.dp.toPx(), StrokeCap.Round)
                    }
                }
                drawPath(pulse, primary, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                val picked = visible[selected.coerceIn(visible.indices)]
                drawCircle(
                    primary,
                    radius = 5.dp.toPx(),
                    center = Offset((selected + 0.5f) * step, height - height * picked.listeningMs / maximum.toFloat() * reveal),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(0, 7, 14, 21, visible.lastIndex).filter { it <= visible.lastIndex }.distinct().sorted().forEach { index ->
                    Text(shortChartDate(visible[index].date), style = MaterialTheme.typography.labelSmall)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { selected = (selected - 1).coerceAtLeast(0) }, enabled = selected > 0) { Text(stringResource(R.string.chart_previous_day)) }
                TextButton(onClick = { selected = (selected + 1).coerceAtMost(visible.lastIndex) }, enabled = selected < visible.lastIndex) { Text(stringResource(R.string.chart_next_day)) }
            }
            val peak = visible.maxBy { it.listeningMs }
            val quietest = visible.minBy { it.listeningMs }
            Text(stringResource(R.string.chart_peak, shortChartDate(peak.date), listeningTime(peak.listeningMs)), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.chart_minimum, shortChartDate(quietest.date), listeningTime(quietest.listeningMs)), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun shortChartDate(date: String): String = SimpleDateFormat("d MMM", Locale.getDefault()).format(
    java.util.Date(com.davidegea.spotifystats.domain.analytics.DateRanges.parse(date)),
)
