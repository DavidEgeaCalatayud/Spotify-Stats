package com.davidegea.spotifystats.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.davidegea.spotifystats.model.PlaySource

@Entity(
    tableName = "albums",
    indices = [
        Index(value = ["spotify_id"], unique = true),
        Index(value = ["identity_key"], unique = true),
        Index(value = ["name"]),
    ],
)
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "spotify_id")
    val spotifyId: String?,
    @ColumnInfo(name = "identity_key")
    val identityKey: String,
    val name: String,
    @ColumnInfo(name = "release_date")
    val releaseDate: String?,
)

@Entity(
    tableName = "artists",
    indices = [
        Index(value = ["spotify_id"], unique = true),
        Index(value = ["identity_key"], unique = true),
        Index(value = ["normalized_name"]),
    ],
)
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "spotify_id")
    val spotifyId: String?,
    @ColumnInfo(name = "identity_key")
    val identityKey: String,
    val name: String,
    @ColumnInfo(name = "normalized_name")
    val normalizedName: String,
)

@Entity(
    tableName = "tracks",
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["album_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["spotify_uri"], unique = true),
        Index(value = ["identity_key"], unique = true),
        Index(value = ["album_id"]),
        Index(value = ["name"]),
    ],
)
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "spotify_uri")
    val spotifyUri: String?,
    @ColumnInfo(name = "identity_key")
    val identityKey: String,
    val name: String,
    @ColumnInfo(name = "album_id")
    val albumId: Long?,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long?,
)

@Entity(
    tableName = "track_artists",
    primaryKeys = ["track_id", "artist_id"],
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["track_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["artist_id", "track_id"]),
    ],
)
data class TrackArtistCrossRef(
    @ColumnInfo(name = "track_id")
    val trackId: Long,
    @ColumnInfo(name = "artist_id")
    val artistId: Long,
    val position: Int,
)

@Entity(
    tableName = "play_events",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["track_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["event_hash"], unique = true),
        Index(value = ["played_at"]),
        Index(value = ["track_id", "played_at"]),
        Index(value = ["source"]),
    ],
)
data class PlayEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "event_hash")
    val eventHash: String,
    @ColumnInfo(name = "track_id")
    val trackId: Long,
    @ColumnInfo(name = "played_at")
    val playedAtEpochMs: Long,
    @ColumnInfo(name = "ms_played")
    val msPlayed: Long,
    val platform: String?,
    val country: String?,
    @ColumnInfo(name = "reason_start")
    val reasonStart: String?,
    @ColumnInfo(name = "reason_end")
    val reasonEnd: String?,
    val shuffle: Boolean?,
    val skipped: Boolean?,
    val offline: Boolean?,
    @ColumnInfo(name = "private_session")
    val privateSession: Boolean?,
    val source: PlaySource,
)
