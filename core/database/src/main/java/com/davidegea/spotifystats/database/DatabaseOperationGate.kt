package com.davidegea.spotifystats.database

import kotlinx.coroutines.sync.Mutex

/** Serializes bulk import, backup, restore and deletion within this app process. */
object DatabaseOperationGate {
    val mutex = Mutex()
}
