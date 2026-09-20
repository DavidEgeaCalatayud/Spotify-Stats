package com.davidegea.spotifystats.data.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.room.withTransaction
import com.davidegea.spotifystats.database.SpotifyStatsDatabase
import com.davidegea.spotifystats.database.dao.ImportDao
import com.davidegea.spotifystats.database.entity.AlbumEntity
import com.davidegea.spotifystats.database.entity.ArtistEntity
import com.davidegea.spotifystats.database.entity.PlayEventEntity
import com.davidegea.spotifystats.database.entity.TrackArtistCrossRef
import com.davidegea.spotifystats.database.entity.TrackEntity
import com.davidegea.spotifystats.domain.model.ImportDocument
import com.davidegea.spotifystats.domain.model.ImportProgress
import com.davidegea.spotifystats.domain.model.ImportSummary
import com.davidegea.spotifystats.domain.repository.SpotifyHistoryImportRepository
import com.davidegea.spotifystats.model.PlaySource
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.withLock
import com.davidegea.spotifystats.database.DatabaseOperationGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

class RoomSpotifyHistoryImportRepository(
    private val context: Context,
    private val database: SpotifyStatsDatabase,
    private val importDao: ImportDao,
    private val parser: SpotifyExtendedHistoryParser = SpotifyExtendedHistoryParser(),
    private val normalizer: SpotifyPlayNormalizer = SpotifyPlayNormalizer(),
) : SpotifyHistoryImportRepository {

    override suspend fun importDocuments(
        documents: List<ImportDocument>,
        onProgress: suspend (ImportProgress) -> Unit,
    ): ImportSummary = withContext(Dispatchers.IO) {
        DatabaseOperationGate.mutex.withLock {
        val counters = Counters(sourceDocuments = documents.size)

        documents.forEachIndexed { index, document ->
            val uri = Uri.parse(document.uri)
            var displayName = "Spotify history"
            try {
                currentCoroutineContext().ensureActive()
                displayName = queryDisplayName(uri)
                context.contentResolver.openInputStream(uri)?.use { rawStream ->
                    val buffered = BufferedInputStream(rawStream)
                    val mimeType = context.contentResolver.getType(uri)
                    if (displayName.endsWith(".zip", ignoreCase = true) ||
                        mimeType?.contains("zip", ignoreCase = true) == true
                    ) {
                        importZip(
                            input = buffered,
                            documentIndex = index,
                            totalDocuments = documents.size,
                            documentName = displayName,
                            counters = counters,
                            onProgress = onProgress,
                        )
                    } else {
                        importJsonStream(
                            input = buffered,
                            documentIndex = index,
                            totalDocuments = documents.size,
                            documentName = displayName,
                            counters = counters,
                            onProgress = onProgress,
                        )
                    }
                } ?: error("Unable to open selected document")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                counters.failedDocuments++
            }

            emitProgress(
                documentIndex = index,
                totalDocuments = documents.size,
                documentName = displayName,
                counters = counters,
                onProgress = onProgress,
            )
        }

        ImportSummary(
            sourceDocuments = counters.sourceDocuments,
            processedRecords = counters.processedRecords,
            insertedEvents = counters.insertedEvents,
            duplicateEvents = counters.duplicateEvents,
            skippedRecords = counters.skippedRecords,
            failedDocuments = counters.failedDocuments,
        )
        }
    }

    private suspend fun importZip(
        input: InputStream,
        documentIndex: Int,
        totalDocuments: Int,
        documentName: String,
        counters: Counters,
        onProgress: suspend (ImportProgress) -> Unit,
    ) {
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            var foundJson = false
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".json", ignoreCase = true)) {
                    foundJson = true
                    importJsonStream(
                        input = zip,
                        documentIndex = documentIndex,
                        totalDocuments = totalDocuments,
                        documentName = documentName + " / " + entry.name,
                        counters = counters,
                        onProgress = onProgress,
                    )
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
            if (!foundJson) {
                counters.failedDocuments++
            }
        }
    }

    private suspend fun importJsonStream(
        input: InputStream,
        documentIndex: Int,
        totalDocuments: Int,
        documentName: String,
        counters: Counters,
        onProgress: suspend (ImportProgress) -> Unit,
    ) {
        val batch = ArrayList<NormalizedPlay>(BATCH_SIZE)

        try {
            for (record in parser.parse(input)) {
                currentCoroutineContext().ensureActive()
                counters.processedRecords++

                val normalized = normalizer.normalize(record)
                if (normalized == null) {
                    counters.skippedRecords++
                } else {
                    batch += normalized
                    if (batch.size >= BATCH_SIZE) {
                        persistBatch(batch, counters)
                        batch.clear()
                    }
                }

                if (counters.processedRecords % PROGRESS_INTERVAL == 0L) {
                    emitProgress(
                        documentIndex = documentIndex,
                        totalDocuments = totalDocuments,
                        documentName = documentName,
                        counters = counters,
                        onProgress = onProgress,
                    )
                }
            }
        } catch (_: SerializationException) {
            counters.failedDocuments++
        }

        if (batch.isNotEmpty()) {
            persistBatch(batch, counters)
        }
    }

    private suspend fun persistBatch(
        batch: List<NormalizedPlay>,
        counters: Counters,
    ) {
        var inserted = 0L
        var duplicate = 0L
        database.withTransaction {
            for (play in batch) {
                val artistId = resolveArtist(play)
                val albumId = resolveAlbum(play)
                val trackId = resolveTrack(play, albumId)

                importDao.insertTrackArtist(
                    TrackArtistCrossRef(
                        trackId = trackId,
                        artistId = artistId,
                        position = 0,
                    ),
                )

                val insertedId = importDao.insertPlayEvent(
                    PlayEventEntity(
                        eventHash = play.eventHash,
                        trackId = trackId,
                        playedAtEpochMs = play.playedAtEpochMs,
                        msPlayed = play.msPlayed,
                        platform = play.platform,
                        country = play.country,
                        reasonStart = play.reasonStart,
                        reasonEnd = play.reasonEnd,
                        shuffle = play.shuffle,
                        skipped = play.skipped,
                        offline = play.offline,
                        privateSession = play.privateSession,
                        source = PlaySource.SPOTIFY_EXPORT,
                    ),
                )

                if (insertedId == -1L) {
                    duplicate++
                } else {
                    inserted++
                }
            }
        }
        counters.insertedEvents += inserted
        counters.duplicateEvents += duplicate
    }

    private suspend fun resolveArtist(play: NormalizedPlay): Long {
        val insertedId = importDao.insertArtist(
            ArtistEntity(
                identityKey = play.artistIdentity,
                spotifyId = null,
                name = play.artistName,
                normalizedName = play.artistNormalizedName,
            ),
        )
        return insertedId.takeIf { it != -1L }
            ?: requireNotNull(importDao.findArtistId(play.artistIdentity))
    }

    private suspend fun resolveAlbum(play: NormalizedPlay): Long? {
        val albumName = play.albumName ?: return null
        val albumIdentity = play.albumIdentity ?: return null
        val insertedId = importDao.insertAlbum(
            AlbumEntity(
                identityKey = albumIdentity,
                spotifyId = null,
                name = albumName,
                releaseDate = null,
            ),
        )
        return insertedId.takeIf { it != -1L }
            ?: importDao.findAlbumId(albumIdentity)
    }

    private suspend fun resolveTrack(
        play: NormalizedPlay,
        albumId: Long?,
    ): Long {
        val insertedId = importDao.insertTrack(
            TrackEntity(
                spotifyUri = play.spotifyTrackUri,
                identityKey = play.trackIdentity,
                name = play.trackName,
                albumId = albumId,
                durationMs = null,
            ),
        )
        return insertedId.takeIf { it != -1L }
            ?: requireNotNull(importDao.findTrackId(play.trackIdentity))
    }

    private fun queryDisplayName(uri: Uri): String {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (column >= 0) {
                    return cursor.getString(column)
                }
            }
        }
        return uri.lastPathSegment ?: "Spotify history"
    }

    private suspend fun emitProgress(
        documentIndex: Int,
        totalDocuments: Int,
        documentName: String?,
        counters: Counters,
        onProgress: suspend (ImportProgress) -> Unit,
    ) {
        onProgress(
            ImportProgress(
                currentDocument = documentIndex + 1,
                totalDocuments = totalDocuments,
                documentName = documentName,
                processedRecords = counters.processedRecords,
                insertedEvents = counters.insertedEvents,
                duplicateEvents = counters.duplicateEvents,
                skippedRecords = counters.skippedRecords,
            ),
        )
    }

    private data class Counters(
        val sourceDocuments: Int,
        var processedRecords: Long = 0,
        var insertedEvents: Long = 0,
        var duplicateEvents: Long = 0,
        var skippedRecords: Long = 0,
        var failedDocuments: Int = 0,
    )

    private companion object {
        const val BATCH_SIZE = 250
        const val PROGRESS_INTERVAL = 500L
    }
}
