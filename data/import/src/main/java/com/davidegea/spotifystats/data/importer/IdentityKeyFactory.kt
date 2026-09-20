package com.davidegea.spotifystats.data.importer

import java.security.MessageDigest
import java.text.Normalizer
import java.util.Locale

class IdentityKeyFactory {

    fun normalize(value: String): String =
        Normalizer.normalize(value.trim(), Normalizer.Form.NFKC)
            .lowercase(Locale.ROOT)
            .replace(WHITESPACE, " ")

    fun artist(name: String): String =
        sha256("artist|" + normalize(name))

    fun album(artistIdentity: String, albumName: String): String =
        sha256("album|" + artistIdentity + "|" + normalize(albumName))

    fun track(
        spotifyUri: String?,
        artistIdentity: String,
        albumIdentity: String?,
        trackName: String,
    ): String {
        val uri = spotifyUri?.trim()?.takeIf(String::isNotEmpty)
        return if (uri != null) {
            "spotify|" + uri
        } else {
            sha256(
                "track|" +
                    artistIdentity + "|" +
                    (albumIdentity ?: "no-album") + "|" +
                    normalize(trackName),
            )
        }
    }

    fun event(
        trackIdentity: String,
        playedAtEpochMs: Long,
        msPlayed: Long,
    ): String = sha256(
        "play|" + trackIdentity + "|" + playedAtEpochMs + "|" + msPlayed,
    )

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { byte -> "%02x".format(byte) }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
