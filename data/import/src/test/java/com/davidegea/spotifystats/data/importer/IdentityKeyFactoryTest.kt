package com.davidegea.spotifystats.data.importer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class IdentityKeyFactoryTest {

    private val keys = IdentityKeyFactory()

    @Test
    fun normalizationIsStableAcrossCaseWhitespaceAndUnicodeCompatibility() {
        assertEquals(
            keys.artist("  The   Weeknd "),
            keys.artist("the weeknd"),
        )
    }

    @Test
    fun spotifyUriDominatesTrackFallbackIdentity() {
        val artist = keys.artist("The Weeknd")
        val album = keys.album(artist, "After Hours")

        assertEquals(
            keys.track("spotify:track:123", artist, album, "After Hours"),
            keys.track("spotify:track:123", artist, null, "Different name"),
        )
    }

    @Test
    fun eventFingerprintChangesWithPlaybackIdentity() {
        val first = keys.event("track-a", 1_000L, 30_000L)
        val second = keys.event("track-a", 2_000L, 30_000L)

        assertNotEquals(first, second)
    }
}
