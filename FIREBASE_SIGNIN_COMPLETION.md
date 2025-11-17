# Completing Firebase Google Sign-In - Native Android

This guide walks through the final steps to complete Firebase Google Sign-In authentication in the native Android app.

## Current Implementation Status

### ✅ What's Already Implemented

1. **Firebase SDK Integration**
   - Firebase BOM (Bill of Materials) in `build.gradle`
   - Firebase Auth and Google Play Services Auth dependencies
   - Google Services plugin for `google-services.json` processing

2. **GoogleAuthService**
   - Located: `app/src/main/java/com/journalforge/app/services/GoogleAuthService.kt`
   - Implements complete sign-in/sign-out flow
   - Uses Firebase Authentication with Google credentials
   - Handles auth state changes

3. **SettingsActivity**
   - Located: `app/src/main/java/com/journalforge/app/ui/SettingsActivity.kt`
   - UI for Google Sign-In button
   - Displays signed-in user info
   - Sign-out functionality
   - Uses Activity Result API for OAuth flow

4. **Firebase Configuration**
   - `google-services.json` is present in `app/` directory
   - Project ID: `journalforge-478513`
   - Package name: `com.journalforge.app`

## ⚙️ Configuration Required

### Step 1: Get OAuth 2.0 Web Client ID

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select project: **journalforge-478513**
3. Navigate to: **APIs & Services** → **Credentials**
4. You should see OAuth 2.0 Client IDs listed
5. Find the **Web client (auto created by Google Service)**
6. Copy the Client ID (format: `XXXXXX-XXXXXXXX.apps.googleusercontent.com`)

**Alternative: Get from Firebase Console**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select **journalforge-478513** project
3. Go to **Project Settings** (gear icon)
4. Scroll to **Your apps** section
5. Click on the **Web app** (if exists) or create one
6. Copy the Web Client ID from the Firebase configuration

### Step 2: Update GoogleAuthService

Open `android-app/app/src/main/java/com/journalforge/app/services/GoogleAuthService.kt`

Find line 27:
```kotlin
.requestIdToken("339764184212-YOUR_CLIENT_ID_HERE.apps.googleusercontent.com")
```

Replace with your actual Web Client ID:
```kotlin
.requestIdToken("339764184212-abcdefgh12345678.apps.googleusercontent.com")
```

### Step 3: Add SHA-1 Fingerprint

**For Debug Builds:**

1. Generate SHA-1 for debug keystore:
```bash
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey -storepass android -keypass android
```

2. Copy the SHA-1 fingerprint from output
3. Go to Firebase Console → Project Settings → Your apps → Android app
4. Click **Add fingerprint** button
5. Paste the SHA-1 fingerprint
6. Click **Save**

**For Release Builds:**

1. Generate SHA-1 for your release keystore:
```bash
keytool -list -v -keystore /path/to/your-release-key.keystore -alias your-alias
```

2. Add this SHA-1 to Firebase Console as well

### Step 4: Enable Google Sign-In in Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select **journalforge-478513** project
3. Navigate to **Authentication** → **Sign-in method**
4. Find **Google** in the providers list
5. Click **Enable** toggle
6. Set **Project public-facing name**: "JournalForge"
7. Set **Project support email**: your email
8. Click **Save**

### Step 5: Verify Firebase Project Configuration

Ensure `google-services.json` is correct:

1. Check the file is located at: `android-app/app/google-services.json`
2. Verify it contains:
   ```json
   {
     "project_info": {
       "project_id": "journalforge-478513",
       "project_number": "339764184212"
     }
   }
   ```

## 🧪 Testing the Implementation

### Build and Install

```bash
cd android-app
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
```

### Test Flow

1. **Launch the app**
   - App should open to the main dashboard

2. **Open Settings**
   - Tap the Settings icon (⚙️) in the toolbar
   - You should see "Sign In with Google" button

3. **Start Sign-In**
   - Tap "Sign In with Google"
   - Google account picker should appear (or browser for OAuth)

4. **Complete OAuth**
   - Select your Google account
   - Review permissions
   - Tap "Allow" or "Continue"

