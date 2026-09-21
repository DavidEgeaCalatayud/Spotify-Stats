# Roadmap

Status for the V1 release-candidate cut. Checked items describe code that is implemented and covered by repository verification; physical-device acceptance is tracked separately in issue #34.

## V1 foundation and data

- [x] Kotlin, Compose, Material 3, Hilt and four-destination navigation
- [x] Clean module boundaries and repository/use-case/ViewModel layers
- [x] Normalized tracks, artists, albums, track-artists and play events
- [x] Unique SHA-256 event hashes and entity/time indexes
- [x] Room schema v3 with non-destructive migrations and real Room tests
- [x] CI tests, lint, debug APK, release APK/AAB and downloadable verification reports
- [x] V1-compatible dependency consolidation
- [ ] Physical-device release acceptance
- [ ] Production signing secrets and signed RC acceptance
- [ ] Play internal/closed testing and public distribution

## Import and onboarding

- [x] Database-driven first-run onboarding
- [x] SAF multiple JSON/ZIP selection and streaming parsing
- [x] Privacy-field filtering and export/value validation
- [x] Normalized batched transactional writes
- [x] Progress, cancellation and safe re-import
- [x] Real-database idempotency and rollback/cancellation tests
- [x] Foreground WorkManager execution
- [x] Persisted per-file diagnostics and resume
- [x] Recovery from individually decodable malformed records
- [x] Explicit hard failure/diagnostics for structurally truncated or corrupt files rather than unsafe invented recovery
- [ ] Large-export Android profiling and battery/memory acceptance

## Core analytics and UI

- [x] Overview, track/artist/album rankings and details
- [x] Raw event counts, actual listened time and meaningful-listen threshold
- [x] Known-duration completion with explicit unknown state
- [x] Today, 7d, 30d, 6 months, current year, all time and custom date filters
- [x] Local indexed search across entities
- [x] Scalable keyset-paginated listening history
- [x] Track yearly history, favourite hour and longest daily streak
- [x] Artist all-time, yearly and arbitrary-range ranks
- [x] Album peak month, active days, meaningful listens and yearly history
- [x] EN/ES localization and Android app-language declaration
- [x] Material 3 state/navigation foundation and launcher branding
- [ ] Physical enlarged-font/TalkBack/contrast acceptance
- [ ] Persistent daily aggregate caches only if Android profiling demonstrates a need

## Advanced analytics

- [x] Listening heatmap and skip/shuffle/offline ratios
- [x] Streamed, explainable session estimates
- [x] Discoveries, rediscoveries, repeat and forgotten-track highlights
- [x] Equal-interval listening trends with honest missing/zero baselines
- [x] Quantified repeat-listening delta/multiplier without causal claims
- [x] Interactive calendar and day summaries

## Wrapped

- [x] Local recap engine
- [x] Year, month, rolling and custom-date recaps
- [x] Single local 9:16 share card
- [x] Four-card 9:16 story sequence
- [x] ACTION_SEND_MULTIPLE with FileProvider content URIs
- [ ] Real Instagram/WhatsApp share-target acceptance

## Privacy and durability

- [x] Explicit portable ZIP export
- [x] Validated staged restore and atomic replacement
- [x] Delete local history and cached share images
- [x] Privacy screen
- [x] No INTERNET permission in offline V1
- [x] CI check against accidental INTERNET permission in built release APK
- [ ] Backup/restore physical-device acceptance

## Explicitly post-V1

- [ ] Provider-neutral optional metadata enrichment (#43)
- [ ] Optional SQLCipher database/encrypted backups (#46)
- [ ] Spotify PKCE, Keystore-backed tokens and Recently Played reconciliation (#47)
- [ ] Device-only MediaSession feasibility study (#48)

These remain optional adapters/features and must not compromise the local-first V1.
