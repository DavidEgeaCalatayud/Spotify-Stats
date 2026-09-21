# V1 release candidate

This document describes the reproducible release path for Spotify Stats. It does not claim that physical-device acceptance has already been completed.

## Versioning

The application version is controlled from `gradle.properties`:

```properties
APP_VERSION_CODE=1
APP_VERSION_NAME=0.1.0-rc.1
```

Increase `APP_VERSION_CODE` for every distributed build. Use semantic versions for `APP_VERSION_NAME`.

## Local verification

```bash
./gradlew --no-daemon \
  :domain:testDebugUnitTest \
  :core:database:testDebugUnitTest \
  :data:import:testDebugUnitTest \
  :data:history:testDebugUnitTest \
  :data:privacy:testDebugUnitTest \
  :app:lintDebug \
  :app:testDebugUnitTest \
  :app:assembleDebug \
  :app:assembleRelease \
  :app:bundleRelease
```

Without signing credentials, the release APK/AAB are release-candidate build artifacts and must not be published to Google Play.

## Release signing

No keystore or password belongs in Git, Gradle files or source code. A signed release can be produced when all four environment variables are present:

```text
ANDROID_KEYSTORE_PATH
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

The Gradle configuration only enables the release signing config when all values are available.

For GitHub's manual **Signed Android Release Candidate** workflow, configure these repository/environment secrets:

```text
ANDROID_KEYSTORE_BASE64
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

`ANDROID_KEYSTORE_BASE64` is the base64 representation of the user-owned keystore. The workflow materializes it only in the runner temporary directory, verifies the APK/AAB signatures, checks the release APK privacy contract, emits SHA-256 checksums, uploads the signed RC artifact and deletes the temporary keystore file. Do not commit the keystore or its passwords.

## Physical-device acceptance

Run these checks on at least one current Android device and one API 24-compatible device before V1:

- clean install and first-run onboarding;
- import a real Spotify Extended Streaming History ZIP;
- re-import the same data and verify deduplication;
- cancel an import and safely restart it;
- background/foreground the application during a large import;
- rotate/resume during navigation where the device permits rotation;
- Home, Library, global search, entity details, Insights and Calendar;
- generate Wrapped cards and share through real Android targets;
- export backup, delete local data, restore backup and compare totals;
- verify airplane-mode operation after import;
- verify no unexpected network permission or traffic;
- accessibility: font scaling, TalkBack labels, touch targets and contrast.

Record Android version, device model, export event count, import elapsed time and observed failures.

Use [the structured V1 device acceptance report](device-acceptance-v1.md) so every RC is evaluated against the same criteria.

## Profiling on Android

Useful commands while validating an installed debug/RC build:

```bash
adb shell dumpsys meminfo com.davidegea.spotifystats
adb shell dumpsys gfxinfo com.davidegea.spotifystats reset
adb shell dumpsys gfxinfo com.davidegea.spotifystats
adb shell dumpsys batterystats --reset
# exercise the import/application
adb shell dumpsys batterystats com.davidegea.spotifystats
```

For import timing, use the app's progress/report plus Android Studio Profiler or Perfetto. Desktop SQLite benchmark results must not be presented as Android performance measurements.

## Play distribution checklist

Before public release:

- [ ] choose and document the repository license;
- [ ] final app name/icon/branding;
- [ ] signed AAB generated from protected CI/local signing inputs;
- [ ] privacy policy reviewed and hosted at a public URL;
- [ ] screenshots and store listing (start from [the V1 listing draft](play-store-listing-v1.md));
- [ ] internal/closed Play testing;
- [ ] data safety form checked against the actual shipped permissions/features;
- [ ] crash/ANR acceptance on the selected device matrix;
- [ ] version code incremented for every upload.

Spotify Stats is an unofficial application and is not affiliated with or endorsed by Spotify.


## CI privacy guardrail

The Android CI inspects the built release APK, not only the source manifest. The build fails if the merged release manifest contains `android.permission.INTERNET`. This protects the offline V1 against accidental permission additions from future dependencies.

## Release candidate acceptance

A release-candidate artifact may be generated automatically by CI, but it is not a V1 release until:

1. CI is green on the exact commit;
2. the signed build is tested on physical Android hardware;
3. the device acceptance report has no open release blockers;
4. backup/restore and real share targets have been exercised;
5. the final Play Console data-safety answers are checked against the uploaded binary.
