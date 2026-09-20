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

- [ ] Android Storage Access Framework document picker
- [ ] Multiple JSON and ZIP support
- [ ] Streaming JSON parser
- [ ] Export validation
- [ ] Privacy-field filtering
- [ ] Entity normalization
- [ ] SHA-256 event fingerprinting
- [ ] Batched transactional inserts
- [ ] Import progress
- [ ] Import summary / error report
- [ ] Re-import idempotency tests

## Epic 4 — Core analytics

- [ ] Total listening time
- [ ] Raw event counts
- [ ] Meaningful-listen threshold
- [ ] Track rankings
- [ ] Artist rankings
- [ ] Album rankings
- [ ] Date filters
- [ ] Daily aggregation cache

## Epic 5 — Core UI

- [x] Navigation shell
- [x] Home overview foundation
- [ ] Import onboarding
- [ ] Songs
- [ ] Song detail
- [ ] Artists
- [ ] Artist detail
- [ ] Albums
- [ ] Album detail
- [ ] History

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

The MVP is releasable when a user can install the app, import official Spotify Extended Streaming History, keep all data on-device, view overview/song/artist analytics with date filters, and use the app fully offline after import.
