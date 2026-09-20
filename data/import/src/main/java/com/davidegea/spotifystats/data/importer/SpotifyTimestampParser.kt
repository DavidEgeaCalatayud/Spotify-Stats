package com.davidegea.spotifystats.data.importer

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class SpotifyTimestampParser {

    fun parseEpochMillis(value: String): Long? {
        for (pattern in PATTERNS) {
            val parser = SimpleDateFormat(pattern, Locale.ROOT).apply {
                isLenient = false
                timeZone = UTC
            }
            val position = ParsePosition(0)
            val date = parser.parse(value, position)
            if (date != null && position.index == value.length) {
                return date.time
            }
        }
        return null
    }

    private companion object {
        val UTC: TimeZone = TimeZone.getTimeZone("UTC")
        val PATTERNS = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ssX",
        )
    }
}
