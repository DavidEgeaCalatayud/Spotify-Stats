# Spotify Stats

[![Android CI](https://github.com/DavidEgeaCalatayud/Spotify-Stats/actions/workflows/android-ci.yml/badge.svg)](https://github.com/DavidEgeaCalatayud/Spotify-Stats/actions/workflows/android-ci.yml)

A local-first native Android application that turns Spotify Extended Streaming History into detailed, private listening analytics.

**Kotlin · Jetpack Compose · Material 3 · Hilt · Room / SQLite · WorkManager · Clean Architecture / MVVM**


## Product principles

- **Local-first:** raw history and analytics live in Room/SQLite on the device.
- **Privacy-first:** no application backend, account system or cloud database is required.
- **Offline-first:** imported history remains usable without a network connection.
- **Spotify API is optional:** the core product works from official history exports even if API quotas change.
- **Explainable metrics:** raw play events, meaningful listens, completions and skips remain separate concepts.

## Architecture

```text
Spotify Extended History
          |
          v
  Streaming importer
          |
          v
     Room / SQLite
          |
          v
   Analytics engine
          |
          v
Compose UI / Calendar / Wrapped
```

The UI follows **UI -> ViewModel -> UseCase -> Repository -> Room/import/optional adapter**. Spotify OAuth/API integration is intentionally not a source of truth for the offline V1.

## V1 release-candidate status

The current V1 codebase includes:

- Kotlin, Jetpack Compose, Material 3 and Hilt;
- Clean Architecture / MVVM-oriented module boundaries;
- Room/SQLite schema v3 with tested non-destructive migrations;
- normalized albums, artists, tracks and track-artists;
- SHA-256 deduplicated play events;
- Spotify Extended Streaming History JSON/ZIP import;
- streaming parsing with per-record malformed-row recovery;
- foreground WorkManager execution with persisted per-file diagnostics and resume;
- scalable keyset-paginated listening history;
- indexed local search;
- Home, Library, entity details, Insights, Calendar and You/privacy surfaces;
- today, 7d, 30d, 6 months, current year, all-time and custom ranges;
- historical artist ranks and richer album analytics;
- explainable sessions, trends, discoveries, rediscoveries and repeat highlights;
- local Wrapped recaps plus single and multi-card 9:16 sharing;
- validated portable backup/restore and local deletion;
- English and Spanish UI;
- CI tests, lint, debug APK and release APK/AAB artifacts.

The offline V1 intentionally has **no Android INTERNET permission**. CI inspects the generated release APK and fails if that privacy contract changes.

The remaining V1 gate is physical-device acceptance and production signing/distribution. Optional metadata enrichment, SQLCipher, Spotify Live Sync and MediaSession capture are V1.x/V1.1 work and do not block the offline release.

See [the release guide](docs/release.md), [V1 device acceptance report](docs/device-acceptance-v1.md), [Play Store listing draft](docs/play-store-listing-v1.md) and [privacy policy](docs/privacy-policy.md).

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
./gradlew :domain:testDebugUnitTest :core:database:testDebugUnitTest \
  :data:import:testDebugUnitTest :data:history:testDebugUnitTest \
  :data:privacy:testDebugUnitTest :app:testDebugUnitTest \
  :app:lintDebug :app:assembleDebug :app:assembleRelease :app:bundleRelease
```

CI uploads `spotify-stats-debug`, `spotify-stats-release-candidate` and verification reports. A separate manual workflow can produce signed RC artifacts from user-owned GitHub signing secrets; no keystore belongs in the repository.

A reproducible desktop SQL check is also available:

```bash
python scripts/benchmark_sql.py --events 300000
```

Desktop benchmarks validate query design but are not a substitute for Android hardware profiling.

## Roadmap

The current release boundary and post-V1 work are tracked in [docs/roadmap.md](docs/roadmap.md) and issue #34.

## License

No open-source license has been selected yet.
