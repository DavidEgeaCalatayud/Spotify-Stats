# Architecture

## Goals

Spotify Stats is designed as a long-lived local analytics product rather than a thin Spotify API client. The architecture must continue to work if Spotify changes API quotas or removes endpoints.

## Dependency rule

Dependencies point inward:

```text
Compose UI
   |
ViewModel
   |
Use case
   |
Domain repository interface
   |
Data implementation
   |
Room / import / optional Spotify API
```

Domain code does not depend on Android UI or network APIs. Data adapters implement domain contracts.

## Modules

### app
Composition root, Android lifecycle, Hilt graph, navigation and feature UI.

### core:model
Cross-layer value types that have no feature orchestration.

### core:database
Room database, entities, DAOs, type converters, schema exports and migrations.

### core:designsystem
Compose theme and reusable presentation primitives.

### domain
Repository contracts, analytics models and use cases.

### data:history
Room-backed implementation of listening-history contracts. The streaming import adapter will be added here or split into data:import if its surface grows.

## Data model

The storage model is normalized around:

- albums
- artists
- tracks
- track_artists
- play_events

Play events reference a track and contain only analytics-relevant playback metadata. `event_hash` is unique to make imports idempotent.

Time is stored as epoch milliseconds in UTC. UI formatting converts it to the user's timezone.

## Performance strategy

The raw event table remains the source of truth. Composite indexes cover the dominant access pattern: entity + time range.

As datasets grow, derived daily aggregate tables will be introduced for track, artist, album and global statistics. They are caches and can be rebuilt from raw events.

## Spotify integration

Extended Streaming History is the primary universal input. Spotify Web API + OAuth PKCE is an optional synchronization adapter. Android media-session capture may later be explored as an additional device-local signal.

No product-critical capability may depend exclusively on Spotify live API access.
