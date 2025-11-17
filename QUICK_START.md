# Quick Start Guide - Native Android App

This guide will help you get the new native Android app up and running.

## Prerequisites

✅ **Required:**
- [Android Studio](https://developer.android.com/studio) Hedgehog (2023.1.1) or later
- JDK 8 or later (bundled with Android Studio)
- Android SDK with API level 34

📱 **Testing:**
- Android device with USB debugging enabled, OR
- Android Emulator (can be set up in Android Studio)

## Step 1: Open Project in Android Studio

1. **Launch Android Studio**

2. **Open the Project**
   - Click **"Open"** on the welcome screen
   - Navigate to: `JournalForge/android-app/`
   - Click **"OK"**

3. **Wait for Gradle Sync**
   - Android Studio will automatically sync Gradle
   - This may take 2-5 minutes on first load
   - You'll see "Gradle sync finished" in the status bar

4. **Install Missing Components** (if prompted)
   - Click "Install" if Android Studio prompts for SDK components
   - Accept licenses if asked

## Step 2: Configure Firebase Google Sign-In

🔧 **This step is required for Google Sign-In to work.**

### Get Web Client ID

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: **journalforge-478513**
3. Click gear icon → **Project Settings**
4. Scroll to **"Your apps"** section
5. Find the **Web app** (or create one if it doesn't exist)
6. Copy the **Web Client ID** (looks like: `XXXXXX-XXXXXXXX.apps.googleusercontent.com`)

### Update the Code

1. In Android Studio, open:
   ```
   app/src/main/java/com/journalforge/app/services/GoogleAuthService.kt
   ```

2. Find line 27:
   ```kotlin
   .requestIdToken("339764184212-YOUR_CLIENT_ID_HERE.apps.googleusercontent.com")
   ```

3. Replace `YOUR_CLIENT_ID_HERE` with your actual Client ID:
   ```kotlin
   .requestIdToken("339764184212-abc123def456.apps.googleusercontent.com")
   ```

4. Save the file (Ctrl+S / Cmd+S)

### Add SHA-1 Fingerprint

1. **Get your SHA-1:**
   - Open Terminal in Android Studio (bottom bar)
   - Run:
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
   - Copy the SHA-1 fingerprint from the output

2. **Add to Firebase:**
   - Go to Firebase Console → Project Settings → Your apps → Android app
   - Click **"Add fingerprint"**
   - Paste your SHA-1
   - Click **"Save"**

### Enable Google Sign-In

1. In Firebase Console, go to **Authentication** → **Sign-in method**
2. Click on **Google** in the providers list
3. Toggle **Enable**
4. Set support email
5. Click **Save**

## Step 3: Build and Run

### Using Android Studio

1. **Select Device**
   - Click the device dropdown in the toolbar
   - Choose your connected device or emulator
   - If no device available, click "Create Device" to set up emulator

2. **Build and Run**
   - Click the green **Run** button (▶️) in the toolbar, OR
   - Press **Shift+F10** (Windows/Linux) or **Control+R** (Mac)

3. **Wait for Build**
   - First build takes 1-2 minutes
   - Progress shown in "Build" panel at bottom
   - App will automatically install and launch

### Using Command Line (Alternative)

```bash
cd android-app

# Build the app
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Both build and install
./gradlew installDebug
```

## Step 4: Test the App

### Basic Functionality

1. **Launch Screen**
   - App opens to dashboard
   - See daily prompt and insight (may say "Loading..." briefly)

2. **Create Entry**
   - Tap **"✍️ Begin Entry"** button
   - Enter title and content
   - Try **"🤖 Ask AI"** button (generates a question)
   - Try **"💡 Suggest Ending"** button (adds AI-generated ending)
   - Tap **"💾 Save Entry"**
   - Should see "Entry saved successfully!" toast

3. **View Entry**
   - Return to main screen (back button)
   - Your entry should appear in "Recent Quest Entries"
   - Tap the entry to open and edit

### Test Google Sign-In

1. **Open Settings**
   - Tap the Settings icon (⚙️) in the top-right toolbar

2. **Sign In**
   - Tap **"Sign In with Google"** button
   - Google account picker should appear
   - Select your account
   - Review permissions
   - Tap **"Allow"**

3. **Verify**
   - You should return to Settings
   - Should see: "Signed in as: your@email.com"
   - **"Sign Out"** button should be visible

4. **Check Firebase**
   - Go to Firebase Console → Authentication → Users
   - Your account should be listed

## Troubleshooting

### Build Errors

**"SDK location not found"**
- Solution: File → Project Structure → SDK Location → Set Android SDK path
- Default: `~/Android/Sdk` (Mac/Linux) or `C:\Users\YourName\AppData\Local\Android\Sdk` (Windows)

**"Could not find google-services.json"**
- Verify the file exists at: `android-app/app/google-services.json`
- If missing, copy from MAUI project: `Platforms/Android/google-services.json`

**Gradle sync failed**
- Try: File → Invalidate Caches → Invalidate and Restart
- Or run: `./gradlew clean build` from terminal

### Sign-In Errors

**"Sign-in failed" (Error 10)**
- Cause: SHA-1 not added or Web Client ID incorrect
- Solution: Double-check SHA-1 in Firebase Console and Client ID in code
- Wait 5-10 minutes after adding SHA-1 for Firebase to update

**"Developer Error"**
- Cause: OAuth client configuration mismatch
- Solution: Verify package name in Firebase matches: `com.journalforge.app`

**Account picker doesn't appear**
- Cause: Google Play Services not available on emulator
- Solution: Use emulator with Play Store, or test on real device

### Runtime Errors

**App crashes on launch**
- Check Logcat in Android Studio (View → Tool Windows → Logcat)
- Look for red error messages
- Common causes: Firebase not initialized, missing dependencies

**Entries don't save**
- Check: Storage permission (should be automatic)
- Look for file I/O errors in Logcat
- Try clearing app data: Settings → Apps → JournalForge → Clear data

## Understanding the Code

### Project Structure
```
app/src/main/
├── java/com/journalforge/app/
│   ├── models/              # Data classes (JournalEntry, etc.)
│   ├── services/            # Business logic
│   │   ├── GoogleAuthService.kt
│   │   ├── JournalEntryService.kt
│   │   └── AIService.kt
│   ├── ui/                  # Activities (screens)
│   │   ├── MainActivity.kt
│   │   ├── JournalEntryActivity.kt
│   │   └── SettingsActivity.kt
│   └── JournalForgeApplication.kt  # App initialization
├── res/
│   ├── layout/              # UI XML files
│   ├── values/              # Strings, colors, themes
│   └── menu/                # Menu resources
└── AndroidManifest.xml      # App configuration
```

### Key Files

- **GoogleAuthService.kt**: Firebase authentication logic
- **MainActivity.kt**: Dashboard screen with entries list
- **JournalEntryActivity.kt**: Create/edit entries
- **SettingsActivity.kt**: Google Sign-In interface
- **build.gradle (app)**: Dependencies and build config

## What's Next?

### Complete Optional Features

1. **History View** (`HistoryActivity.kt`)
   - Show all entries with search
   - Filter and sort options

2. **Time Capsules** (`TimeCapsuleActivity.kt`)
   - Seal messages for future dates
   - Unseal when ready

3. **Cloud Sync**
   - Implement Firestore integration
   - Sync entries across devices

### Customize

1. **Change Colors**: `app/src/main/res/values/colors.xml`
2. **Change Strings**: `app/src/main/res/values/strings.xml`
3. **Modify Layouts**: `app/src/main/res/layout/*.xml`

### Add Features

1. **Speech-to-Text**: Implement voice dictation
2. **Export**: Add share functionality for entries
3. **Tags**: Add tagging system for entries
4. **Search**: Implement full-text search

## Getting Help

- **Migration Guide**: See `MIGRATION_GUIDE.md` for MAUI → Android comparison
- **Firebase Setup**: See `FIREBASE_SIGNIN_COMPLETION.md` for detailed Firebase instructions
- **Implementation Details**: See `IMPLEMENTATION_SUMMARY.md` for code overview
- **Android Docs**: [developer.android.com](https://developer.android.com)
- **Firebase Docs**: [firebase.google.com/docs](https://firebase.google.com/docs)

## Success Checklist

- [ ] Android Studio installed and project opened
- [ ] Gradle sync completed successfully
- [ ] Web Client ID configured in GoogleAuthService.kt
- [ ] SHA-1 fingerprint added to Firebase Console
- [ ] Google Sign-In enabled in Firebase Authentication
- [ ] App builds without errors
- [ ] App launches on device/emulator
- [ ] Can create and save journal entries
- [ ] Google Sign-In works in Settings
- [ ] User account appears in Firebase Console

---

**You're all set!** 🎉 The native Android app should now be running with full functionality. Enjoy much faster builds and native Firebase integration!
