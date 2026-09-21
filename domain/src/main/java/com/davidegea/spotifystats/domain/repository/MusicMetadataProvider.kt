package com.davidegea.spotifystats.domain.repository

import com.davidegea.spotifystats.domain.model.MetadataLookup
import com.davidegea.spotifystats.domain.model.ProviderTrackMetadata

interface MusicMetadataProvider {
    val id: String

    /**
     * Returns null when the provider cannot resolve this track.
     *
     * Implementations must treat provider/network failure as a normal optional-adapter failure;
     * imported listening history remains the source of truth.
     */
    suspend fun lookup(track: MetadataLookup): ProviderTrackMetadata?
}
