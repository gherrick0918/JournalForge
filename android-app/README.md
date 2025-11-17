# JournalForge - Native Android App

This is the native Android version of JournalForge, migrated from .NET MAUI for improved build times and better integration with Firebase services.

## Features

- 📜 Journal entries with AI-powered assistance
- 🔥 Firebase Google Sign-In authentication
- ⏰ Time capsules (coming soon)
- 📚 Entry history and search (coming soon)
- 🎨 RPG-themed UI

## Requirements

- **Android Studio Hedgehog or later** (Required for building Android apps)
- Android SDK 26+ (minimum)
- Android SDK 34+ (target)  
- JDK 8+
- Gradle 7.0+ (included with Android Studio)

**Note**: This project requires Android Studio to build. The Android Gradle Plugin is not available in standard Gradle installations.

## Firebase Setup

1. This project includes `google-services.json` for Firebase integration
2. To enable Google Sign-In:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select the JournalForge project
   - Go to Authentication > Sign-in method
   - Enable Google as a sign-in provider
   - Get your Web Client ID from the OAuth 2.0 Client IDs
   - Update `GoogleAuthService.kt` with your Web Client ID (replace `YOUR_CLIENT_ID_HERE`)

## Building

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

## Project Structure

```
app/src/main/
├── java/com/journalforge/app/
│   ├── models/          # Data models
│   ├── services/        # Business logic
│   ├── ui/              # Activities
│   └── JournalForgeApplication.kt
├── res/
│   ├── layout/          # UI layouts
│   ├── values/          # Strings, colors, themes
│   └── menu/            # Menu resources
└── AndroidManifest.xml
```

## Key Services

### GoogleAuthService
Handles Firebase authentication with Google Sign-In.

### JournalEntryService  
Manages journal entries with local JSON storage.

### AIService
Provides AI-powered features using OpenAI API (optional).

## Migration Notes

This project was migrated from .NET MAUI to native Android to:
- Reduce build times significantly
- Improve Firebase integration
- Better leverage Android-specific features
- Simplify the development workflow

## Known Issues

- Web Client ID needs to be configured in GoogleAuthService for sign-in to work
- OpenAI API key configuration not yet implemented (uses mock responses)
- History and Time Capsule features are placeholder implementations

## Next Steps

1. Configure Firebase OAuth Web Client ID
2. Test Google Sign-In flow
3. Implement cloud sync with Firestore
4. Complete History and Time Capsule features
5. Add app icons and splash screen
