package com.davidegea.spotifystats.domain.repository

interface DataControlRepository {
    suspend fun exportBackup(uri: String)
    /** Validate the entire backup in a staging database before replacing any user data. */
    suspend fun restoreBackup(uri: String)
    suspend fun deleteHistory()
}
