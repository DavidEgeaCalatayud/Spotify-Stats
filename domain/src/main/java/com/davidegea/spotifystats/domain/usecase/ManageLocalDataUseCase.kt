package com.davidegea.spotifystats.domain.usecase

import com.davidegea.spotifystats.domain.repository.DataControlRepository

class ManageLocalDataUseCase(private val repository: DataControlRepository) {
    suspend fun export(uri: String) = repository.exportBackup(uri)
    suspend fun restore(uri: String) = repository.restoreBackup(uri)
    suspend fun delete() = repository.deleteHistory()
}
