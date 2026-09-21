package com.davidegea.spotifystats.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "import_runs",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["status"]),
    ],
)
data class ImportRunEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAtEpochMs: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMs: Long,
    val status: String,
    @ColumnInfo(name = "total_documents")
    val totalDocuments: Int,
    @ColumnInfo(name = "processed_records")
    val processedRecords: Long = 0,
    @ColumnInfo(name = "inserted_events")
    val insertedEvents: Long = 0,
    @ColumnInfo(name = "duplicate_events")
    val duplicateEvents: Long = 0,
    @ColumnInfo(name = "skipped_records")
    val skippedRecords: Long = 0,
    @ColumnInfo(name = "failed_documents")
    val failedDocuments: Int = 0,
    @ColumnInfo(name = "last_error")
    val lastError: String? = null,
)

@Entity(
    tableName = "import_documents",
    foreignKeys = [
        ForeignKey(
            entity = ImportRunEntity::class,
            parentColumns = ["id"],
            childColumns = ["run_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["run_id", "position"], unique = true),
        Index(value = ["run_id", "status"]),
    ],
)
data class ImportDocumentDiagnosticEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "run_id")
    val runId: String,
    val position: Int,
    val uri: String,
    @ColumnInfo(name = "display_name")
    val displayName: String?,
    val status: String,
    @ColumnInfo(name = "processed_records")
    val processedRecords: Long = 0,
    @ColumnInfo(name = "inserted_events")
    val insertedEvents: Long = 0,
    @ColumnInfo(name = "duplicate_events")
    val duplicateEvents: Long = 0,
    @ColumnInfo(name = "skipped_records")
    val skippedRecords: Long = 0,
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMs: Long,
)
