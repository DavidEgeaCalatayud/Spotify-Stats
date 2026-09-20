package com.davidegea.spotifystats.data.privacy

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import androidx.room.withTransaction
import com.davidegea.spotifystats.database.DatabaseOperationGate
import com.davidegea.spotifystats.database.SpotifyStatsDatabase
import com.davidegea.spotifystats.domain.repository.DataControlRepository
import java.io.File
import java.io.FilterInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LocalDataControlRepository(
    private val context: Context,
    private val database: SpotifyStatsDatabase,
) : DataControlRepository {
    override suspend fun exportBackup(uri: String) = withContext(Dispatchers.IO) {
        DatabaseOperationGate.mutex.withLock {
            val output = requireNotNull(context.contentResolver.openOutputStream(Uri.parse(uri), "wt"))
            ZipOutputStream(output.buffered()).use { zip ->
                zip.putNextEntry(ZipEntry("history.json"))
                val writer = JsonWriter(zip.writer(Charsets.UTF_8))
                database.withTransaction {
                    writer.beginObject().name("format").value(FORMAT).name("version").value(1).name("tables").beginObject()
                    for ((table, columns) in TABLES) {
                        writer.name(table).beginArray()
                        database.openHelper.writableDatabase.query("SELECT * FROM `$table`").use { cursor ->
                            while (cursor.moveToNext()) {
                                currentCoroutineContext().ensureActive()
                                writer.beginObject()
                                for (column in columns) {
                                    writer.name(column)
                                    val index = cursor.getColumnIndexOrThrow(column)
                                    when (cursor.getType(index)) {
                                        Cursor.FIELD_TYPE_NULL -> writer.nullValue()
                                        Cursor.FIELD_TYPE_INTEGER -> writer.value(cursor.getLong(index))
                                        else -> writer.value(cursor.getString(index))
                                    }
                                }
                                writer.endObject()
                            }
                        }
                        writer.endArray()
                    }
                    writer.endObject().endObject()
                    writer.flush()
                }
                zip.closeEntry()
            }
        }
    }

    override suspend fun restoreBackup(uri: String) = withContext(Dispatchers.IO) {
        DatabaseOperationGate.mutex.withLock {
            val temporary = File.createTempFile("restore-", ".db", context.cacheDir)
            try {
                SQLiteDatabase.openOrCreateDatabase(temporary, null).use { staging ->
                    staging.setForeignKeyConstraintsEnabled(true)
                    // The schema comes from our own database, never from the selected file.
                    for (table in TABLES.keys) {
                        database.openHelper.readableDatabase.query("SELECT sql FROM sqlite_master WHERE type='table' AND name=?", arrayOf(table)).use {
                            check(it.moveToFirst())
                            staging.execSQL(it.getString(0))
                        }
                    }
                    // Recreate unique indexes too: duplicate hashes/identities must fail validation before replacement.
                    for (table in TABLES.keys) {
                        database.openHelper.readableDatabase.query("SELECT sql FROM sqlite_master WHERE type='index' AND tbl_name=? AND sql IS NOT NULL", arrayOf(table)).use {
                            while (it.moveToNext()) staging.execSQL(it.getString(0))
                        }
                    }
                    readBackup(uri, staging)
                    staging.rawQuery("PRAGMA integrity_check", null).use { check(it.moveToFirst() && it.getString(0) == "ok") { "Backup integrity check failed" } }
                    staging.rawQuery("PRAGMA foreign_key_check", null).use { check(!it.moveToFirst()) { "Backup has broken references" } }
                    // All-or-nothing replacement. Cancellation or insert failure rolls back the old history.
                    database.withTransaction {
                        val target = database.openHelper.writableDatabase
                        TABLES.keys.reversed().forEach { target.execSQL("DELETE FROM `$it`") }
                        for ((table, columns) in TABLES) {
                            val placeholders = columns.joinToString(",") { "?" }
                            val names = columns.joinToString(",") { "`$it`" }
                            target.compileStatement("INSERT INTO `$table` ($names) VALUES ($placeholders)").use { statement ->
                                staging.rawQuery("SELECT $names FROM `$table`", null).use { cursor ->
                                    while (cursor.moveToNext()) {
                                        currentCoroutineContext().ensureActive()
                                        statement.clearBindings()
                                        columns.indices.forEach { i ->
                                            when (cursor.getType(i)) {
                                                Cursor.FIELD_TYPE_NULL -> statement.bindNull(i + 1)
                                                Cursor.FIELD_TYPE_INTEGER -> statement.bindLong(i + 1, cursor.getLong(i))
                                                else -> statement.bindString(i + 1, cursor.getString(i))
                                            }
                                        }
                                        statement.executeInsert()
                                    }
                                }
                            }
                        }
                    }
                }
            } finally {
                SQLiteDatabase.deleteDatabase(temporary)
            }
        }
    }

    private suspend fun readBackup(uri: String, staging: SQLiteDatabase) {
        val input = requireNotNull(context.contentResolver.openInputStream(Uri.parse(uri)))
        ZipInputStream(input.buffered()).use { zip ->
            require(zip.nextEntry?.name == "history.json") { "Not a Spotify Stats backup" }
            val reader = JsonReader(LimitedInputStream(zip, MAX_BYTES).reader(Charsets.UTF_8))
            reader.beginObject()
            require(reader.nextName() == "format" && reader.nextString() == FORMAT) { "Unknown backup format" }
            require(reader.nextName() == "version" && reader.nextInt() == 1) { "Unsupported backup version" }
            require(reader.nextName() == "tables")
            reader.beginObject()
            staging.beginTransaction()
            try {
                for ((table, columns) in TABLES) {
                    require(reader.nextName() == table) { "Invalid backup table order" }
                    reader.beginArray()
                    while (reader.hasNext()) {
                        currentCoroutineContext().ensureActive()
                        val values = ContentValues()
                        val seen = mutableSetOf<String>()
                        reader.beginObject()
                        while (reader.hasNext()) {
                            val name = reader.nextName()
                            require(name in columns && seen.add(name)) { "Unknown or duplicate backup field" }
                            when (reader.peek()) {
                                JsonToken.NULL -> { reader.nextNull(); values.putNull(name) }
                                JsonToken.NUMBER -> { require(name in NUMERIC); values.put(name, reader.nextLong()) }
                                JsonToken.STRING -> { require(name !in NUMERIC); val value = reader.nextString(); require(value.length <= 16_384); values.put(name, value) }
                                else -> error("Invalid backup value")
                            }
                        }
                        reader.endObject()
                        require(seen == columns.toSet()) { "Missing backup fields" }
                        validate(table, values)
                        staging.insertOrThrow(table, null, values)
                    }
                    reader.endArray()
                }
                reader.endObject()
                reader.endObject()
                require(reader.peek() == JsonToken.END_DOCUMENT) { "Trailing backup data" }
                zip.closeEntry()
                require(zip.nextEntry == null) { "Unexpected backup entries" }
                staging.setTransactionSuccessful()
            } finally { staging.endTransaction() }
        }
    }

    private fun validate(table: String, values: ContentValues) {
        for (key in listOf("id", "track_id", "artist_id", "album_id")) {
            if (values.get(key) != null) require(values.getAsLong(key) > 0) { "Invalid identity" }
        }
        if (table == "play_events") {
            require(values.getAsLong("ms_played") in 0..86_400_000L) { "Invalid listening duration" }
            require(values.getAsLong("played_at") in 0..7_289_654_399_999L) { "Invalid playback date" }
            require(values.getAsString("event_hash").matches(Regex("[a-f0-9]{64}"))) { "Invalid event hash" }
            require(values.getAsString("source") in setOf("SPOTIFY_EXPORT", "SPOTIFY_API", "ANDROID_CAPTURE"))
            listOf("shuffle", "skipped", "offline", "private_session").forEach {
                if (values.get(it) != null) require(values.getAsLong(it) in 0..1)
            }
        }
        if (table == "tracks" && values.get("duration_ms") != null) require(values.getAsLong("duration_ms") in 1..86_400_000L)
    }

    override suspend fun deleteHistory() = withContext(Dispatchers.IO) {
        DatabaseOperationGate.mutex.withLock {
            database.withTransaction {
                TABLES.keys.reversed().forEach { database.openHelper.writableDatabase.execSQL("DELETE FROM `$it`") }
            }
            // Remove freed content from the DB and truncate the WAL after logical deletion.
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }
            database.openHelper.writableDatabase.execSQL("VACUUM")
        }
    }

    companion object {
        private const val FORMAT = "spotify-stats-local-backup"
        private const val MAX_BYTES = 1_073_741_824L
        private val TABLES = linkedMapOf(
            "artists" to listOf("id", "spotify_id", "identity_key", "name", "normalized_name"),
            "albums" to listOf("id", "spotify_id", "identity_key", "name", "release_date"),
            "tracks" to listOf("id", "spotify_uri", "identity_key", "name", "album_id", "duration_ms"),
            "track_artists" to listOf("track_id", "artist_id", "position"),
            "play_events" to listOf("id", "event_hash", "track_id", "played_at", "ms_played", "platform", "country", "reason_start", "reason_end", "shuffle", "skipped", "offline", "private_session", "source"),
        )
        private val NUMERIC = setOf("id", "track_id", "artist_id", "album_id", "duration_ms", "position", "played_at", "ms_played", "shuffle", "skipped", "offline", "private_session")
    }
}

internal class LimitedInputStream(input: InputStream, private val limit: Long) : FilterInputStream(input) {
    private var consumed = 0L
    override fun read(): Int = super.read().also { if (it >= 0) count(1) }
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int = `in`.read(buffer, offset, length).also { if (it > 0) count(it) }
    private fun count(amount: Int) { consumed += amount; require(consumed <= limit) { "Backup exceeds size limit" } }
}
