package com.davidegea.spotifystats.data.importer

import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpotifyExtendedHistoryParserTest {

    @Test
    fun parsesTopLevelArrayLazilyAndIgnoresPrivacyFields() {
        val json = """
            [
              {
                "ts": "2026-09-20T09:43:22Z",
                "platform": "Android OS",
                "ms_played": 214823,
                "conn_country": "ES",
                "ip_addr_decrypted": "203.0.113.42",
                "user_agent_decrypted": "private-user-agent",
                "master_metadata_track_name": "After Hours",
                "master_metadata_album_artist_name": "The Weeknd",
                "master_metadata_album_album_name": "After Hours",
                "spotify_track_uri": "spotify:track:test",
                "reason_start": "trackdone",
                "reason_end": "trackdone",
                "shuffle": false,
                "skipped": false,
                "offline": false,
                "incognito_mode": false
              },
              {
                "ts": "2026-09-20T10:00:00Z",
                "ms_played": 1000,
                "episode_name": "Podcast episode",
                "episode_show_name": "Podcast show"
              }
            ]
        """.trimIndent()

        val records = SpotifyExtendedHistoryParser()
            .parse(ByteArrayInputStream(json.toByteArray()))
            .toList()

        assertEquals(2, records.size)
        assertEquals("After Hours", records.first().trackName)
        assertEquals(214823L, records.first().msPlayed)
        assertNull(records[1].trackName)
    }
}
