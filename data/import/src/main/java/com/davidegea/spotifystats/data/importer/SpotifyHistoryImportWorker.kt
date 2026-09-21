package com.davidegea.spotifystats.data.importer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.davidegea.spotifystats.database.SpotifyStatsDatabase
import com.davidegea.spotifystats.database.dao.ImportJobDao
import com.davidegea.spotifystats.domain.model.ImportDocument
import com.davidegea.spotifystats.domain.model.ImportJobStatus
import com.davidegea.spotifystats.domain.repository.SpotifyHistoryImportRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

@HiltWorker
class SpotifyHistoryImportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val database: SpotifyStatsDatabase,
    private val importJobDao: ImportJobDao,
    private val importRepository: SpotifyHistoryImportRepository,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val runId = inputData.getString(KEY_RUN_ID) ?: return Result.failure()
        val run = importJobDao.getRun(runId) ?: return Result.failure()
        if (run.status == ImportJobStatus.CANCELLED.name) return Result.success()

        ensureNotificationChannel()
        setForeground(createForegroundInfo("Preparing Spotify history import"))
        importJobDao.updateRunStatus(
            runId = runId,
            status = ImportJobStatus.RUNNING.name,
            updatedAt = System.currentTimeMillis(),
            lastError = null,
        )

        return try {
            val documents = importJobDao.getIncompleteDocuments(runId)
            documents.forEachIndexed { index, document ->
                if (isStopped) throw CancellationException("Import worker stopped")

                importJobDao.updateDocumentStatus(
                    documentId = document.id,
                    status = WorkManagerSpotifyImportJobManager.DOCUMENT_RUNNING,
                    errorMessage = null,
                    updatedAt = System.currentTimeMillis(),
                )
                setForeground(
                    createForegroundInfo(
                        "Importing " + (document.displayName ?: "Spotify history") +
                            " (" + (index + 1) + "/" + documents.size + ")",
                    ),
                )

                try {
                    val baseProcessed = document.processedRecords
                    val baseInserted = document.insertedEvents
                    val baseDuplicates = document.duplicateEvents
                    val baseSkipped = document.skippedRecords

                    val summary = importRepository.importDocuments(
                        documents = listOf(ImportDocument(document.uri)),
                    ) { progress ->
                        val now = System.currentTimeMillis()
                        importJobDao.updateDocumentProgress(
                            documentId = document.id,
                            processedRecords = baseProcessed + progress.processedRecords,
                            insertedEvents = baseInserted + progress.insertedEvents,
                            duplicateEvents = baseDuplicates + progress.duplicateEvents,
                            skippedRecords = baseSkipped + progress.skippedRecords,
                            updatedAt = now,
                        )
                        importJobDao.recalculateRunCounters(runId, now)
                        setProgress(
                            workDataOf(
                                KEY_RUN_ID to runId,
                                KEY_PROCESSED_RECORDS to progress.processedRecords,
                            ),
                        )
                    }

                    val failed = summary.failedDocuments > 0
                    val now = System.currentTimeMillis()
                    importJobDao.finishDocument(
                        documentId = document.id,
                        processedRecords = baseProcessed + summary.processedRecords,
                        insertedEvents = baseInserted + summary.insertedEvents,
                        duplicateEvents = baseDuplicates + summary.duplicateEvents,
                        skippedRecords = baseSkipped + summary.skippedRecords,
                        status = if (failed) {
                            WorkManagerSpotifyImportJobManager.DOCUMENT_FAILED
                        } else {
                            WorkManagerSpotifyImportJobManager.DOCUMENT_COMPLETED
                        },
                        errorMessage = if (failed) {
                            "The document could not be fully processed"
                        } else {
                            null
                        },
                        updatedAt = now,
                    )
                    importJobDao.recalculateRunCounters(runId, now)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    val now = System.currentTimeMillis()
                    importJobDao.updateDocumentStatus(
                        documentId = document.id,
                        status = WorkManagerSpotifyImportJobManager.DOCUMENT_FAILED,
                        errorMessage = error.message ?: error::class.java.simpleName,
                        updatedAt = now,
                    )
                    importJobDao.recalculateRunCounters(runId, now)
                }
            }

            val finalRun = importJobDao.getRun(runId)
            val failedDocuments = finalRun?.failedDocuments ?: 0
            importJobDao.updateRunStatus(
                runId = runId,
                status = if (failedDocuments > 0) {
                    ImportJobStatus.FAILED.name
                } else {
                    ImportJobStatus.COMPLETED.name
                },
                updatedAt = System.currentTimeMillis(),
                lastError = if (failedDocuments > 0) {
                    failedDocuments.toString() + " document(s) need attention"
                } else {
                    null
                },
            )
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            importJobDao.updateRunStatus(
                runId = runId,
                status = ImportJobStatus.FAILED.name,
                updatedAt = System.currentTimeMillis(),
                lastError = error.message ?: "Background import failed",
            )
            Result.failure()
        }
    }

    private fun createForegroundInfo(message: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Spotify Stats")
            .setContentText(message)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Spotify history import",
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
    }

    companion object {
        const val KEY_RUN_ID = "run_id"
        const val KEY_PROCESSED_RECORDS = "processed_records"
        private const val CHANNEL_ID = "spotify_history_import"
        private const val NOTIFICATION_ID = 1001
    }
}
