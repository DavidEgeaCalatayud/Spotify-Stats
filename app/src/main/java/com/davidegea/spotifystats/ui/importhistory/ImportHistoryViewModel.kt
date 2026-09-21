package com.davidegea.spotifystats.ui.importhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.ImportDocument
import com.davidegea.spotifystats.domain.model.ImportJobSnapshot
import com.davidegea.spotifystats.domain.model.ImportJobStatus
import com.davidegea.spotifystats.domain.model.ImportProgress
import com.davidegea.spotifystats.domain.model.ImportSummary
import com.davidegea.spotifystats.domain.repository.SpotifyImportJobManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ImportHistoryUiState {
    data object Idle : ImportHistoryUiState
    data class Importing(
        val runId: String,
        val progress: ImportProgress,
        val queued: Boolean,
    ) : ImportHistoryUiState
    data class Complete(
        val runId: String,
        val summary: ImportSummary,
    ) : ImportHistoryUiState
    data class Failed(
        val runId: String?,
        val message: String,
        val canRetry: Boolean,
    ) : ImportHistoryUiState
}

@HiltViewModel
class ImportHistoryViewModel @Inject constructor(
    private val importJobs: SpotifyImportJobManager,
) : ViewModel() {

    private val transientError = MutableStateFlow<String?>(null)

    val uiState = combine(
        importJobs.observeLatestJob(),
        transientError,
    ) { job, error ->
        if (error != null) {
            ImportHistoryUiState.Failed(
                runId = job?.id,
                message = error,
                canRetry = job != null,
            )
        } else {
            job.toUiState()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ImportHistoryUiState.Idle,
    )

    fun importDocuments(uriStrings: List<String>) {
        if (uriStrings.isEmpty() || uiState.value is ImportHistoryUiState.Importing) return
        transientError.value = null
        viewModelScope.launch {
            try {
                importJobs.enqueue(
                    uriStrings.distinct().map(::ImportDocument),
                )
            } catch (error: Exception) {
                transientError.value = error.message ?: "Unable to queue import"
            }
        }
    }

    fun cancelImport() {
        val current = uiState.value as? ImportHistoryUiState.Importing ?: return
        viewModelScope.launch {
            importJobs.cancel(current.runId)
        }
    }

    fun retryImport() {
        val current = uiState.value as? ImportHistoryUiState.Failed ?: return
        val runId = current.runId ?: return
        transientError.value = null
        viewModelScope.launch {
            try {
                importJobs.retry(runId)
            } catch (error: Exception) {
                transientError.value = error.message ?: "Unable to retry import"
            }
        }
    }

    fun clearTransientError() {
        transientError.value = null
    }

    private fun ImportJobSnapshot?.toUiState(): ImportHistoryUiState {
        this ?: return ImportHistoryUiState.Idle
        return when (status) {
            ImportJobStatus.QUEUED,
            ImportJobStatus.RUNNING,
            -> ImportHistoryUiState.Importing(
                runId = id,
                progress = ImportProgress(
                    currentDocument = (completedDocuments + 1).coerceAtMost(totalDocuments),
                    totalDocuments = totalDocuments,
                    documentName = currentDocumentName,
                    processedRecords = processedRecords,
                    insertedEvents = insertedEvents,
                    duplicateEvents = duplicateEvents,
                    skippedRecords = skippedRecords,
                ),
                queued = status == ImportJobStatus.QUEUED,
            )

            ImportJobStatus.COMPLETED -> ImportHistoryUiState.Complete(
                runId = id,
                summary = ImportSummary(
                    sourceDocuments = totalDocuments,
                    processedRecords = processedRecords,
                    insertedEvents = insertedEvents,
                    duplicateEvents = duplicateEvents,
                    skippedRecords = skippedRecords,
                    failedDocuments = failedDocuments,
                ),
            )

            ImportJobStatus.FAILED -> ImportHistoryUiState.Failed(
                runId = id,
                message = lastError ?: "Import finished with document errors",
                canRetry = true,
            )

            ImportJobStatus.CANCELLED -> ImportHistoryUiState.Failed(
                runId = id,
                message = "Import cancelled. You can resume the remaining documents.",
                canRetry = true,
            )
        }
    }
}
