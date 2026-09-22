package com.davidegea.spotifystats.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import java.util.TimeZone
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
    val haptic = LocalHapticFeedback.current
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(AnalyticsPeriod.TODAY, AnalyticsPeriod.LAST_7_DAYS, AnalyticsPeriod.LAST_30_DAYS, AnalyticsPeriod.LAST_6_MONTHS, AnalyticsPeriod.THIS_YEAR, AnalyticsPeriod.ALL_TIME).forEach { candidate ->
            FilterChip(
                selected = custom == null && period == candidate,
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onPeriod(candidate) },
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
            initialRange = custom,
            onDismiss = { showDialog = false },
            onSelected = {
                onCustom(it)
                showDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeDialog(
    onDismiss: () -> Unit,
    onSelected: (TimeRange) -> Unit,
    initialRange: TimeRange? = null,
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialRange?.let { pickerMillis(it.fromInclusive) },
        initialSelectedEndDateMillis = initialRange?.let { pickerMillis(it.toInclusive) },
        yearRange = 1900..2200,
    )
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.widthIn(max = 600.dp).fillMaxWidth().fillMaxHeight(0.95f), shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(12.dp)) {
                Text(stringResource(R.string.date_range_choose), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(12.dp))
                DateRangePicker(state, Modifier.weight(1f), title = null, showModeToggle = true)
                Text(stringResource(R.string.date_range_body), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                    TextButton(
                        enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
                        onClick = {
                            val start = state.selectedStartDateMillis
                            val end = state.selectedEndDateMillis
                            if (start != null && end != null) onSelected(pickerRange(start, end))
                        },
                    ) { Text(stringResource(R.string.action_apply)) }
                }
            }
        }
    }
}

/** Material pickers use UTC dates; analytics uses inclusive local days (including DST). */
internal fun pickerRange(start: Long, end: Long): TimeRange = DateRanges.dates(pickerIso(start), pickerIso(end))

private fun pickerIso(value: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}.format(Date(value))

private fun pickerMillis(value: Long): Long = DateRanges.parse(isoDate(value), TimeZone.getTimeZone("UTC"))

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
