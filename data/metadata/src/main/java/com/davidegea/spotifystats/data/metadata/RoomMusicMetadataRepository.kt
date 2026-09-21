package com.davidegea.spotifystats.data.metadata

import android.content.Context
import androidx.room.withTransaction
import com.davidegea.spotifystats.database.SpotifyStatsDatabase
import com.davidegea.spotifystats.database.dao.MetadataDao
import com.davidegea.spotifystats.database.entity.AlbumMetadataEntity
import com.davidegea.spotifystats.database.entity.TrackMetadataEntity
import com.davidegea.spotifystats.domain.model.AlbumMetadata
import com.davidegea.spotifystats.domain.model.MetadataLookup
import com.davidegea.spotifystats.domain.model.MetadataRefreshResult
import com.davidegea.spotifystats.domain.model.ProviderTrackMetadata
import com.davidegea.spotifystats.domain.model.TrackMetadata
import com.davidegea.spotifystats.domain.repository.MusicMetadataProvider
import com.davidegea.spotifystats.domain.repository.MusicMetadataRepository
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class RoomMusicMetadataRepository(
    private val context: Context,
    private val database: SpotifyStatsDatabase,
    private val dao: MetadataDao,
    private val providers: List<MusicMetadataProvider>,
) : MusicMetadataRepository {

    private val artworkDirectory =
        File(context.filesDir, "metadata-artwork").apply { mkdirs() }

    override fun observeTrackMetadata(trackId: Long): Flow<TrackMetadata?> =
        dao.observeTrackMetadata(trackId).map { entity ->
            entity?.let {
                TrackMetadata(
                    trackId = it.trackId,
                    provider = it.provider,
                    providerTrackId = it.providerTrackId,
                    durationMs = it.durationMs,
                    artworkPath = it.artworkPath?.let(::absoluteArtworkPath),
                    refreshedAtEpochMs = it.refreshedAtEpochMs,
                )
            }
        }

    override fun observeAlbumMetadata(albumId: Long): Flow<AlbumMetadata?> =
        dao.observeAlbumMetadata(albumId).map { entity ->
            entity?.let {
                AlbumMetadata(
                    albumId = it.albumId,
                    provider = it.provider,
                    releaseDate = it.releaseDate,
                    artworkPath = it.artworkPath?.let(::absoluteArtworkPath),
                    refreshedAtEpochMs = it.refreshedAtEpochMs,
                )
            }
        }

    override suspend fun refresh(
        limit: Int,
        staleAfterMs: Long,
    ): MetadataRefreshResult = withContext(Dispatchers.IO) {
        require(limit in 1..500) { "limit must be between 1 and 500" }
        require(staleAfterMs >= 0L) { "staleAfterMs must be non-negative" }

        if (providers.isEmpty()) {
            return@withContext MetadataRefreshResult(
                attempted = 0,
                updated = 0,
                unavailable = 0,
                failed = 0,
                providerConfigured = false,
            )
        }

        val now = System.currentTimeMillis()
        val candidates = dao.loadCandidates(
            staleBeforeEpochMs = now - staleAfterMs,
            limit = limit,
        )

        var updated = 0
        var unavailable = 0
        var failed = 0

        for (candidate in candidates) {
            coroutineContext.ensureActive()
            val lookup = MetadataLookup(
                trackId = candidate.trackId,
                spotifyUri = candidate.spotifyUri,
                trackName = candidate.trackName,
                artistName = candidate.artistName,
                albumId = candidate.albumId,
                albumName = candidate.albumName,
            )

            var resolved: Pair<MusicMetadataProvider, ProviderTrackMetadata>? = null
            var providerFailure = false

            for (provider in providers) {
                coroutineContext.ensureActive()
                try {
                    val result = provider.lookup(lookup)
                    if (result != null) {
                        resolved = provider to result
                        break
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    providerFailure = true
                }
            }

            val match = resolved
            if (match == null) {
                if (providerFailure) failed++ else unavailable++
                continue
            }

            try {
                val provider = match.first
                val metadata = validate(match.second)
                val artworkFileName = metadata.artworkBytes?.let { bytes ->
                    writeArtwork(
                        trackId = candidate.trackId,
                        bytes = bytes,
                        mimeType = metadata.artworkMimeType,
                    )
                }

                database.withTransaction {
                    dao.upsertTrackMetadata(
                        TrackMetadataEntity(
                            trackId = candidate.trackId,
                            provider = provider.id,
                            providerTrackId = metadata.providerTrackId,
                            durationMs = metadata.durationMs,
                            artworkPath = artworkFileName,
                            refreshedAtEpochMs = now,
                        ),
                    )

                    candidate.albumId?.let { albumId ->
                        dao.upsertAlbumMetadata(
                            AlbumMetadataEntity(
                                albumId = albumId,
                                provider = provider.id,
                                releaseDate = metadata.albumReleaseDate,
                                artworkPath = artworkFileName,
                                refreshedAtEpochMs = now,
                            ),
                        )
                    }
                }
                updated++
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                failed++
            }
        }

        trimArtworkCache()

        MetadataRefreshResult(
            attempted = candidates.size,
            updated = updated,
            unavailable = unavailable,
            failed = failed,
            providerConfigured = true,
        )
    }

    override suspend fun clearCache() = withContext(Dispatchers.IO) {
        database.withTransaction {
            dao.clearTrackMetadata()
            dao.clearAlbumMetadata()
        }
        artworkDirectory.deleteRecursively()
        artworkDirectory.mkdirs()
        Unit
    }

    private fun validate(metadata: ProviderTrackMetadata): ProviderTrackMetadata {
        metadata.durationMs?.let {
            require(it in 1L..MAX_DURATION_MS) { "Invalid metadata duration" }
        }
        metadata.albumReleaseDate?.let {
            require(RELEASE_DATE.matches(it)) { "Invalid metadata release date" }
        }

        val bytes = metadata.artworkBytes
        val mime = metadata.artworkMimeType
        if (bytes != null) {
            require(bytes.isNotEmpty() && bytes.size <= MAX_ARTWORK_BYTES) {
                "Artwork exceeds size limit"
            }
            require(mime in SUPPORTED_ARTWORK_MIME_TYPES) {
                "Unsupported artwork format"
            }
        } else {
            require(mime == null) { "Artwork MIME type without artwork bytes" }
        }

        return metadata
    }

    private fun writeArtwork(
        trackId: Long,
        bytes: ByteArray,
        mimeType: String?,
    ): String {
        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> error("Unsupported artwork MIME type")
        }
        val fileName = "track-" + trackId + "." + extension
        val destination = File(artworkDirectory, fileName)
        val temporary = File(artworkDirectory, fileName + ".tmp")
        temporary.outputStream().use { it.write(bytes) }
        check(temporary.renameTo(destination)) { "Unable to commit artwork cache file" }
        return fileName
    }

    private fun trimArtworkCache() {
        var total = artworkDirectory.listFiles()
            .orEmpty()
            .filter(File::isFile)
            .sumOf(File::length)

        if (total <= MAX_ARTWORK_CACHE_BYTES) return

        artworkDirectory.listFiles()
            .orEmpty()
            .filter(File::isFile)
            .sortedBy(File::lastModified)
            .forEach { file ->
                if (total <= MAX_ARTWORK_CACHE_BYTES) return
                val length = file.length()
                if (file.delete()) total -= length
            }
    }

    private fun absoluteArtworkPath(fileName: String): String =
        File(artworkDirectory, fileName).absolutePath

    private companion object {
        const val MAX_DURATION_MS = 86_400_000L
        const val MAX_ARTWORK_BYTES = 2 * 1024 * 1024
        const val MAX_ARTWORK_CACHE_BYTES = 100L * 1024L * 1024L
        val RELEASE_DATE = Regex("""\d{4}(?:-\d{2}(?:-\d{2})?)?""")
        val SUPPORTED_ARTWORK_MIME_TYPES =
            setOf("image/jpeg", "image/png", "image/webp")
    }
}
