package com.davidegea.spotifystats.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.domain.model.DailyListening

@Composable
fun ActivityChart(days: List<DailyListening>) {
    val visible = days.takeLast(30)
    if (visible.isEmpty()) return
    val color = MaterialTheme.colorScheme.primary
    val maximum = visible.maxOf { it.listeningMs }.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Listening activity · last ${visible.size} active days", style = MaterialTheme.typography.titleMedium)
        Canvas(Modifier.fillMaxWidth().height(100.dp).semantics {
            contentDescription = visible.joinToString("; ") { "${it.date}: ${listeningTime(it.listeningMs)}, ${it.plays} events" }
        }) {
            val step = size.width / visible.size
            visible.forEachIndexed { index, day ->
                val height = size.height * (day.listeningMs.toFloat() / maximum)
                drawRect(color, Offset(index * step, size.height - height), Size(step * 0.75f, height))
            }
        }
        Text("${visible.first().date} – ${visible.last().date}", style = MaterialTheme.typography.bodySmall)
    }
}
