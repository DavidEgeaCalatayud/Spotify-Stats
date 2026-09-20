package com.davidegea.spotifystats.data.importer

import java.io.InputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence

class SpotifyExtendedHistoryParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    },
) {
    @OptIn(ExperimentalSerializationApi::class)
    fun parse(input: InputStream): Sequence<SpotifyExtendedHistoryRecord> =
        json.decodeToSequence(
            stream = input,
            format = DecodeSequenceMode.ARRAY_WRAPPED,
        )
}
