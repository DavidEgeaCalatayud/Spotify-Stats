package com.davidegea.spotifystats.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.designsystem.StatsPalette
import com.davidegea.spotifystats.designsystem.StatsSpacing
import java.text.NumberFormat

@Composable
fun ContentContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = StatsSpacing.contentWidth).fillMaxSize()) { content() }
    }
}

@Composable
fun StatsPage(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    resetScrollKey: Any? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(resetScrollKey) { if (resetScrollKey != null) scrollState.scrollTo(0) }
    ContentContainer {
        Column(Modifier.fillMaxSize()) {
            if (title != null && onBack != null) StatsTopBar(title, onBack)
            Column(
                Modifier.weight(1f).verticalScroll(scrollState).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(StatsSpacing.xl),
                content = content,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
            }
        },
        windowInsets = WindowInsets(0, 0, 0, 0),
    )
}

@Composable
fun SectionHeading(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun HeroSurface(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = StatsPalette.ink,
        contentColor = Color.White,
    ) {
        Column(
            Modifier
                .background(Brush.linearGradient(listOf(StatsPalette.ink, StatsPalette.forest)))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

/** Stable, decorative artwork generated on-device. Never implies a Spotify album cover. */
@Composable
fun LocalArtwork(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    round: Boolean = false,
) {
    val hash = remember(name) { name.lowercase(java.util.Locale.ROOT).hashCode() and Int.MAX_VALUE }
    val color = StatsPalette.artwork[hash % StatsPalette.artwork.size]
    val initials = remember(name) {
        name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2)
            .joinToString("") { it.take(1) }.uppercase(java.util.Locale.ROOT).ifBlank { "♪" }
    }
    Box(
        modifier
            .size(size)
            .clip(if (round) CircleShape else MaterialTheme.shapes.medium)
            .background(Brush.linearGradient(listOf(color, StatsPalette.ink)))
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(this.size.width * 0.72f, this.size.height * 0.25f)
            for (ring in 1..4) {
                drawCircle(
                    Color.White.copy(alpha = 0.10f),
                    this.size.width * (0.13f * ring),
                    center,
                    style = Stroke(this.size.width * 0.055f),
                )
            }
            drawCircle(
                StatsPalette.mint.copy(alpha = 0.16f),
                this.size.width * 0.36f,
                Offset(0f, this.size.height),
            )
        }
        Text(
            initials,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = if (size >= 100.dp) MaterialTheme.typography.displaySmall
            else MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun EntityHero(
    eyebrow: String,
    title: String,
    subtitle: String?,
    primaryValue: String,
    primaryLabel: String,
    secondaryValue: String,
    secondaryLabel: String,
    roundArtwork: Boolean,
) {
    val largeText = LocalConfiguration.current.fontScale > 1.3f
    HeroSurface {
        Text(
            eyebrow.uppercase(java.util.Locale.getDefault()),
            style = MaterialTheme.typography.labelLarge,
            color = StatsPalette.mint,
        )
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val compact = maxWidth < 360.dp || largeText
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LocalArtwork(
                        title,
                        size = if (largeText) 88.dp else 104.dp,
                        round = roundArtwork,
                    )
                    EntityTitle(title, subtitle)
                }
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LocalArtwork(title, size = 104.dp, round = roundArtwork)
                    EntityTitle(title, subtitle, Modifier.weight(1f))
                }
            }
        }
        HorizontalDivider(color = StatsPalette.mint.copy(alpha = 0.22f))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth < 320.dp || largeText) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeroMetric(primaryValue, primaryLabel)
                    HeroMetric(secondaryValue, secondaryLabel)
                }
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    HeroMetric(primaryValue, primaryLabel, Modifier.weight(1f))
                    HeroMetric(secondaryValue, secondaryLabel, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EntityTitle(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HeroMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            color = StatsPalette.mint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.72f),
        )
    }
}

@Composable
fun MetricBarRow(
    label: String,
    value: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
        }
    }
}

@Composable
fun AnimatedMetric(value: Long, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = value,
        transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(180)) },
        label = "metric",
        modifier = modifier,
    ) { number ->
        Text(
            NumberFormat.getIntegerInstance().format(number),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
