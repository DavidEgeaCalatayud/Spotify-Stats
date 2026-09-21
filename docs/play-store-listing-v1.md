# Google Play listing draft — V1

This is publishing copy, not evidence that the app is already available on Google Play.

## Product identity

**Working app name:** Spotify Stats

The launcher artwork is project-owned and intentionally does not reproduce Spotify's logo. Before public store submission, review the final product name and listing against Spotify trademark/branding requirements.

**Category:** Music & Audio

## Short description

Private, offline analytics from your Spotify Extended Streaming History.

## Full description

Turn your Spotify Extended Streaming History into detailed listening statistics directly on your Android device.

Spotify Stats is designed around a local-first model: your imported listening history is stored and analysed on your device. The core app does not require an account, application backend or Internet connection.

Explore:

- top songs, artists and albums across multiple date ranges;
- your complete listening history with scalable pagination;
- track, artist and album detail analytics;
- listening-time trends, sessions and playback behaviour;
- a local listening calendar;
- discoveries, rediscoveries and repeat-listening highlights;
- local Wrapped-style recaps and 9:16 share cards;
- portable backup and validated restore.

### Privacy by design

The offline V1 does not request the Android INTERNET permission. Spotify export fields that are not needed for analytics, including IP address and user-agent information, are intentionally not stored.

Sharing a recap or exporting a backup happens only after you explicitly choose an Android destination.

### Important

Spotify Stats is an unofficial application and is not affiliated with, endorsed by or sponsored by Spotify.

Statistics describe the listening history you imported. Missing export periods can make historical comparisons incomplete.

## Suggested screenshot set

1. Onboarding — local/private import proposition.
2. Home — overview and recent listening.
3. Library — top songs/artists/albums.
4. Search/history — large-history exploration.
5. Insights — heatmap and listening behaviour.
6. Calendar — yearly listening calendar.
7. Track or artist detail.
8. Wrapped — recap.
9. Wrapped — 9:16 story card.
10. You — privacy/backup controls.

Capture screenshots from the final signed RC on a real device; do not use mock data unless the listing clearly represents it as illustrative.

## Data safety draft inputs

Validate these against the exact binary uploaded to Play Console.

- App backend: none in offline V1.
- Account creation: none.
- Advertising SDK: none.
- Analytics/telemetry SDK: none.
- INTERNET permission: absent.
- Imported listening history: processed/stored locally.
- Backup destination: user-selected through Android document APIs.
- Share destination: user-selected through Android Sharesheet.

The Play Console form must be completed from the shipped implementation and current Google Play definitions at submission time.

## Release notes — RC / V1

Initial local-first release:

- import Spotify Extended Streaming History from JSON or ZIP;
- resilient background import with resume and per-file diagnostics;
- offline rankings, search, details, insights and calendar;
- scalable listening history;
- local Wrapped recaps and share cards;
- local backup, restore and deletion controls;
- English and Spanish UI.
