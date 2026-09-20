package com.davidegea.spotifystats.domain.analytics

import com.davidegea.spotifystats.domain.model.TimeRange
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DateRanges {
    /** Inclusive local calendar dates; Calendar.add handles DST days of 23/25 hours. */
    fun dates(from: String, to: String, zone: TimeZone = TimeZone.getDefault()): TimeRange {
        val start = parse(from, zone)
        val end = parse(to, zone)
        require(start <= end) { "Start date must not follow end date" }
        val next = Calendar.getInstance(zone).apply { timeInMillis = end; add(Calendar.DAY_OF_MONTH, 1) }
        return TimeRange(start, next.timeInMillis - 1)
    }

    fun previous(range: TimeRange): TimeRange? {
        if (range.fromInclusive == Long.MIN_VALUE || range.toInclusive == Long.MAX_VALUE) return null
        val length = range.toInclusive - range.fromInclusive + 1
        if (length <= 0 || range.fromInclusive < Long.MIN_VALUE + length) return null
        return TimeRange(range.fromInclusive - length, range.fromInclusive - 1)
    }

    fun parse(value: String, zone: TimeZone = TimeZone.getDefault()): Long {
        require(Regex("\\d{4}-\\d{2}-\\d{2}").matches(value)) { "Use YYYY-MM-DD" }
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false; timeZone = zone }
        val position = ParsePosition(0)
        val parsed = format.parse(value, position)
        require(parsed != null && position.index == value.length) { "Invalid calendar date" }
        require(value.substring(0, 4).toInt() in 1900..2200) { "Year must be between 1900 and 2200" }
        return parsed.time
    }
}
