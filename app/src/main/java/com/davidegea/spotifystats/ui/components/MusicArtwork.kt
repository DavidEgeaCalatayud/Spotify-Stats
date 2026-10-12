package com.davidegea.spotifystats.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Artwork is OPTIONAL: Spotify track IDs are sent only after the user opts in.
 * No image or metadata is written to Room or to the disk cache.
 * Spotify imagery must remain linked to its source and unmodified.
 */
@Composable
fun MusicArtwork(
    name: String,
    spotifyUri: String?,
    onlineEnabled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    round: Boolean = false,
) {
    val trackId = remember(spotifyUri) { SpotifyArtworkSource.trackId(spotifyUri) }
    var bitmap by remember(trackId, onlineEnabled) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(trackId, onlineEnabled) {
        if (onlineEnabled && trackId != null) bitmap = SpotifyArtworkSource.load(trackId)
    }
    val loaded = bitmap
    if (loaded != null) {
        Image(
            bitmap = loaded.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier.size(size).clip(if (round) CircleShape else MaterialTheme.shapes.medium),
        )
    } else {
        LocalArtwork(name, modifier = modifier, size = size, round = round)
    }
}

internal object SpotifyArtworkSource {
    private val idPattern = Regex("^[A-Za-z0-9]{22}$")

    fun trackId(uri: String?): String? {
        if (uri == null) return null
        val candidate = when {
            uri.startsWith("spotify:track:") -> uri.removePrefix("spotify:track:")
            uri.startsWith("https://open.spotify.com/track/") ->
                uri.removePrefix("https://open.spotify.com/track/").substringBefore('?')
            else -> return null
        }
        return candidate.takeIf { idPattern.matches(it) }
    }

    fun trackUrl(trackId: String): String = "https://open.spotify.com/track/$trackId"

    suspend fun load(trackId: String): Bitmap? = withContext(Dispatchers.IO) {
        if (!idPattern.matches(trackId)) return@withContext null
        runCatching {
            val uri = "https://open.spotify.com/oembed?url=" +
                URLEncoder.encode(trackUrl(trackId), "UTF-8")
            val metadata = readHttps(uri, 64 * 1024) ?: return@runCatching null
            val artworkUrl = JSONObject(metadata.toString(Charsets.UTF_8)).optString("thumbnail_url")
            if (!artworkUrl.startsWith("https://i.scdn.co/image/")) return@runCatching null
            val bytes = readHttps(artworkUrl, 2 * 1024 * 1024) ?: return@runCatching null
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

    private fun readHttps(url: String, maxBytes: Int): ByteArray? {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 4500
            readTimeout = 4500
            instanceFollowRedirects = false
        }
        return try {
            if (connection.responseCode != 200) return null
            if (connection.contentLength > maxBytes) return null
            connection.inputStream.use { stream ->
                val result = ByteArrayOutputStream()
                val chunk = ByteArray(8 * 1024)
                while (true) {
                    val count = stream.read(chunk)
                    if (count < 0) break
                    if (result.size() + count > maxBytes) return null
                    result.write(chunk, 0, count)
                }
                result.toByteArray()
            }
        } finally {
            connection.disconnect()
        }
    }
}
