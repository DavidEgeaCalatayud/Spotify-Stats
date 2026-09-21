package com.davidegea.spotifystats.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.domain.analytics.DateRanges
import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.TimeRange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DateRangeControls(
    period: AnalyticsPeriod,
    custom: TimeRange?,
    onPeriod: (AnalyticsPeriod) -> Unit,
    onCustom: (TimeRange) -> Unit,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnalyticsPeriod.entries.forEach { candidate ->
            FilterChip(
                selected = custom == null && period == candidate,
                onClick = { onPeriod(candidate) },
                label = { Text(stringResource(periodLabelRes(candidate))) },
            )
        }
        FilterChip(
            selected = custom != null,
            onClick = { showDialog = true },
            label = { Text(stringResource(R.string.period_custom_dates)) },
        )
    }
    if (custom != null) {
        Text(
            rangeLabel(custom, stringResource(R.string.period_all_time)),
            style = MaterialTheme.typography.bodySmall,
        )
    }
    if (showDialog) {
        DateRangeDialog(
            onDismiss = { showDialog = false },
            onSelected = {
                onCustom(it)
                showDialog = false
            },
        )
    }
}

@Composable
fun DateRangeDialog(
    onDismiss: () -> Unit,
    onSelected: (TimeRange) -> Unit,
) {
    var from by rememberSaveable { mutableStateOf(isoDate(System.currentTimeMillis())) }
    var to by rememberSaveable { mutableStateOf(from) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.date_range_choose)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.date_range_body))
                OutlinedTextField(
                    value = from,
                    onValueChange = { from = it },
                    label = { Text(stringResource(R.string.date_range_from)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text(stringResource(R.string.date_range_to)) },
                    singleLine = true,
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        onSelected(DateRanges.dates(from, to))
                    } catch (exception: IllegalArgumentException) {
                        error = exception.message
                    }
                },
            ) {
                Text(stringResource(R.string.action_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@StringRes
fun periodLabelRes(period: AnalyticsPeriod): Int = when (period) {
    AnalyticsPeriod.TODAY -> R.string.period_today
    AnalyticsPeriod.LAST_6_MONTHS -> R.string.period_6_months
    AnalyticsPeriod.LAST_7_DAYS -> R.string.period_7_days
    AnalyticsPeriod.LAST_30_DAYS -> R.string.period_30_days
    AnalyticsPeriod.THIS_YEAR -> R.string.period_this_year
    AnalyticsPeriod.ALL_TIME -> R.string.period_all_time
}

fun isoDate(value: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date(value))

fun rangeLabel(
    range: TimeRange,
    allTimeLabel: String = "All time",
): String = if (range.fromInclusive == Long.MIN_VALUE) {
    allTimeLabel
} else {
    isoDate(range.fromInclusive) + " – " + isoDate(range.toInclusive)
}

fun listeningTime(ms: Long): String =
    (ms / 3_600_000).toString() + "h " + ((ms % 3_600_000) / 60_000).toString() + "m"
