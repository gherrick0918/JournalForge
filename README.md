# JournalForge - AI-Powered Journaling for Android

JournalForge is a native Android application that provides an AI-powered journaling experience with an old-school RPG visual theme. Write, reflect, and explore your thoughts with AI assistance.

## Features

### ✍️ Journal Entries
- Create and save journal entries with titles and content
- Voice dictation support with speech-to-text
- AI-powered probing questions to help you explore your thoughts deeper
- AI suggestions for entry endings
- Conversation history with AI to guide reflection

### 📚 Chronicle History
- Complete history view of all journal entries
- Search functionality to find specific entries
- Sort by date (newest/oldest first)
- View, export, or delete individual entries

### ☁️ Cloud Sync with Google Sign-In
- Sign in with your Google account
- Cloud backup of your journal entries to Firebase
- Sync across multiple devices
- Secure authentication with Google

### ⏰ Time Capsule System
- Seal journal entries to be opened in the future
- Set custom unseal dates
- Automatically track which capsules are ready to be opened

### 🤖 AI Features (OpenAI Integration)
- Daily writing prompts with RPG-themed language
- Context-aware probing questions based on your entry content
- Personalized entry ending suggestions
- Smart insights about journaling patterns

### 🎨 RPG Visual Theme
- Medieval/fantasy color scheme with gold, brown, and parchment tones
- RPG-style UI elements
- Card-based layout reminiscent of old-school RPG menus

## Project Structure

```
android-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/journalforge/app/
│   │   │   ├── models/          # Data classes (JournalEntry, UserProfile, etc.)
│   │   │   ├── services/        # Business logic (GoogleAuthService, AIService, etc.)
│   │   │   └── ui/              # Activities (MainActivity, JournalEntryActivity, etc.)
│   │   ├── res/
│   │   │   ├── layout/          # XML layouts
│   │   │   ├── values/          # Strings, colors, themes
│   │   │   ├── drawable/        # Images and icons
│   │   │   └── menu/            # Navigation menus
│   │   └── AndroidManifest.xml
│   ├── google-services.json     # Firebase configuration
│   └── build.gradle             # App dependencies
├── build.gradle                 # Project configuration
└── settings.gradle              # Module settings
```

## Getting Started

### Prerequisites
- Android Studio (latest version recommended)
- Android SDK 26 or higher
- JDK 17 or higher
- Firebase project with Google Sign-In enabled

### Building the App

