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
