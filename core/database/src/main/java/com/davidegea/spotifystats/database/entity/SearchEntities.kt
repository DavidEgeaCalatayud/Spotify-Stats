package com.davidegea.spotifystats.database.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Fts4(contentEntity = TrackEntity::class, tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "tracks_fts")
data class TrackSearchEntity(val name: String)

@Fts4(contentEntity = ArtistEntity::class, tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "artists_fts")
data class ArtistSearchEntity(val name: String)

@Fts4(contentEntity = AlbumEntity::class, tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "albums_fts")
data class AlbumSearchEntity(val name: String)
