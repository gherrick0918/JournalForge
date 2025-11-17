# Fixing Google Sign-In: "Stays on Sign-In Screen" Issue

## Problem

When you click "Sign In with Google" in the Settings page and select your Google account, the app just stays on the sign-in screen instead of completing the authentication.

## Root Cause

The Google Sign-In implementation in `GoogleAuthService.kt` is trying to read a string resource called `default_web_client_id`:

```kotlin
// Line 32 in GoogleAuthService.kt
.requestIdToken(context.getString(R.string.default_web_client_id))
```

However, this string resource **is not defined** in `app/src/main/res/values/strings.xml`. Without this Web Client ID, the Google Sign-In flow cannot authenticate with Firebase, causing it to fail silently or stay on the sign-in screen.

## Solution

You need to add your Firebase Web Client ID to the strings.xml file. There are two ways to do this:

### Option 1: Let google-services Plugin Generate It (Recommended)

The `google-services` Gradle plugin should automatically generate the `default_web_client_id` from your `google-services.json` file when you build the project. However, this requires:

1. **A valid `google-services.json` file** with a Web OAuth client configured in Firebase
2. **Internet connection** to download the plugin dependencies
3. **A successful Gradle build**

The auto-generated file will be at:
```
app/build/generated/res/google-services/debug/values/values.xml
```

### Option 2: Add It Manually (Quick Fix)

If the plugin doesn't generate it (due to network restrictions or configuration issues), you can add it manually:

#### Step 1: Get Your Web Client ID from Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **journalforgeapp**
3. Click the **gear icon** → **Project Settings**
4. Scroll to **Your apps** section
5. Look at the Web app configuration (you may need to add a Web app if one doesn't exist)
6. Copy the **Web Client ID** (format: `XXXXX-XXXXXXXX.apps.googleusercontent.com`)

Alternatively, you can extract it from your `google-services.json`:

```bash
cd android-app
cat app/google-services.json | jq '.client[0].oauth_client[] | select(.client_type == 3) | .client_id'
```

From your current `google-services.json`, the Web Client ID is:
```
774563628600-v77alhhdahp5cvuka6krperk85beeg4h.apps.googleusercontent.com
```

#### Step 2: Add to strings.xml

Open `android-app/app/src/main/res/values/strings.xml` and add this line before the closing `</resources>` tag:

```xml
<string name="default_web_client_id">774563628600-v77alhhdahp5cvuka6krperk85beeg4h.apps.googleusercontent.com</string>
```

Complete example:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">JournalForge</string>
    
    <!-- ... other strings ... -->
    
    <!-- Firebase Web Client ID for Google Sign-In -->
    <string name="default_web_client_id">774563628600-v77alhhdahp5cvuka6krperk85beeg4h.apps.googleusercontent.com</string>
</resources>
```

### Additional Required Configuration

Even after adding the Web Client ID, you also need to:

#### 1. Add SHA-1 Fingerprint to Firebase

The SHA-1 fingerprint of your signing key must be registered in Firebase Console:

**For Debug Builds:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey -storepass android -keypass android
```

**For Release Builds:**
```bash
keytool -list -v -keystore /path/to/your-keystore.jks -alias your-alias
```

Copy the SHA-1 fingerprint, then:
1. Go to Firebase Console → Project Settings → Your apps
2. Select your Android app (`com.journalforge.app`)
3. Click **Add fingerprint**
4. Paste the SHA-1 fingerprint
5. Click **Save**

**Important:** Wait 5-10 minutes after adding the SHA-1 for Firebase to propagate the changes.

#### 2. Enable Google Sign-In in Firebase Authentication

1. Go to Firebase Console → **Authentication**
2. Click the **Sign-in method** tab
3. Find **Google** in the providers list
4. Click the **Enable** toggle
5. Set your **Project support email**
6. Click **Save**

## Testing the Fix

1. **Rebuild the app:**
   ```bash
   cd android-app
   ./gradlew clean
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

2. **Test the sign-in flow:**
   - Open the app
   - Go to Settings
   - Tap "Sign In with Google"
   - Select your Google account
   - The app should now successfully sign you in and return to the Settings page
   - You should see "Signed in as: your@email.com"

3. **Verify in Firebase Console:**
   - Go to Firebase Console → Authentication → Users
   - You should see your account listed with provider "google.com"

## Troubleshooting

### Error: "Sign-in failed" or Developer Error (Error 10)

**Cause:** SHA-1 fingerprint not registered or package name mismatch

**Solution:**
- Double-check SHA-1 is added to Firebase
- Verify package name is `com.journalforge.app`
- Wait 5-10 minutes after adding SHA-1
- Clear app data and try again

### Error: Still stays on sign-in screen

**Cause:** Web Client ID is incorrect or not set

**Solution:**
- Verify the Web Client ID in strings.xml matches the one from Firebase Console
- Ensure you're using the **Web Client ID** (type 3), not the Android Client ID (type 1)
- Check Android Studio Logcat for error messages:
  ```bash
  adb logcat | grep -E "GoogleAuthService|FirebaseAuth"
  ```

### Error: "IdpResponse is null"

**Cause:** User cancelled the sign-in flow

**Solution:** This is normal behavior when the user cancels. Not an error.

## Summary

To fix the "stays on sign-in screen" issue:

1. ✅ Add `default_web_client_id` to `strings.xml` with your Firebase Web Client ID
2. ✅ Register your SHA-1 fingerprint in Firebase Console
3. ✅ Enable Google Sign-In in Firebase Authentication
4. ✅ Rebuild and reinstall the app
5. ✅ Test the sign-in flow

After completing these steps, Google Sign-In should work correctly!

## Reference Links

- [Firebase Console](https://console.firebase.google.com/)
- [Firebase Authentication Setup](https://firebase.google.com/docs/auth/android/google-signin)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android/start-integrating)
