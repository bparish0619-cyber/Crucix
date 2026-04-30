# Crucix Android

Mobile companion app for the [Crucix](https://github.com/bparish0619-cyber/Crucix) OSINT intelligence dashboard.

## Features

- **Dashboard**: Full WebView client connecting to your running Crucix server with pull-to-refresh and SSE support
- **Settings**: Encrypted storage for all 20 Crucix API keys/config values, organized by category with direct links to each registration page
- **Help**: Complete setup guide, key priority tiers, and 12-item expandable FAQ

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 26+ (API 26 minimum, targets API 34)
- A running Crucix server on your network or a remote URL

## Build Instructions

```bash
# Clone this repo
git clone <this-repo>
cd CrucixAndroid

# Open in Android Studio OR build from command line:
./gradlew assembleDebug

# Install on connected device:
./gradlew installDebug
```

If on Windows without the wrapper, generate it first:
```
gradle wrapper --gradle-version 8.4
```

## Architecture

```
CrucixAndroid/
├── app/src/main/java/com/crucix/android/
│   ├── MainActivity.kt              — Single-activity host, bottom nav
│   ├── data/
│   │   ├── CrucixConfig.kt          — Config data class + .env serializer
│   │   └── PreferencesManager.kt    — EncryptedSharedPreferences wrapper
│   └── ui/
│       ├── dashboard/DashboardFragment.kt   — WebView + error/loading states
│       ├── settings/SettingsFragment.kt     — API key management UI
│       ├── settings/ApiKeyItem.kt           — Data models for settings rows
│       └── help/HelpFragment.kt             — Setup guide + expandable FAQ
```

## Key Storage

All API keys are stored using `EncryptedSharedPreferences` (AES-256-GCM via Jetpack Security).
Keys are stored exclusively on-device and never transmitted anywhere by the app.

The **Copy .env** button generates the complete `.env` file text to your clipboard,
which you paste into your Crucix server's `.env` file manually.

## API Keys Managed

| Category | Keys |
|----------|------|
| Server Connection | Server URL, Port, Refresh Interval |
| Core Data (free) | FRED, NASA FIRMS, EIA |
| Extended Data | ACLED (email+password), AISstream, ADS-B Exchange |
| LLM | Provider, API Key, Model override |
| Telegram | Bot Token, Chat ID, Extra Channels, Poll Interval |
| Discord | Bot Token, Channel ID, Guild ID, Webhook URL |

## License

MIT — same as upstream Crucix project
