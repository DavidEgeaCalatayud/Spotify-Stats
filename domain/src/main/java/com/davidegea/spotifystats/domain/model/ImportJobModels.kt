package com.davidegea.spotifystats.domain.model

enum class ImportJobStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
}

data class ImportJobSnapshot(
    val id: String,
    val status: ImportJobStatus,
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
