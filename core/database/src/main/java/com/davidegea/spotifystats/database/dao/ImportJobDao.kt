package com.davidegea.spotifystats.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.davidegea.spotifystats.database.entity.ImportDocumentDiagnosticEntity
import com.davidegea.spotifystats.database.entity.ImportRunEntity
import kotlinx.coroutines.flow.Flow

data class ImportJobSnapshotRow(
    val id: String,
    val status: String,
    val totalDocuments: Int,
    val completedDocuments: Int,
    val processedRecords: Long,
    val insertedEvents: Long,
    val duplicateEvents: Long,
    val skippedRecords: Long,
    val failedDocuments: Int,
    val currentDocumentName: String?,
    val lastError: String?,
)

@Dao
interface ImportJobDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRun(run: ImportRunEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDocuments(documents: List<ImportDocumentDiagnosticEntity>)

    @Query("SELECT * FROM import_runs WHERE id = :runId LIMIT 1")
    suspend fun getRun(runId: String): ImportRunEntity?

    @Query(
        """
        SELECT *
        FROM import_documents
        WHERE run_id = :runId
          AND status != 'COMPLETED'
        ORDER BY position ASC
        """,
    )
    suspend fun getIncompleteDocuments(runId: String): List<ImportDocumentDiagnosticEntity>

    @Query(
        """
        SELECT *
        FROM import_documents
        WHERE run_id = :runId
        ORDER BY position ASC
        """,
    )
    fun observeDocuments(runId: String): Flow<List<ImportDocumentDiagnosticEntity>>

    @Query(
        """
        SELECT
            r.id AS id,
            r.status AS status,
            r.total_documents AS totalDocuments,
            (
                SELECT COUNT(*)
                FROM import_documents d
                WHERE d.run_id = r.id
                  AND d.status = 'COMPLETED'
            ) AS completedDocuments,
            r.processed_records AS processedRecords,
            r.inserted_events AS insertedEvents,
            r.duplicate_events AS duplicateEvents,
            r.skipped_records AS skippedRecords,
            r.failed_documents AS failedDocuments,
            (
                SELECT d.display_name
                FROM import_documents d
                WHERE d.run_id = r.id
                  AND d.status = 'RUNNING'
                ORDER BY d.position ASC
                LIMIT 1
            ) AS currentDocumentName,
            r.last_error AS lastError
        FROM import_runs r
        ORDER BY r.created_at DESC
        LIMIT 1
        """,
    )
    fun observeLatestRun(): Flow<ImportJobSnapshotRow?>

    @Query(
        """
        UPDATE import_runs
        SET status = :status,
            updated_at = :updatedAt,
            last_error = :lastError
        WHERE id = :runId
        """,
    )
    suspend fun updateRunStatus(
        runId: String,
        status: String,
        updatedAt: Long,
        lastError: String?,
    )

    @Query(
        """
        UPDATE import_documents
        SET status = :status,
            error_message = :errorMessage,
            updated_at = :updatedAt
        WHERE id = :documentId
        """,
    )
    suspend fun updateDocumentStatus(
        documentId: Long,
        status: String,
        errorMessage: String?,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE import_documents
        SET processed_records = :processedRecords,
            inserted_events = :insertedEvents,
            duplicate_events = :duplicateEvents,
            skipped_records = :skippedRecords,
            updated_at = :updatedAt
        WHERE id = :documentId
        """,
    )
    suspend fun updateDocumentProgress(
        documentId: Long,
        processedRecords: Long,
        insertedEvents: Long,
        duplicateEvents: Long,
        skippedRecords: Long,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE import_documents
        SET processed_records = :processedRecords,
            inserted_events = :insertedEvents,
            duplicate_events = :duplicateEvents,
            skipped_records = :skippedRecords,
            status = :status,
            error_message = :errorMessage,
            updated_at = :updatedAt
        WHERE id = :documentId
        """,
    )
    suspend fun finishDocument(
        documentId: Long,
        processedRecords: Long,
        insertedEvents: Long,
        duplicateEvents: Long,
        skippedRecords: Long,
        status: String,
        errorMessage: String?,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE import_runs
        SET processed_records = (
                SELECT COALESCE(SUM(processed_records), 0)
                FROM import_documents
                WHERE run_id = :runId
            ),
            inserted_events = (
                SELECT COALESCE(SUM(inserted_events), 0)
                FROM import_documents
                WHERE run_id = :runId
            ),
            duplicate_events = (
                SELECT COALESCE(SUM(duplicate_events), 0)
                FROM import_documents
                WHERE run_id = :runId
            ),
            skipped_records = (
                SELECT COALESCE(SUM(skipped_records), 0)
                FROM import_documents
                WHERE run_id = :runId
            ),
            failed_documents = (
                SELECT COUNT(*)
                FROM import_documents
                WHERE run_id = :runId
                  AND status = 'FAILED'
            ),
            updated_at = :updatedAt
        WHERE id = :runId
        """,
    )
    suspend fun recalculateRunCounters(runId: String, updatedAt: Long)

    @Query(
        """
        UPDATE import_documents
        SET status = 'CANCELLED',
            updated_at = :updatedAt
        WHERE run_id = :runId
          AND status IN ('PENDING', 'RUNNING')
        """,
    )
    suspend fun cancelIncompleteDocuments(runId: String, updatedAt: Long)
}
