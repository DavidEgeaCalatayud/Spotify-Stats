package com.davidegea.spotifystats.data.importer

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class SpotifyExtendedHistoryParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    },
) {
    /**
     * Streams one top-level Spotify history object at a time.
     *
     * A syntactically isolated object that cannot be decoded is emitted as null so callers can
     * count it as a skipped record and continue with later entries. Broken top-level JSON remains
     * a document failure.
     */
    fun parse(input: InputStream): Sequence<SpotifyExtendedHistoryRecord?> = sequence {
        val reader = BufferedReader(InputStreamReader(input, Charsets.UTF_8))
        val opening = readNextNonWhitespace(reader)
        if (opening != '['.code) {
            throw SerializationException("Spotify history must be a top-level JSON array")
        }

        var hasRecord = false
        var expectingRecord = true

        while (true) {
            val token = readNextNonWhitespace(reader)
            if (token == -1) {
                throw SerializationException("Unexpected end of Spotify history array")
            }

            if (expectingRecord) {
                if (token == ']'.code) {
                    if (hasRecord) {
                        throw SerializationException("Trailing comma in Spotify history array")
                    }
                    break
                }
                if (token != '{'.code) {
                    throw SerializationException("Expected a Spotify history object")
                }

                val rawRecord = readObject(reader)
                if (rawRecord == null) {
                    yield(null)
                } else {
                    val record = try {
                        json.decodeFromString<SpotifyExtendedHistoryRecord>(rawRecord)
                    } catch (_: SerializationException) {
                        null
                    } catch (_: IllegalArgumentException) {
                        null
                    }
                    yield(record)
                }
                hasRecord = true
                expectingRecord = false
            } else {
                when (token) {
                    ','.code -> expectingRecord = true
                    ']'.code -> break
                    else -> throw SerializationException(
                        "Expected ',' or ']' after Spotify history record",
                    )
                }
            }
        }

        if (readNextNonWhitespace(reader) != -1) {
            throw SerializationException("Unexpected data after Spotify history array")
        }
    }

    private fun readObject(reader: BufferedReader): String? {
        val builder = StringBuilder().append('{')
        var depth = 1
        var inString = false
        var escaped = false
        var oversized = false

        while (depth > 0) {
            val value = reader.read()
            if (value == -1) {
                throw SerializationException("Truncated Spotify history record")
            }

            val char = value.toChar()
            if (!oversized) {
                builder.append(char)
                if (builder.length > MAX_RECORD_CHARS) {
                    oversized = true
                    builder.setLength(0)
                }
            }

            if (inString) {
                when {
                    escaped -> escaped = false
                    char == '\\' -> escaped = true
                    char == '"' -> inString = false
                }
                continue
            }

            when (char) {
                '"' -> inString = true
                '{' -> depth++
                '}' -> depth--
            }
        }

        return if (oversized) null else builder.toString()
    }

    private fun readNextNonWhitespace(reader: BufferedReader): Int {
        while (true) {
            val value = reader.read()
            if (value == -1) return -1
            val char = value.toChar()
            if (!char.isWhitespace() && char != '\uFEFF') return value
        }
    }

    private companion object {
        const val MAX_RECORD_CHARS = 1_048_576
    }
}
