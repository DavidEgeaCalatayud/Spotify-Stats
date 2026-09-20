# Roadmap

## Epic 1 — Foundation

- [x] Initialize Android project
- [x] Configure Compose + Material 3
- [x] Add Room
- [x] Configure Hilt
- [x] Establish module boundaries
- [x] Add four-destination navigation
- [x] Add Android CI

## Epic 2 — Data model

- [x] Tracks table
- [x] Artists table
- [x] Albums table
- [x] Track-artists relation
- [x] Play-events table
- [x] Event-hash uniqueness
- [x] Core database indexes
- [x] Repository boundary
- [ ] Migration tests

## Epic 3 — Spotify Extended History import

- [x] Android Storage Access Framework document picker
- [x] Multiple JSON and ZIP support
- [x] Streaming JSON parser
- [ ] Export validation
- [x] Privacy-field filtering
- [x] Entity normalization
- [x] SHA-256 event fingerprinting
- [x] Batched transactional inserts
- [x] Import progress
- [x] Import summary / error report
- [ ] Re-import idempotency tests

## Epic 4 — Core analytics

- [x] Total listening time
- [x] Raw event counts
- [ ] Meaningful-listen threshold
- [x] Track rankings
- [x] Artist rankings
- [x] Album rankings
- [x] Date filters
- [ ] Daily aggregation cache

## Epic 5 — Core UI

- [x] Navigation shell
- [x] Home overview foundation
- [ ] Import onboarding
- [x] Songs
- [x] Song detail
- [x] Artists
- [x] Artist detail
- [x] Albums
- [x] Album detail
- [x] History

## Epic 6 — Advanced analytics

- [ ] Listening heatmap
- [ ] Session detection
- [ ] Skip analysis
- [ ] Completion analysis
- [ ] Obsession detection
- [ ] Rediscovery detection
- [ ] Discovery detection
- [ ] Listening trends
- [ ] Calendar

## Epic 7 — Wrapped

- [ ] Wrapped analytics engine
- [ ] Year recap
- [ ] Monthly recap
- [ ] Custom date range recap
- [ ] Local 9:16 share cards

## Epic 8 — Optional Spotify integration

- [ ] OAuth Authorization Code + PKCE
- [ ] Keystore-backed token storage
- [ ] Recently Played synchronization
- [ ] WorkManager scheduling
- [ ] Cross-source deduplication
- [ ] Sync state UI

## Epic 9 — Privacy and durability

- [ ] Export backup
- [ ] Restore backup
- [ ] Delete all local data
- [ ] Privacy screen
- [ ] Evaluate SQLCipher / encrypted SQLite
- [ ] Database integrity checks

## MVP exit criteria

The MVP is releasable when a user can install the app, import official Spotify Extended Streaming History, keep all data on-device, view overview/song/artist/album analytics with date filters, browse recent listening history, and use the app fully offline after import.