5. **Verify Success**
   - App should return to Settings
   - You should see "Signed in as: your@email.com"
   - Sign-out button should be visible

### Verify in Firebase Console

1. Go to Firebase Console → Authentication → Users
2. You should see your account listed
3. Provider should show "google.com"

## 🐛 Troubleshooting

### "Sign-in failed" Error

**Cause**: Web Client ID not configured or incorrect

**Solution**:
1. Double-check the Client ID in `GoogleAuthService.kt`
2. Ensure you're using the **Web Client ID**, not Android Client ID
3. The Client ID should NOT have `.apps.googleusercontent.com` twice

### Error 10: "Developer Error"

**Cause**: SHA-1 fingerprint not added to Firebase or incorrect package name

**Solutions**:
1. Verify SHA-1 is added: Firebase Console → Project Settings → Your apps → SHA-1
2. Verify package name matches: `com.journalforge.app`
3. Wait 5-10 minutes after adding SHA-1 (Firebase needs time to update)
4. Clear app data and try again

### "IdpResponse is null"

**Cause**: User cancelled the sign-in flow

**Solution**: This is normal user behavior, not an error. The app handles this gracefully.

### "API not enabled"

**Cause**: Required Google APIs not enabled in Cloud Console

**Solution**:
1. Go to Google Cloud Console → APIs & Services → Library
2. Search for "Google Sign-In API" or "Identity Toolkit API"
3. Click Enable if not already enabled

### Build Error: "Could not parse google-services.json"

**Solutions**:
1. Verify JSON file is valid (check with JSON validator)
2. Ensure it's in `app/` directory, not `app/src/main/`
3. Clean and rebuild: `./gradlew clean build`

### Sign-In Succeeds but getCurrentUser() Returns Null

**Cause**: Auth state not updated yet

**Solution**: This shouldn't happen with current implementation, but if it does:
1. Check that `onAuthStateChanged` callback is triggered
2. Add delay before calling `getCurrentUser()`
3. Verify Firebase Auth initialization in Application class

## 📱 Production Checklist

Before releasing to production:

- [ ] Update Web Client ID in `GoogleAuthService.kt`
- [ ] Add release keystore SHA-1 to Firebase
- [ ] Configure signing config in `app/build.gradle`
- [ ] Test sign-in on multiple devices
- [ ] Test sign-out functionality
- [ ] Handle edge cases (no internet, account conflicts)
- [ ] Add analytics for sign-in events
- [ ] Test app behavior when sign-in is cancelled

## 🔐 Security Notes

1. **Web Client ID**: Not a secret, safe to include in code
2. **google-services.json**: Contains public project info, safe to commit
3. **Keystore**: Never commit keystores or passwords
4. **User Data**: Ensure user journal entries are private and secure

## 📚 Additional Resources

- [Firebase Auth Android Guide](https://firebase.google.com/docs/auth/android/google-signin)
- [Google Sign-In Integration](https://developers.google.com/identity/sign-in/android/start-integrating)
- [Troubleshooting Google Sign-In](https://developers.google.com/identity/sign-in/android/troubleshooting)

## 🎯 Next Steps After Sign-In Works

1. **Implement Cloud Sync**
   - Use Firestore to sync journal entries
   - Store entries in `/users/{userId}/entries/`
   - Implement sync service

2. **Add Profile Management**
   - Display user profile picture
   - Allow account switching
   - Add sign-out confirmation dialog

3. **Enhance Error Handling**
   - Show user-friendly error messages
   - Retry logic for network errors
   - Offline mode support

## ✅ Verification Checklist

After configuration:

- [ ] Web Client ID added to GoogleAuthService.kt
- [ ] SHA-1 fingerprint added to Firebase Console
- [ ] Google Sign-In enabled in Firebase Authentication
- [ ] App builds successfully
- [ ] Sign-in flow works on test device
- [ ] User info displays correctly after sign-in
- [ ] Sign-out works correctly
- [ ] User appears in Firebase Console → Authentication → Users

---

**Need Help?** If sign-in still doesn't work after following all steps:
1. Check Android Studio Logcat for error messages
2. Look for "GoogleAuthService" or "FirebaseAuth" tags
3. Share the error logs for debugging
