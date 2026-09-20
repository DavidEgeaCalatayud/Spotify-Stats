package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.model.ImportDocument
import com.davidegea.spotifystats.domain.model.ImportProgress
import com.davidegea.spotifystats.domain.model.ImportSummary
import com.davidegea.spotifystats.domain.repository.SpotifyHistoryImportRepository

class ImportSpotifyHistoryUseCase(
    private val repository: SpotifyHistoryImportRepository,
) {
    suspend operator fun invoke(
        documents: List<ImportDocument>,
        onProgress: suspend (ImportProgress) -> Unit,
    ): ImportSummary = repository.importDocuments(documents, onProgress)
}
