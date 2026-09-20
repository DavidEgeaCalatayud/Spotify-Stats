# Optional integrations and remaining gates

## Spotify beta adapter (not implemented)

The public offline build deliberately has no INTERNET permission. Add a separate
`data:spotify` adapter and a beta build variant so import-only installations retain that property.

Activation requires a Spotify developer application's public client ID and registered
redirect URI. Never request or embed a client secret in Android. Authorize with PKCE S256,
a cryptographically random state/verifier, expiry and one-time callback consumption.
Store refresh/access tokens only in AES-GCM storage whose wrapping key resides in Android
Keystore; exclude them from exports and logs. Clear credentials and cancel unique workers
on disconnect. Refresh under a mutex; honor token rotation and terminal invalid-grant errors.

Use minimal `user-read-recently-played` scope. Handle 401 refresh, 403 eligibility,
429 Retry-After, offline retries and bounded cursor pagination. Schedule opt-in unique
WorkManager work; report last successful sync, failures and gaps rather than claiming
continuous monitoring or complete recovery of all listening.

**Do not implement the original cross-source fingerprint assumption literally.**
The export contains actual listened milliseconds and a stream-end timestamp. Recently
Played returns a playback timestamp and catalogue `duration_ms`, not actual listened
milliseconds or a reliable skip signal. These are different observations. A beta schema
must preserve provenance/time semantics and unknown duration, and reconcile candidates
without converting catalogue length into fabricated listening time or collapsing real repeats.
Test same song repeated, partial listens, overlaps, reimports and late arriving exports.

As checked on 20 September 2026, Spotify's development mode documentation describes a
five-user authenticated cap and requires the app owner to have Premium, with grandfathering
rules for some existing apps. July 2026 quota changes add per-developer accounting.
Treat quotas and public eligibility as external constraints, not a foundation for the product.

Sources: [PKCE](https://developer.spotify.com/documentation/web-api/tutorials/code-pkce-flow),
[Recently Played](https://developer.spotify.com/documentation/web-api/reference/get-recently-played),
[quota modes](https://developer.spotify.com/documentation/web-api/concepts/quota-modes),
[July 2026 quota changes](https://developer.spotify.com/blog/2026-07-23-web-api-quota-updates).

## Database encryption (evaluated; not enabled)

The current database is plaintext within Android private app storage, with platform
backup disabled. The privacy UI states this explicitly. FTS and temporary restore
files are also plaintext private files. Portable ZIP backups are not encrypted.

SQLCipher is the intended candidate, subject to device verification. A safe opt-in rollout
needs a supported native library (including 16 KiB Android page-size support), random
Keystore-wrapped database key, recoverable staged conversion of an existing database,
free-space checks, crash recovery and rollback, verification before swapping files, and
coordinated closure/recreation of Room and all observers. Never overwrite a user's only
working database or enable destructive migration to make encryption work.

Acceptance matrix: fresh plain/encrypted installs; populated v1/v2 conversion both ways;
process death at each conversion step; wrong/invalidated key; disk exhaustion; FTS integrity;
backup/restore across devices; key loss; API 24 and modern 16 KiB devices. Portable encrypted
backups need a separate user-secret-based recovery design because a device Keystore key
cannot decrypt them on a replacement phone. This remains a separate deliverable.

## Android MediaSession capture

Research only. It cannot see playback on another device and cannot replace Extended
History as the source of truth. Any experiment must disclose the notification/media-access
permission, require explicit opt-in, and keep uncertain observations distinct from verified
exported listening. No capture permission or background service is added by this change.
