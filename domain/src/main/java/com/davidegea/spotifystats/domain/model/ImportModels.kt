package com.davidegea.spotifystats.domain.model

data class ImportDocument(
    val uri: String,
)

data class ImportProgress(
    val currentDocument: Int,
    val totalDocuments: Int,
    val documentName: String?,
    val processedRecords: Long,
    val insertedEvents: Long,
    val duplicateEvents: Long,
    val skippedRecords: Long,
)

data class ImportSummary(
    val sourceDocuments: Int,
    val processedRecords: Long,
    val insertedEvents: Long,
    val duplicateEvents: Long,
    val skippedRecords: Long,
    val failedDocuments: Int,
)
