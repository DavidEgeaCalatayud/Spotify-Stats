# Spotify Stats

A local-first Android application that turns Spotify Extended Streaming History into detailed, private listening analytics.

## Product principles

- **Local-first:** raw history and analytics live in Room/SQLite on the device.
- **Privacy-first:** no application backend, account system or cloud database is required.
- **Offline-first:** imported history remains usable without a network connection.
- **Spotify API is optional:** the core product must work from official history exports even if API quotas change.
- **Explainable metrics:** raw play events, meaningful listens, completions and skips are separate concepts.

## Architecture

```text
Spotify Extended History
          |
          v
      Importer
          |
          v
     Room / SQLite
          |
          v
   Analytics Engine
          |
          v
Compose UI / Wrapped
```

Module boundaries:

```text
app
 |-- core:designsystem
 |-- domain
 |    \-- core:model
 |-- data:history
 |    |-- domain
 |    \-- core:database
 |-- data:import
 |    |-- domain
 |    \-- core:database
 \-- core:database
```

The UI follows **UI -> ViewModel -> UseCase -> Repository -> Room/import/API**. Spotify OAuth/API integration will be an optional adapter, not the source of truth for the product.

## Current status

The MVP foundation currently includes:

- Kotlin + Jetpack Compose + Material 3
- Clean Architecture / MVVM-oriented module boundaries
- Hilt dependency injection
- Room/SQLite schema v1
- normalized albums, artists, tracks and track-artists
- deduplicated play events via a unique event hash
- streaming JSON and ZIP import through Android's document picker
- import progress and import summary
- indexed track, artist and album rankings
- 7-day, 30-day, current-year and all-time filters
- track drill-down metrics with yearly listening history
- artist drill-down metrics with top-song rankings
- four-destination navigation: Home / Library / Insights / You
- GitHub Actions CI and Dependabot

The next MVP slices are richer Home analytics, album detail, history browsing and search.

## Privacy model

Spotify exports can contain fields that are not required for analytics. **IP addresses and user-agent values are intentionally absent from the database schema.** The application parses only the minimum fields needed for product functionality.

See [docs/privacy.md](docs/privacy.md).

## Build

Requirements:

- JDK 17
- Android SDK API 36
- Android Build Tools 36.0.0
- Gradle 9.4.1

The repository includes the Gradle Wrapper pinned to Gradle 9.4.1 with distribution checksum verification.

```bash
./gradlew   :domain:testDebugUnitTest   :data:import:testDebugUnitTest   :app:lintDebug   :app:testDebugUnitTest   :app:assembleDebug
```

## Roadmap

The roadmap is tracked in [docs/roadmap.md](docs/roadmap.md). The first MVP is intentionally narrow: import Extended Streaming History, persist it locally, expose overview/rankings/details, support date filters, and work offline.

## License

No open-source license has been selected yet.
