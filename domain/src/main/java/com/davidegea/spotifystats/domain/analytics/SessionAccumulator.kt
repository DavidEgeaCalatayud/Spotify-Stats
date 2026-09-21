package com.davidegea.spotifystats.domain.analytics

import com.davidegea.spotifystats.domain.model.SessionStats

/** Streams intervals ordered by start. Export ts is the END of playback.
 * A break longer than 30 minutes starts a new inferred session. Durations are
 * summed listening time, not wall time including breaks. Zero-duration rows do not form sessions.
 */
class SessionAccumulator(private val gapMs: Long = 30 * 60_000L) {
    private var start = 0L
    private var end = 0L
    private var listening = 0L
    private var count = 0L
    private var total = 0L
    private var longest = 0L
    private var longestSpan = 0L

    init { require(gapMs >= 0) }

    fun add(startMs: Long, endMs: Long, listeningMs: Long) {
        require(endMs >= startMs && listeningMs >= 0)
        if (listeningMs == 0L) return
        if (count == 0L || startMs - end > gapMs) {
            count++
            start = startMs
            end = endMs
            listening = 0
        }
        end = maxOf(end, endMs)
        listening += listeningMs
        total += listeningMs
        longest = maxOf(longest, listening)
        longestSpan = maxOf(longestSpan, end - start)
    }

    fun result(): SessionStats = SessionStats(count, if (count == 0L) 0 else total / count, longest, longestSpan)
}
