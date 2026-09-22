# UI/UX V1 Polish

The presentation keeps analytics, Room, import processing, backup validation and the offline contract intact. No network permission, artwork service or database migration is needed.

## Visual foundations

- `StatsTokens`: a 4/8/12/16/24/32 dp spacing scale, forest/mint palette with lilac and peach illustration accents.
- Material shapes: 12/18/26 dp. Both light and dark surfaces are specified.
- `LocalArtwork`: stable decorative artwork from names; not official Spotify covers. No downloads or cache.
- `ContentContainer`: centred, capped at 960 dp. Home uses two columns from 720 dp of content width; navigation switches to a rail from 840 dp. Larger font scales favour stacked content.
- Native back headers, 180–300 ms Compose transitions, optional platform haptics, skeletons and localised empty/error messages.

## Experience

| Area | Behaviour |
| --- | --- |
| Home | Plays and listening time form the hero. Tracks/artists are secondary. Favourites have artwork. Import opens its own screen. |
| Activity | Rounded interactive bars include zero-listening days. At most 30 calendar days, date ticks, peak/quietest dates, accessible previous/next selection, optional preceding-interval comparison. Comparisons retain the domain's equal-duration interval definition. |
| Library | SearchBar, section tabs, podium, artwork rankings and paginated history. Query and tab UI survive recreation. |
| Insights | Listening rhythm, continuous-colour weekday/hour heatmap, day/hour controls, playback proportions and illustrated discovery/repeat stories. Definitions and thresholds are expandable. |
| Calendar | Year navigation and month tabs, real day cells, day bottom sheet and full-day history route. Loading and failed day requests are separate states. |
| Wrapped | Year/month/custom entry and four paged stories. Swipe, previous/next, close/system back, local story and overview sharing. Long content scrolls, including at large font sizes. |
| You | Music/data/privacy/connections/app groups, isolated delete action, unchanged restore/delete confirmations. Persisted light/dark/system theme and EN/ES/system language. |
| Onboarding | Three guided steps with a visible progress indicator, request-history link, local import and optional exploration. |
| Dates | Material range picker. UTC picker dates are converted to local inclusive days using the existing DST-aware DateRanges code. |

`Calendar.weekday` data uses Sunday = 0; the heatmap displays Monday first without changing the domain representation. A null playback proportion remains unknown rather than being displayed as zero.

## Verification

- Existing module tests, real-activity navigation, Android lint, debug/release APK and release AAB remain CI gates.
- `PresentationDataTest` covers silent days, bounded charts and date selection across Madrid DST and negative UTC offsets.
- `PolishedUiTest` exercises ranking tabs/navigation, chart and heatmap selection, leap-day selection, Wrapped controls and sharing reachability at large text sizes. Native Canvas renders are uploaded with verification reports.
- `NavigationSmokeTest` exercises guided onboarding, all primary destinations, secondary headers and persisted theme selection through the actual activity/Hilt/Room graph.
- Both string catalogs must contain the same keys.

Physical-device acceptance still needs the real imported dataset on the user's Xiaomi: scroll/keyboard/gesture behaviour, TalkBack, rotation/background return, import completion, backups and Android share targets. Automated UI renders do not substitute for this check or for release signing.
