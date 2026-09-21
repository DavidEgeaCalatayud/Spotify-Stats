package com.davidegea.spotifystats.domain.repository

import com.davidegea.spotifystats.domain.model.ImportDocument
import com.davidegea.spotifystats.domain.model.ImportJobSnapshot
import kotlinx.coroutines.flow.Flow

interface SpotifyImportJobManager {
    fun observeLatestJob(): Flow<ImportJobSnapshot?>

    suspend fun enqueue(documents: List<ImportDocument>): String

    suspend fun cancel(runId: String)

    suspend fun retry(runId: String)
}
