package com.davidegea.spotifystats.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        for (table in listOf("tracks", "artists", "albums")) {
            val fts = "${table}_fts"
            db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `$fts` USING FTS4(`name` TEXT NOT NULL, tokenize=unicode61, content=`$table`)")
            db.execSQL("INSERT INTO `$fts`(`$fts`) VALUES('rebuild')")
        }
        // Room recreates external-content FTS triggers after this migration.
    }
}


val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS import_runs (
                id TEXT NOT NULL PRIMARY KEY,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                status TEXT NOT NULL,
                total_documents INTEGER NOT NULL,
                processed_records INTEGER NOT NULL,
                inserted_events INTEGER NOT NULL,
                duplicate_events INTEGER NOT NULL,
                skipped_records INTEGER NOT NULL,
                failed_documents INTEGER NOT NULL,
                last_error TEXT
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_import_runs_created_at ON import_runs(created_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_import_runs_status ON import_runs(status)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS import_documents (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                run_id TEXT NOT NULL,
                position INTEGER NOT NULL,
                uri TEXT NOT NULL,
                display_name TEXT,
                status TEXT NOT NULL,
                processed_records INTEGER NOT NULL,
                inserted_events INTEGER NOT NULL,
                duplicate_events INTEGER NOT NULL,
                skipped_records INTEGER NOT NULL,
                error_message TEXT,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY(run_id) REFERENCES import_runs(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_import_documents_run_id_position " +
                "ON import_documents(run_id, position)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_import_documents_run_id_status " +
                "ON import_documents(run_id, status)",
        )
    }
}


val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS track_metadata (
                track_id INTEGER NOT NULL PRIMARY KEY,
                provider TEXT NOT NULL,
                provider_track_id TEXT,
                duration_ms INTEGER,
                artwork_path TEXT,
                refreshed_at INTEGER NOT NULL,
                FOREIGN KEY(track_id) REFERENCES tracks(id)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_track_metadata_provider " +
                "ON track_metadata(provider)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_track_metadata_refreshed_at " +
                "ON track_metadata(refreshed_at)",
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS album_metadata (
                album_id INTEGER NOT NULL PRIMARY KEY,
                provider TEXT NOT NULL,
                release_date TEXT,
                artwork_path TEXT,
                refreshed_at INTEGER NOT NULL,
                FOREIGN KEY(album_id) REFERENCES albums(id)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_album_metadata_provider " +
                "ON album_metadata(provider)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_album_metadata_refreshed_at " +
                "ON album_metadata(refreshed_at)",
        )
    }
}
