# Privacy policy

_Last updated: 21 September 2026_

Spotify Stats is a local-first Android application for analysing a user's Spotify Extended Streaming History.

## Data processed

When a user explicitly selects a Spotify history export, the app may process listening-event fields such as timestamps, track, artist, album, Spotify URI, listening duration, platform, country, playback reason, shuffle, skip, offline and private-session flags.

The app deliberately does not store IP-address or user-agent fields from Spotify exports.

## Where data goes

The offline V1 stores imported history and derived analytics in the application's local Room/SQLite database on the Android device. The offline V1 does not require an account and does not upload listening history to an application backend.

The Android manifest for the offline V1 does not request Internet access.

## Backups and sharing

A user may explicitly export a portable backup using Android's document/storage mechanisms. Once exported, that copy is controlled by the storage location or provider chosen by the user.

Wrapped/share images are generated locally and are only sent to another application after the user invokes the Android share chooser.

## Deletion

The app provides a local-data deletion action. This removes the app-managed listening database and cached share images. It cannot delete copies the user previously exported to another folder, drive or application.

## Optional future integrations

Spotify API synchronization, optional encrypted storage and other network-backed features are not part of the current offline V1. If such features are released, this policy and the application's permission/data-safety declarations must be updated before distribution.

## Third parties

Spotify Stats is an unofficial application and is not affiliated with or endorsed by Spotify. Spotify exports are obtained directly by users through Spotify's own data-export process.

## Contact

Before public store distribution, the project owner must add a public support/privacy contact and host this policy at a stable public URL.
