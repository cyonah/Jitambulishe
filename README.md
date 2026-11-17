# Jitambulishe

A personal, offline-first caller ID and spam blocker for Android, built for Kenya.

**Jitambulishe** is Swahili for "identify yourself" — a fitting name for a tool that tells you who is calling before you pick up.

## What It Does

- **Screens calls instantly** using `CallScreeningService` (no blocking network calls on the call path)
- **Blocks spoofed numbers** via STIR/SHAKEN carrier verification (Android 11+)
- **Keeps a local spam database** (Room/SQLite) that works fully offline
- **Manual Truecaller lookup** — one tap opens `truecaller.com/search/ke/{number}`
- **Pluggable Truecaller API client** — a scaffolded stub you can fill in later
- **Optional periodic blocklist sync** via WorkManager

## Requirements

- Android 11+ (API 30) — required for STIR/SHAKEN verification
- Android Studio (Ladybug or newer recommended)
- JDK 17

## Setup

1. Open the project in Android Studio.
2. Let Gradle sync.
3. Run on your Samsung A54 (or any Android 11+ device).
4. On first launch, tap **"Set as default caller ID app"** and grant the requested role.
5. Add spam numbers via the **"Add to blocklist"** screen, or use **"Look up on Truecaller"** for unknown numbers.

## Project Structure

```

app/src/main/java/com/jitambulishe/app/
├── JitambulisheApp.kt              # Application class
├── MainActivity.kt                 # Compose entry point
├── data/
│   ├── SpamNumber.kt               # Room entity
│   ├── SpamDao.kt                  # Room DAO
│   └── AppDatabase.kt              # Room database
├── screening/
│   └── SpamScreeningService.kt     # CallScreeningService
├── truecaller/
│   └── TruecallerClient.kt         # Placeholder API client (fill in later)
├── workers/
│   └── BlocklistSyncWorker.kt      # WorkManager sync job
├── ui/
│   ├── HomeScreen.kt               # Main UI
│   ├── BlocklistScreen.kt          # Manage spam numbers
│   └── theme/                      # Compose theme
└── util/
└── PhoneNumberUtils.kt         # E.164 normalization helpers

```

## Detection Pipeline (priority order)

1. **Emergency numbers** → always allow
2. **User's contacts** → always allow (optional, requires READ_CONTACTS)
3. **STIR/SHAKEN passed** → allow
4. **STIR/SHAKEN failed** → block
5. **Local blocklist** → block
6. **Unknown** → allow + optional Truecaller lookup

## Filling In the Truecaller API Client

Open `truecaller/TruecallerClient.kt`. The `lookup()` method is a stub. When you have an endpoint, replace the stub with your actual HTTP call. The rest of the app already handles the result correctly.

## Privacy

- All call data stays on-device.
- No account, no OTP, no telemetry.
- The Truecaller web button uses Truecaller's own public search page — nothing is sent through any server you don't control.

## License

Personal use. Not for redistribution.
