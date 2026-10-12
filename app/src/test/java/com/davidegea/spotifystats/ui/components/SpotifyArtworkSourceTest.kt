package com.davidegea.spotifystats.ui.components

import org.junit.Assert.*
import org.junit.Test

class SpotifyArtworkSourceTest {
    @Test fun validSpotifyUrisAreAccepted() {
        assertEquals("0VjIjW4GlUZAMYd2vXMi3b",
            SpotifyArtworkSource.trackId("spotify:track:0VjIjW4GlUZAMYd2vXMi3b"))
        assertEquals("0VjIjW4GlUZAMYd2vXMi3b",
            SpotifyArtworkSource.trackId("https://open.spotify.com/track/0VjIjW4GlUZAMYd2vXMi3b?si=abc"))
    }

    @Test fun invalidUrisAreRejectedBeforeRequest() {
        listOf(null, "", "spotify:album:0VjIjW4GlUZAMYd2vXMi3b", "spotify:track:../../etc/passwd",
            "https://other.example/track/0VjIjW4GlUZAMYd2vXMi3b").forEach {
            assertNull(SpotifyArtworkSource.trackId(it))
        }
    }
}
