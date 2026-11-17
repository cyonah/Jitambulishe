# Jitambulishe – Passive Post-call Edition

Jitambulishe is a personal, offline-first Android caller companion. It does not act as the dialer or call-screening service. It does not answer, reject, silence, or alter incoming calls.

## v1.1.1 changes

- Completed-call notifications now include an explicit Android `PendingIntent`.
- Tapping a notification opens the app and selects the matching call record.
- The app displays call number, caller label, call type, date and duration in an in-app detail panel.
- Notification intents are unique per call to avoid opening the wrong call when several notifications exist.
- The app remains passive; no live call controls were added.

## Truecaller integration status

The app currently offers a manual Truecaller web lookup. It does not automatically query the Truecaller caller database.

Truecaller's publicly documented Android SDK is intended for consent-based verification and retrieval of the app user's own Truecaller profile. It is not a general reverse-phone-number lookup API for arbitrary incoming callers. Automatic in-app reverse lookup requires a separately authorized provider/API and its documented terms. Do not scrape Truecaller's website or embed unofficial endpoints/API keys in the Android client.

## Build

Use GitHub Actions (`.github/workflows/build.yml`) to run `./gradlew assembleDebug` and download the APK artifact.

## Permissions and Android behavior

Call-log reading requires `READ_CALL_LOG` access. Android may restrict this permission depending on install/distribution conditions. Notifications require notification permission on Android 13+. WorkManager periodic jobs are inexact and can be delayed by the operating system.

## Privacy

Call records are stored locally in Room. No call data is sent to a remote server by this release.
