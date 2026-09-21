# Privacy

The offline build has no Internet permission, app accounts, analytics SDK or backend.
Imported listening history and derived analytics remain on the device. Spotify is not
authenticated by this build and IP/user-agent export fields are discarded.

## Stored locally

Track/artist/album identity, Spotify URI when provided, stream-end timestamps, listened
milliseconds, platform/country, playback reasons, optional skip/shuffle/offline/private
flags and source provenance. Names inferred from the export may be ambiguous; no remote
metadata is fetched to resolve them.

Room/SQLite and FTS4 indexes live in Android app-private storage. **They are not SQLCipher
encrypted.** Android platform backup is disabled. Encryption evaluation and the remaining
acceptance gates are documented in [optional-integrations.md](optional-integrations.md).

## Explicit export and sharing

You → Export backup opens Android's Storage Access Framework. A user-selected provider
may be local storage or a cloud service. The app does not operate a backup server.
Backups are unencrypted ZIPs of the five normalized data tables; tokens, raw source files,
IP addresses and user-agent fields are excluded. Keep exported copies in a private place.

Wrapped creates a 1080×1920 PNG locally and opens Android's share chooser. Only recap
metrics and top names are included. A non-exported FileProvider exposes only the recap
cache directory with a temporary read grant. Old recap images are cleaned up when creating
new ones. Sharing to another app is an explicit user action.

## Restore and deletion

Restore requires confirmation. All content is validated in a private temporary SQLite
file, including schema-format version, value types/ranges, unique constraints and foreign
keys. Only then does a single transaction replace current history and reconstruct search.
Failed validation leaves current history intact. Temporary restore files are removed on
normal success, cancellation and exceptions; hard process termination may leave a private
cache file until the next restore cleanup or Android cache eviction.

Delete requires confirmation and removes local music/history, search entries and cached
recaps. It then checkpoints the WAL and vacuums freed database space. Flash storage and
OS backups outside the application's control are not claimed to support forensic erasure.
Exported backups and cards already shared to other apps must be deleted at their destination.
