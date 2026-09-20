# Privacy

## Guarantee

The product is designed so a user can use the complete core experience without creating an account or sending listening history to an application server.

## Stored locally

Expected local fields include:

- track / album / artist identity
- Spotify URI or ID when present
- playback timestamp
- milliseconds played
- platform and country when supplied
- start/end reason
- shuffle / skip / offline / private-session flags
- source of the event

## Deliberately discarded

The importer must not persist:

- IP address
- user-agent string
- credentials
- Spotify password
- unrelated fields from the export

## Tokens

When optional Spotify live sync is added, OAuth tokens must be protected using Android Keystore-backed storage. Tokens must never be written to logs or the Room history database.

## Backups

Backups will be explicit user actions through Android's Storage Access Framework. The user chooses the destination. Spotify Stats will not operate its own backup server.

## Deletion

A future privacy screen will provide a destructive local-data deletion action. Database deletion must be immediate and must not depend on network availability.
