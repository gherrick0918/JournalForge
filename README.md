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

Google Sign-In is implemented in the app, but requires configuration to work properly. When you click "Sign In with Google" and select an account, it stays on the sign-in screen because the Firebase configuration is incomplete.

### What's Already Configured

✅ Firebase SDK integrated  
✅ `google-services.json` file present  
✅ `GoogleAuthService.kt` implemented  
✅ Sign-in UI in `SettingsActivity`  

### What You Need to Configure

The app is currently missing the **Web Client ID** configuration. Here's how to fix it:

#### Step 1: Get Your Web Client ID

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **journalforgeapp**
3. Go to **Project Settings** (gear icon)
4. Scroll to **Your apps** section
5. Find the Web app or create one if it doesn't exist
6. Copy the **Web Client ID** (format: `XXXXX-XXXXXXXX.apps.googleusercontent.com`)

#### Step 2: Add Web Client ID to strings.xml

The Google Sign-In plugin expects a string resource called `default_web_client_id`. When you build the project, the `google-services` gradle plugin should automatically generate this from your `google-services.json` file. However, if it's not being generated, you can add it manually:

Open `android-app/app/src/main/res/values/strings.xml` and add:

```xml
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE</string>
```

Replace `YOUR_WEB_CLIENT_ID_HERE` with your actual Web Client ID from Step 1.

#### Step 3: Add SHA-1 Fingerprint to Firebase

For Google Sign-In to work, you need to register your app's SHA-1 fingerprint:

**For Debug Builds:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey -storepass android -keypass android
```

**For Release Builds:**
```bash
keytool -list -v -keystore /path/to/your-keystore.jks -alias your-alias
```

Copy the SHA-1 fingerprint from the output, then:

1. Go to Firebase Console → Project Settings → Your apps
2. Select your Android app
3. Click **Add fingerprint**
4. Paste the SHA-1 fingerprint
5. Click **Save**

#### Step 4: Enable Google Sign-In in Firebase

1. Go to Firebase Console → **Authentication**
2. Click **Sign-in method** tab
3. Find **Google** in the providers list
4. Click **Enable**
5. Set **Project support email**
6. Click **Save**

#### Step 5: Rebuild and Test

1. Clean and rebuild the project:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

2. Open the app and navigate to Settings
3. Tap "Sign In with Google"
4. Select your Google account
5. You should now be signed in!

### Troubleshooting Google Sign-In

**Problem: "Sign-in failed" or stays on sign-in screen**
- Verify Web Client ID is correctly added to `strings.xml` or generated by google-services plugin
- Check SHA-1 fingerprint is added to Firebase Console
- Ensure Google Sign-In is enabled in Firebase Authentication

**Problem: "Developer Error" (Error 10)**
- SHA-1 fingerprint not registered or incorrect
- Package name mismatch (ensure it's `com.journalforge.app`)
- Wait 5-10 minutes after adding SHA-1 for Firebase to update

**Problem: Build fails with "Could not resolve dependencies"**
- Check your internet connection
- Try running `./gradlew --refresh-dependencies`

For more troubleshooting help, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md).

## OpenAI Integration

The app includes AI-powered features using OpenAI's API. To configure:

1. Get an API key from [OpenAI](https://platform.openai.com/)
2. Add your API key to the `AIService.kt` configuration
3. The app will use GPT-4o-mini for cost-effectiveness

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
