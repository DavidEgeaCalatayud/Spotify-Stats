# Roadmap

Status after the offline completion branch (PR #31). Checked items describe implemented
code; shipping acceptance and remaining limits are in [the audit](audit-2026-09-20.md).

## Foundation and data

- [x] Kotlin, Compose, Material 3, Hilt and four-destination navigation
- [x] Clean module boundaries and repository/use-case/ViewModel layers
- [x] Normalized tracks, artists, albums, track-artists and play events
- [x] Unique SHA-256 hashes and entity/time indexes
- [x] Schema v2 and non-destructive v1 migration with real Room tests
- [x] CI tests, lint, APK build and downloadable reports
- [ ] Physical-device release acceptance, signing and store distribution

## Import

- [x] SAF multiple JSON/ZIP selection and streaming parsing
- [x] Privacy-field filtering and basic export/value validation
- [x] Normalized batched transactional writes
- [x] Progress, summary, cancellation and safe re-import
- [x] Real-database idempotency and rollback/cancellation tests
- [ ] Resumable background import and persisted per-file diagnostics
- [ ] Recovery from individual malformed records without abandoning the JSON document

## Core analytics and UI

- [x] Overview, track/artist/album rankings and details
- [x] Raw event counts, actual listened time and meaningful-listen threshold
- [x] Known-duration completion with an explicit unknown state
- [x] Today, 7d, 30d, 6 months, current year, all time and custom date filters
- [x] Local indexed search across entities with artist-related matches
- [x] Recent history, load more and Home activity chart
- [x] Track yearly history, favourite hour and longest daily streak
- [x] Artist all-time rank/peak year and album peak month
- [ ] Arbitrary-year artist ranking and album catalogue metadata
- [ ] Persistent daily aggregate caches after Android profiling
- [ ] Paging/keyset history, persisted filters, localization and visual polish

## Advanced analytics

- [x] Listening heatmap and skip/shuffle/offline ratios
- [x] Streamed, explainable session estimates
- [x] Discoveries, rediscoveries, repeat and forgotten-track highlights
- [x] Equal-interval listening trends with honest missing/zero baselines
- [x] Interactive calendar and day summaries

## Wrapped

- [x] Local recap engine
- [x] Year, month, rolling and custom-date recaps
- [x] Local 9:16 cards and Android share chooser
- [ ] Additional designs and share-target acceptance on hardware

## Privacy and durability

- [x] Explicit portable ZIP export
- [x] Validated staged restore and atomic replacement
- [x] Delete local history and cached share images
- [x] Privacy screen; no Internet permission in the offline app
- [x] SQLCipher evaluation and rollout/rollback criteria
- [ ] Optional encrypted database and encrypted portable backups

## Optional Spotify beta and future capture

- [ ] PKCE OAuth, Keystore-backed tokens and disconnect
- [ ] Recently Played adapter with explicit uncertainty/provenance
- [ ] WorkManager scheduling, throttling and sync-state UI
- [ ] Cross-source reconciliation with repeated-song/partial-listen tests
- [ ] Device-only MediaSession feasibility study

These are separate adapters, not requirements for importing and exploring local history.
See [optional-integrations.md](optional-integrations.md). No live adapter is claimed to be
implemented by the offline completion work.
