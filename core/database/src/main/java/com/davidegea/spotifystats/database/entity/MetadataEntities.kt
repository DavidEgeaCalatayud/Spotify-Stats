package com.davidegea.spotifystats.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "track_metadata",
    primaryKeys = ["track_id"],
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["track_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["provider"]),
        Index(value = ["refreshed_at"]),
    ],
)
data class TrackMetadataEntity(
    @ColumnInfo(name = "track_id")
    val trackId: Long,
    val provider: String,
    @ColumnInfo(name = "provider_track_id")
    val providerTrackId: String?,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long?,
    @ColumnInfo(name = "artwork_path")
    val artworkPath: String?,
    @ColumnInfo(name = "refreshed_at")
    val refreshedAtEpochMs: Long,
)

@Entity(
    tableName = "album_metadata",
    primaryKeys = ["album_id"],
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["album_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["provider"]),
        Index(value = ["refreshed_at"]),
    ],
)
data class AlbumMetadataEntity(
    @ColumnInfo(name = "album_id")
    val albumId: Long,
    val provider: String,
    @ColumnInfo(name = "release_date")
    val releaseDate: String?,
    @ColumnInfo(name = "artwork_path")
    val artworkPath: String?,
    @ColumnInfo(name = "refreshed_at")
    val refreshedAtEpochMs: Long,
)