1. **Clone the repository**
   ```bash
   git clone https://github.com/gherrick0918/JournalForge.git
   cd JournalForge/android-app
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `android-app` directory
   - Click "OK"

3. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on a device or emulator**
   ```bash
   ./gradlew installDebug
   ```
   Or use Android Studio's "Run" button.

## Setting Up Google Sign-In

Google Sign-In has been completely refactored in V8 with a clean, modern architecture.

### 📚 Documentation

**🆕 [GOOGLE_SIGNIN_V8_QUICK_REFERENCE.md](GOOGLE_SIGNIN_V8_QUICK_REFERENCE.md) - Start Here!**
- Quick reference for the new V8 architecture
- How to use the refactored system
- Best practices and what NOT to do

**📖 [GOOGLE_SIGNIN_REFACTOR_V8.md](GOOGLE_SIGNIN_REFACTOR_V8.md) - Complete Details**
- Full architecture documentation
- Comparison with previous versions (V1-V7)
- Design principles and testing guide

**🎯 [GOOGLE_SIGNIN_CONFIGURATION.md](GOOGLE_SIGNIN_CONFIGURATION.md) - Visual Walkthrough**
- Best for understanding "what goes where"
- Shows file locations and Firebase Console settings
- Includes flow diagrams and examples

**📖 [FIREBASE_SETUP_GUIDE.md](FIREBASE_SETUP_GUIDE.md) - Complete Step-by-Step Guide**
- Detailed instructions for each step
- Verification commands to check your setup
- Comprehensive troubleshooting section

### What's New in V8?

The V8 refactor completely rebuilds Google sign-in with modern Android Architecture Components:

- ✅ **AuthStateManager** - Single source of truth for auth state
- ✅ **ViewModel + LiveData** - Reactive, lifecycle-aware state management
- ✅ **No flags or workarounds** - Clean, simple code
- ✅ **No race conditions** - Proper reactive architecture
- ✅ **170 lines of code removed** - Eliminated all workaround code

**Previous versions (V1-V7)** used SharedPreferences flags, retry loops, and timing delays. These are now deprecated.

### Quick Reference

**What's Already Done:**
✅ Firebase SDK integrated  
✅ `google-services.json` file present  
✅ Web Client ID configured in `strings.xml`  
✅ `GoogleAuthService.kt` implemented  
✅ Sign-in UI in `SettingsActivity`  
✅ Error messages show specific issues

**What YOU Need to Configure:**
1. **Add SHA-1 fingerprint** to Firebase Console (most critical!)
2. **Enable Google Sign-In** in Firebase Authentication
3. **Set project support email** in Firebase

### Getting Your SHA-1 Fingerprint

The most common issue is a missing SHA-1 fingerprint. Get it with:

```bash
# For debug builds (development)
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey -storepass android -keypass android
```

Then add it to: Firebase Console → Project Settings → Your apps → Add fingerprint

**Important:** Wait 5-10 minutes after adding SHA-1 for Firebase to propagate changes.

### Common Error Messages

If you see:
- **"Developer error: Please ensure SHA-1 fingerprint is configured"** → Add SHA-1 to Firebase Console
- **"Configuration error: Please check your Firebase setup"** → Download updated `google-services.json`
- **"Network error"** → Check internet connection

For detailed troubleshooting, see [FIREBASE_SETUP_GUIDE.md](FIREBASE_SETUP_GUIDE.md).

## OpenAI Integration

The app includes AI-powered features using OpenAI's API for intelligent journaling assistance.

### Setting Up OpenAI

1. **Get an API key** from [OpenAI Platform](https://platform.openai.com/api-keys)

2. **Configure the API key**:
   - Navigate to `android-app/` directory
   - Copy `local.properties.example` to `local.properties`:
     ```bash
     cp local.properties.example local.properties
     ```
   - Edit `local.properties` and add your API key:
     ```properties
     openai.api.key=sk-your-actual-api-key-here
     openai.model=gpt-4o-mini
     ```

3. **Build the app** - The API key will be injected into BuildConfig

4. **Verify configuration** - Open Settings in the app to see the AI configuration status

### Important Notes

- **Never commit `local.properties`** - It's already in `.gitignore`
- Without an API key, the app falls back to mock responses
- Default model is `gpt-4o-mini` for cost-effectiveness
- You can change the model to `gpt-4o`, `gpt-4`, or `gpt-3.5-turbo` in `local.properties`

### AI Features

When properly configured with an OpenAI API key, the app provides:
- Daily writing prompts with RPG-themed language
- Context-aware probing questions based on your entry content
- Personalized entry ending suggestions
- Smart insights about journaling patterns
- Conversational AI companion that learns from your entries


## Architecture

- **Kotlin** - Modern, type-safe Android development
- **Firebase Authentication** - Google Sign-In with Firebase Auth
- **Firebase Firestore** - Cloud storage for journal entries (ready for implementation)
- **Coroutines** - Asynchronous programming
- **Material Design 3** - Modern Android UI components
- **Navigation Component** - Fragment-based navigation

## Development Status

**Completed Features:**
- ✅ Journal entry creation and editing
- ✅ Google Sign-In infrastructure (needs configuration)
- ✅ AI integration with OpenAI
- ✅ Local JSON storage
- ✅ History view with search
- ✅ Time capsule system
- ✅ RPG-themed UI

**In Progress:**
- 🚧 Cloud sync with Firestore
- 🚧 Multi-device synchronization
- 🚧 Offline support

**Planned:**
- 📋 Export entries to PDF
- 📋 Mood tracking and analytics
- 📋 Reminder notifications
- 📋 Media attachments (photos, audio)

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Testing

See [TESTING.md](TESTING.md) for information about running tests.

## License

[Your License Here]

## Support

For issues and questions:
- Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
- Open an issue on GitHub
- Contact: [Your Contact Information]

---

**Note**: This app was originally built with .NET MAUI but has been fully migrated to native Android (Kotlin) for better performance and Firebase integration.
