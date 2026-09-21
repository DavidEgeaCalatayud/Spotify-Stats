package com.davidegea.spotifystats.data.importer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.room.withTransaction
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.davidegea.spotifystats.database.SpotifyStatsDatabase
import com.davidegea.spotifystats.database.dao.ImportJobDao
import com.davidegea.spotifystats.database.entity.ImportDocumentDiagnosticEntity
import com.davidegea.spotifystats.database.entity.ImportRunEntity
import com.davidegea.spotifystats.domain.model.ImportDocument
import com.davidegea.spotifystats.domain.model.ImportDocumentDiagnostic
import com.davidegea.spotifystats.domain.model.ImportDocumentStatus
import com.davidegea.spotifystats.domain.model.ImportJobSnapshot
import com.davidegea.spotifystats.domain.model.ImportJobStatus
import com.davidegea.spotifystats.domain.repository.SpotifyImportJobManager
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class WorkManagerSpotifyImportJobManager(
    private val context: Context,
    private val database: SpotifyStatsDatabase,
    private val importJobDao: ImportJobDao,
    private val workManager: WorkManager = WorkManager.getInstance(context),
) : SpotifyImportJobManager {

    override fun observeLatestJob(): Flow<ImportJobSnapshot?> =
        importJobDao.observeLatestRun().flatMapLatest { row ->
            if (row == null) {
                flowOf(null)
            } else {
                importJobDao.observeDocuments(row.id).map { documents ->
                    ImportJobSnapshot(
                        id = row.id,
                        status = runCatching { ImportJobStatus.valueOf(row.status) }
                            .getOrDefault(ImportJobStatus.FAILED),
                        totalDocuments = row.totalDocuments,
                        completedDocuments = row.completedDocuments,
                        processedRecords = row.processedRecords,
                        insertedEvents = row.insertedEvents,
                        duplicateEvents = row.duplicateEvents,
                        skippedRecords = row.skippedRecords,
                        failedDocuments = row.failedDocuments,
                        currentDocumentName = row.currentDocumentName,
                        lastError = row.lastError,
                        documents = documents.map { document ->
                            ImportDocumentDiagnostic(
                                position = document.position,
                                displayName = document.displayName,
                                status = runCatching {
                                    ImportDocumentStatus.valueOf(document.status)
                                }.getOrDefault(ImportDocumentStatus.FAILED),
                                processedRecords = document.processedRecords,
                                insertedEvents = document.insertedEvents,
                                duplicateEvents = document.duplicateEvents,
                                skippedRecords = document.skippedRecords,
                                errorMessage = document.errorMessage,
                            )
                        },
                    )
                }
            }
        }

    override suspend fun enqueue(documents: List<ImportDocument>): String {
        val unique = documents.distinctBy(ImportDocument::uri)
        require(unique.isNotEmpty()) { "Select at least one Spotify history document" }

        unique.forEach { persistReadPermission(Uri.parse(it.uri)) }

        val runId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        database.withTransaction {
            importJobDao.insertRun(
                ImportRunEntity(
                    id = runId,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now,
                    status = ImportJobStatus.QUEUED.name,
                    totalDocuments = unique.size,
                ),
            )
            importJobDao.insertDocuments(
                unique.mapIndexed { index, document ->
                    val uri = Uri.parse(document.uri)
                    ImportDocumentDiagnosticEntity(
                        runId = runId,
                        position = index,
                        uri = document.uri,
                        displayName = queryDisplayName(uri),
                        status = DOCUMENT_PENDING,
                        updatedAtEpochMs = now,
                    )
                },
            )
        }
        enqueueWork(runId, ExistingWorkPolicy.KEEP)
        return runId
    }

    override suspend fun cancel(runId: String) {
        workManager.cancelUniqueWork(workName(runId))
        val now = System.currentTimeMillis()
        database.withTransaction {
            importJobDao.updateRunStatus(
                runId = runId,
                status = ImportJobStatus.CANCELLED.name,
                updatedAt = now,
                lastError = "Import cancelled",
            )
            importJobDao.cancelIncompleteDocuments(runId, now)
            importJobDao.recalculateRunCounters(runId, now)
        }
    }

    override suspend fun retry(runId: String) {
        requireNotNull(importJobDao.getRun(runId)) { "Import run not found" }
        val now = System.currentTimeMillis()
        importJobDao.updateRunStatus(
            runId = runId,
            status = ImportJobStatus.QUEUED.name,
            updatedAt = now,
            lastError = null,
        )
        enqueueWork(runId, ExistingWorkPolicy.REPLACE)
    }

    private fun enqueueWork(runId: String, policy: ExistingWorkPolicy) {
        val request = OneTimeWorkRequestBuilder<SpotifyHistoryImportWorker>()
            .setInputData(workDataOf(SpotifyHistoryImportWorker.KEY_RUN_ID to runId))
            .addTag(WORK_TAG)
            .build()
        workManager.enqueueUniqueWork(workName(runId), policy, request)
    }

    private fun persistReadPermission(uri: Uri) {
        if (uri.scheme != "content") return
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (error: SecurityException) {
            throw IllegalArgumentException(
                "The selected file cannot be kept for background import. Select it again using the Android document picker.",
                error,
            )
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        if (uri.scheme != "content") return uri.lastPathSegment
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            }
        }.getOrNull() ?: uri.lastPathSegment
    }

    companion object {
        const val DOCUMENT_PENDING = "PENDING"
        const val DOCUMENT_RUNNING = "RUNNING"
        const val DOCUMENT_COMPLETED = "COMPLETED"
        const val DOCUMENT_FAILED = "FAILED"
        const val DOCUMENT_CANCELLED = "CANCELLED"
        const val WORK_TAG = "spotify-history-import"

        fun workName(runId: String) = "spotify-history-import-" + runId
    }
}
