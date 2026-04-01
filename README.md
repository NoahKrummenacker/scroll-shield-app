# ScrollShield

Android app that automatically blocks Instagram Reels and YouTube Shorts using the Accessibility API.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%208%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Flutter](https://img.shields.io/badge/UI-Flutter-blue.svg)](https://flutter.dev)

---

## Features

- Block Instagram Reels when the Reels tab is selected
- Block Reels opened from the main feed or Explore
- Block YouTube Shorts (tab and player)
- Block Shorts launched from the YouTube home screen
- Schedule-based blocking — active only during a defined time window
- PIN protection to prevent disabling the blocker
- 4 themes: Bleu & Jaune, Gris foncé, Gris (default), Clair

## Requirements

- Android 8.0+ (API 26)
- Accessibility permission enabled manually in Android Settings
- Flutter 3.x (for the Flutter UI build)

## Project structure

```
scroll-shield-android/
├── app/                            # Kotlin app (original)
│   └── src/main/
│       ├── java/com/example/contentblocker/
│       │   ├── BlockerAccessibilityService.kt
│       │   ├── MainActivity.kt
│       │   └── data/PrefsManager.kt
│       └── res/
└── flutter_ui/                     # Flutter app (current)
    ├── lib/
    │   ├── main.dart
    │   ├── screens/
    │   │   ├── home_screen.dart     # Toggles + service status
    │   │   └── settings_screen.dart # Theme, schedule, PIN
    │   ├── services/
    │   │   └── blocker_channel.dart # MethodChannel bridge
    │   └── theme/
    │       ├── app_colors.dart      # 4 themes
    │       └── theme_provider.dart
    └── android/
        └── app/src/main/kotlin/com/example/scroll_shield/
            ├── BlockerAccessibilityService.kt
            ├── MainActivity.kt      # MethodChannel handler
            └── PrefsManager.kt     # SharedPreferences + PIN (SHA-256)
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

### Kotlin app (original)

Requires Android SDK (compileSdk 34) and a connected device with ADB enabled.

```bash
./gradlew installDebug
```

### Docker (Kotlin app only, no dependencies required)

```bash
./build-apk.sh
```

Outputs `ScrollShield.apk` at the project root.

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
