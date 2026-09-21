package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.AnalyticsPeriod
import com.davidegea.spotifystats.domain.model.TimeRange
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class AnalyticsTimeRangeResolver(
    private val nowProvider: () -> Long = System::currentTimeMillis,
    private val timeZone: TimeZone = TimeZone.getDefault(),
) {

    fun resolve(period: AnalyticsPeriod): TimeRange {
        val now = nowProvider()

        return when (period) {
            AnalyticsPeriod.TODAY -> {
                val calendar = Calendar.getInstance(timeZone).apply {
                    timeInMillis = now
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                TimeRange(calendar.timeInMillis, now)
            }
            AnalyticsPeriod.LAST_6_MONTHS -> {
                val calendar = Calendar.getInstance(timeZone).apply { timeInMillis = now; add(Calendar.MONTH, -6) }
                TimeRange(calendar.timeInMillis, now)
            }
            AnalyticsPeriod.LAST_7_DAYS -> TimeRange(
                fromInclusive = now - TimeUnit.DAYS.toMillis(7),
                toInclusive = now,
            )
            AnalyticsPeriod.LAST_30_DAYS -> TimeRange(
                fromInclusive = now - TimeUnit.DAYS.toMillis(30),
                toInclusive = now,
            )
            AnalyticsPeriod.THIS_YEAR -> {
                val calendar = Calendar.getInstance(timeZone).apply {
                    timeInMillis = now
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                TimeRange(
                    fromInclusive = calendar.timeInMillis,
                    toInclusive = now,
                )
            }
            AnalyticsPeriod.ALL_TIME -> TimeRange(
                fromInclusive = Long.MIN_VALUE,
                toInclusive = Long.MAX_VALUE,
            )
        }
    }
}
