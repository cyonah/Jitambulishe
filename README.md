# Jitambulishe — Personal passive caller identification

This build is configured for personal sideloaded use on Android 11+ and is designed to stay out of the phone's live calling path.

## What this build does

- Does **not** register a `CallScreeningService`.
- Does **not** request the default caller-ID / call-screening role.
- Does **not** answer, reject, silence, or modify incoming calls.
- Reads completed call records from Android's call log when permission is granted.
- Runs a WorkManager scan when the app opens and periodically (15-minute minimum interval; Android may defer it).
- Stores imported call metadata in Room and displays recent calls in the app.
- Matches call numbers against the local saved-number list.
- Can post low-importance notifications after a call has been imported.
- Provides a manual Truecaller web lookup shortcut for each imported call.

## Important Android 16 permission limitation

`READ_CALL_LOG` is a restricted Android permission. Requesting it at runtime does not guarantee Android will grant it; availability depends on the installer/device policy and applicable Android restrictions. This is a personal sideload build, not a guarantee that call-log access will be allowed on every Android 16 device.

If access is denied, the app remains usable for manual number entry and manual Truecaller lookups, but automatic post-call import will not work. The app does not attempt to bypass Android's permission model.

`POST_NOTIFICATIONS` is separately requested on Android 13+. If it is denied, imported calls can still appear in the app when call-log access is available.

## Truecaller integration

The app does not have access to Truecaller's private caller-identification database through a documented general-purpose reverse-number lookup API. The included shortcut opens Truecaller's public search page; it does not silently query or scrape Truecaller. Automatic database lookup requires an authorized API/partner arrangement.

## Build

- Android Studio
- JDK 17
- Android SDK 35
- Gradle wrapper distribution 8.9

Open the project in Android Studio, sync Gradle, and build the `debug` APK.

## Privacy

Call-log entries and local blocklist records are stored on-device in Room. The app does not upload call history or contact data.
