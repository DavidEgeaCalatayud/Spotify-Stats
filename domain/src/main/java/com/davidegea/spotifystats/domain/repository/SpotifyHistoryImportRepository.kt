package com.davidegea.spotifystats.domain.repository

import com.davidegea.spotifystats.domain.model.ImportDocument
import com.davidegea.spotifystats.domain.model.ImportProgress
import com.davidegea.spotifystats.domain.model.ImportSummary

interface SpotifyHistoryImportRepository {
    suspend fun importDocuments(
        documents: List<ImportDocument>,
        onProgress: suspend (ImportProgress) -> Unit,
    ): ImportSummary
}
