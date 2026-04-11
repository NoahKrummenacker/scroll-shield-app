# ScrollShield

Android app that automatically blocks Instagram Reels and YouTube Shorts using the Accessibility API.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%208%2B-green.svg)](https://developer.android.com)
[![Flutter](https://img.shields.io/badge/UI-Flutter-blue.svg)](https://flutter.dev)

---

## Features

- Block Instagram Reels when the Reels tab is selected
- Block Reels opened from the main feed or Explore
- Block YouTube Shorts (tab and player)
- Block Shorts launched from the YouTube home screen
- Schedule-based blocking — active only during a defined time window
- PIN protection to prevent disabling the blocker
- 6 themes: Bleu & Jaune, Gris foncé, Gris (default), Clair, Boss Lady, Boss Lady 2
- Daily time limit on Reels and Shorts — free access until budget is exhausted

## Requirements

- Android 8.0+ (API 26)
- Accessibility permission enabled manually in Android Settings
- Flutter 3.x (for the Flutter UI build)

## Project structure

```
scroll-shield-app/
└── flutter_ui/
    ├── lib/
    │   ├── main.dart
    │   ├── screens/
    │   │   ├── home_screen.dart     # Toggles + service status + daily progress
    │   │   └── settings_screen.dart # Theme, schedule, daily limit, PIN
    │   ├── services/
    │   │   └── blocker_channel.dart # MethodChannel bridge
    │   └── theme/
    │       ├── app_colors.dart      # 6 themes
    │       └── theme_provider.dart
    └── android/
        └── app/src/main/kotlin/com/example/scroll_shield/
            ├── BlockerAccessibilityService.kt  # Detection + time tracking
            ├── MainActivity.kt                 # MethodChannel handler
            └── PrefsManager.kt                 # SharedPreferences + PIN (SHA-256)
```

## Build & install

### Flutter app (recommended)

Requires [Flutter](https://docs.flutter.dev/get-started/install) and a connected device with ADB enabled.

```bash
cd flutter_ui
flutter build apk --release
adb install -r build/app/outputs/flutter-apk/app-release.apk
```

Or for a debug build:

```bash
flutter run
```

## CI/CD

Each push to `main` triggers a GitHub Actions workflow that:
- Builds the Flutter APK (release) via `flutter build apk --release`
- Publishes it as `ScrollShield.apk` in the "Latest Build" GitHub release

---

After installing, open ScrollShield and tap **Activer dans les paramètres d'accessibilité** to enable the service.

## How it works

The app registers an `AccessibilityService` that listens to window events from Instagram and YouTube. When short-form video content is detected, it navigates to the app's home screen instead.

Detection uses view IDs from each app's UI tree. These IDs may change after app updates.

The Flutter UI communicates with the native Android layer via a `MethodChannel` (`com.example.contentblocker/blocker`) to read/write preferences, check service status, and manage the PIN.

## Privacy

No data is collected or transmitted. All preferences are stored locally via `SharedPreferences`. The PIN is stored as a SHA-256 hash.

## License

[MIT](LICENSE)
