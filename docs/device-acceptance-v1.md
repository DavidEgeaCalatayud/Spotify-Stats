# V1 physical-device acceptance report

Use one copy of this report per release candidate and device. Do not mark V1 accepted from desktop/CI results alone.

## Build under test

- Version name:
- Version code:
- Commit SHA:
- Artifact: APK / AAB-derived install
- Signed: yes / no
- Install path: clean install / upgrade

## Device

- Manufacturer/model:
- Android version:
- API level:
- Free storage before test:
- Battery level before test:
- Power saver enabled: yes / no

## Spotify export

- Source: Extended Streaming History
- Container: ZIP / JSON files
- Compressed size:
- Uncompressed size:
- Number of files:
- Imported records:
- New play events:
- Duplicate events:
- Skipped/invalid records:

## Large import

- Start time:
- End time:
- Elapsed:
- Peak PSS / memory observation:
- Battery before:
- Battery after:
- Device temperature observation:
- ANR/crash: yes / no
- Foreground notification visible: yes / no
- Background/resume successful: yes / no
- Process killed and resumed safely: yes / no / not tested
- Cancellation and resume successful: yes / no
- Re-import deduplicated correctly: yes / no

## Core navigation and analytics

- Onboarding:
- Home:
- Library rankings:
- Keyset history / Load more:
- Search:
- Track detail:
- Artist detail, including yearly rank history:
- Album detail, including active days/year history:
- Insights:
- Calendar:
- Custom date ranges:
- Wrapped:

For each failure, record exact steps, expected behavior, actual behavior and screenshot/log reference.

## Wrapped share acceptance

Generate both supported V1 share outputs.

- Single 9:16 card renders correctly:
- Multi-card story sequence renders correctly:
- Android Sharesheet opens:
- WhatsApp accepts the image(s):
- Instagram accepts the image(s):
- No unintended files/data are shared:
- Long track/artist/album names remain readable:

## Backup and restore

1. Record event/track/artist/album totals.
2. Export a backup.
3. Delete local history.
4. Verify the app returns to the empty-data state.
5. Restore the backup.
6. Compare totals and representative analytics.

- Export succeeded:
- Delete succeeded:
- Restore succeeded:
- Totals identical:
- Search/detail/calendar representative checks identical:
- Corrupt/unsupported backup rejected without destroying current data:

## Offline/privacy acceptance

- Airplane mode after import: app remains functional
- Home/Library/Insights/Calendar/Details work offline
- Wrapped generation works offline
- Android manifest has no INTERNET permission
- No unexpected network traffic observed

## Accessibility acceptance

Test at default font size and at least one enlarged font setting.

- No critical clipped text:
- Bottom navigation remains usable:
- Minimum touch targets acceptable:
- TalkBack announces navigation/actions meaningfully:
- Calendar day controls have meaningful labels:
- Light/dark contrast acceptable:

## Result

- [ ] PASS — suitable for V1 release
- [ ] FAIL — release blocker(s) remain

### Blocking findings

-

### Non-blocking findings / V1.1 candidates

-
