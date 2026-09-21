package com.davidegea.spotifystats.database

import androidx.room.Room
import com.davidegea.spotifystats.database.entity.ImportDocumentDiagnosticEntity
import com.davidegea.spotifystats.database.entity.ImportRunEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ImportJobDatabaseTest {

    private lateinit var db: SpotifyStatsDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            SpotifyStatsDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun close() {
        db.close()
    }

    @Test
    fun completedDocumentsAreCheckpointedAndExcludedFromResume() = runBlocking {
        val dao = db.importJobDao()
        dao.insertRun(
            ImportRunEntity(
                id = "run",
                createdAtEpochMs = 1,
                updatedAtEpochMs = 1,
                status = "RUNNING",
                totalDocuments = 2,
            ),
        )
        dao.insertDocuments(
            listOf(
                ImportDocumentDiagnosticEntity(
                    runId = "run",
                    position = 0,
                    uri = "content://history/first",
                    displayName = "first.json",
                    status = "PENDING",
                    updatedAtEpochMs = 1,
                ),
                ImportDocumentDiagnosticEntity(
                    runId = "run",
                    position = 1,
                    uri = "content://history/second",
                    displayName = "second.json",
                    status = "PENDING",
                    updatedAtEpochMs = 1,
                ),
            ),
        )

        val documents = dao.observeDocuments("run").first()
        val first = documents[0]
        val second = documents[1]

        dao.finishDocument(
            documentId = first.id,
            processedRecords = 100,
            insertedEvents = 90,
            duplicateEvents = 5,
            skippedRecords = 5,
            status = "COMPLETED",
            errorMessage = null,
            updatedAt = 2,
        )
        dao.updateDocumentStatus(
            documentId = second.id,
            status = "RUNNING",
            errorMessage = null,
            updatedAt = 2,
        )
        dao.updateDocumentProgress(
            documentId = second.id,
            processedRecords = 40,
            insertedEvents = 35,
            duplicateEvents = 3,
            skippedRecords = 2,
            updatedAt = 3,
        )
        dao.recalculateRunCounters("run", 3)

        val resumable = dao.getIncompleteDocuments("run")
        assertEquals(listOf("second.json"), resumable.map { it.displayName })

        val snapshot = dao.observeLatestRun().first()!!
        assertEquals(1, snapshot.completedDocuments)
        assertEquals(140L, snapshot.processedRecords)
        assertEquals(125L, snapshot.insertedEvents)
        assertEquals(8L, snapshot.duplicateEvents)
        assertEquals(7L, snapshot.skippedRecords)
    }

    @Test
    fun cancellationPreservesCompletedFileAndMarksOnlyRemainingFiles() = runBlocking {
        val dao = db.importJobDao()
        dao.insertRun(
            ImportRunEntity(
                id = "cancel-run",
                createdAtEpochMs = 1,
                updatedAtEpochMs = 1,
                status = "RUNNING",
                totalDocuments = 2,
            ),
        )
        dao.insertDocuments(
            listOf(
                ImportDocumentDiagnosticEntity(
                    runId = "cancel-run",
                    position = 0,
                    uri = "content://history/completed",
                    displayName = "completed.json",
                    status = "COMPLETED",
                    processedRecords = 10,
                    insertedEvents = 10,
                    updatedAtEpochMs = 1,
                ),
                ImportDocumentDiagnosticEntity(
                    runId = "cancel-run",
                    position = 1,
                    uri = "content://history/pending",
                    displayName = "pending.json",
                    status = "RUNNING",
                    updatedAtEpochMs = 1,
                ),
            ),
        )

        dao.cancelIncompleteDocuments("cancel-run", 2)

        val all = dao.observeDocuments("cancel-run").first()
        assertEquals("COMPLETED", all[0].status)
        assertEquals("CANCELLED", all[1].status)
        assertTrue(dao.getIncompleteDocuments("cancel-run").all { it.position != 0 })
    }
}
