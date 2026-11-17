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

