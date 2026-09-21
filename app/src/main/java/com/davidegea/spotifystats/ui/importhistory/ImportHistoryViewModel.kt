package com.davidegea.spotifystats.ui.importhistory

import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidegea.spotifystats.domain.model.ImportDocument
import com.davidegea.spotifystats.domain.model.ImportProgress
import com.davidegea.spotifystats.domain.model.ImportSummary
import com.davidegea.spotifystats.domain.usecase.ImportSpotifyHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ImportHistoryUiState {
    data object Idle : ImportHistoryUiState
    data class Importing(val progress: ImportProgress) : ImportHistoryUiState
    data class Complete(val summary: ImportSummary) : ImportHistoryUiState
    data class Failed(val message: String) : ImportHistoryUiState
}

@HiltViewModel
class ImportHistoryViewModel @Inject constructor(
    private val importSpotifyHistory: ImportSpotifyHistoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportHistoryUiState>(ImportHistoryUiState.Idle)
    val uiState: StateFlow<ImportHistoryUiState> = _uiState.asStateFlow()

    private var importJob: Job? = null

    fun cancelImport() {
        importJob?.cancel()
        _uiState.value = ImportHistoryUiState.Failed("Import cancelled. Completed batches are kept; you can safely import the same files again.")
    }

    fun importDocuments(uriStrings: List<String>) {
        if (uriStrings.isEmpty() || _uiState.value is ImportHistoryUiState.Importing) return
        _uiState.value = ImportHistoryUiState.Importing(ImportProgress(0, uriStrings.size, null, 0, 0, 0, 0))

        importJob = viewModelScope.launch {
            val documents = uriStrings.distinct().map(::ImportDocument)
            try {
                val summary = importSpotifyHistory(documents) { progress ->
                    _uiState.value = ImportHistoryUiState.Importing(progress)
                }
                _uiState.value = ImportHistoryUiState.Complete(summary)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = ImportHistoryUiState.Failed(
                    error.message ?: "Import failed",
                )
            }
        }
    }
}
