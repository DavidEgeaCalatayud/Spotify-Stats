package com.davidegea.spotifystats.data.importer

import java.io.ByteArrayInputStream
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
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
        assertEquals("After Hours", records.first()?.trackName)
        assertEquals(214823L, records.first()?.msPlayed)
        assertNull(records[1]?.trackName)
    }

    @Test
    fun malformedRecordDoesNotPreventLaterRecordsFromBeingParsed() {
        val json = """
            [
              {
                "ts":"2026-09-20T12:00:00Z",
                "ms_played":30000,
                "master_metadata_track_name":"First",
                "master_metadata_album_artist_name":"Artist"
              },
              {
                "ts":"2026-09-20T12:01:00Z",
                "ms_played":"not-a-number",
                "master_metadata_track_name":"Broken",
                "master_metadata_album_artist_name":"Artist"
              },
              {
                "ts":"2026-09-20T12:02:00Z",
                "ms_played":45000,
                "master_metadata_track_name":"Last",
                "master_metadata_album_artist_name":"Artist"
              }
            ]
        """.trimIndent()

        val records = SpotifyExtendedHistoryParser()
            .parse(ByteArrayInputStream(json.toByteArray()))
            .toList()

        assertEquals(3, records.size)
        assertEquals("First", records[0]?.trackName)
        assertNull(records[1])
        assertEquals("Last", records[2]?.trackName)
    }

    @Test
    fun rejectsNonArrayDocuments() {
        try {
            SpotifyExtendedHistoryParser()
                .parse(ByteArrayInputStream("""{"not":"history"}""".toByteArray()))
                .toList()
            fail("Expected a document-level parse failure")
        } catch (_: SerializationException) {
            // expected
        }
    }
    @Test
    fun rejectsMissingTopLevelCommaEvenWhenBothObjectsAreIndividuallyValid() {
        val invalid = """[
          {"ts":"2026-09-20T12:00:00Z","ms_played":1000}
          {"ts":"2026-09-20T12:01:00Z","ms_played":2000}
        ]""".trimIndent()

        try {
            SpotifyExtendedHistoryParser()
                .parse(ByteArrayInputStream(invalid.toByteArray()))
                .toList()
            fail("Expected strict top-level JSON validation")
        } catch (_: SerializationException) {
            // expected
        }
    }

}
