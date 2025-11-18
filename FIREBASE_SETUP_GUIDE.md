# Firebase Setup Guide for Google Sign-In

This guide walks you through **exactly** what needs to be configured to get Google Sign-In working in JournalForge.

## Already Configured? Start Here! ✅

**If you've already completed the Firebase setup steps and the verification script passes:**

1. **Run the verification script:**
   ```bash
   cd android-app
   ./verify-firebase-setup.sh
   ```

2. **If you see all checks passed and:**
   - Your SHA-1 was already in Firebase Console (no changes needed)
   - Google Sign-In provider was already enabled
   - `google-services.json` showed no changes when you downloaded it

   **→ You're all set! Just build and test the app:**
   ```bash
   ./gradlew clean assembleDebug installDebug
   ```

3. **Then test Google Sign-In in the app:**
   - Open the app
   - Go to Settings (⚙️)
   - Tap "Sign In with Google"
   - ✅ Should work!

**If it still doesn't work after building and testing, see the [Troubleshooting](#troubleshooting) section below.**

---

## What You're Seeing

If you see the error:
> "Developer error: Please ensure SHA-1 fingerprint is configured in Firebase Console..."

This means the app is correctly configured on your device, but Firebase doesn't recognize your app's signing certificate. This is the **most common issue** with Google Sign-In on Android.

## Prerequisites

✅ **Already Configured (You don't need to do these)**
- Firebase project created (`journalforgeapp`)
- `google-services.json` file present in the app
- Firebase SDK integrated in the app
- Web Client ID configured in `strings.xml`
- Google Sign-In code implemented
- Error messaging system in place

❗ **What YOU Need to Configure**
- SHA-1 fingerprint(s) in Firebase Console
- Google Sign-In provider enabled in Firebase Authentication
- Proper Firebase app configuration

---

## Step-by-Step Setup

### Step 1: Get Your SHA-1 Fingerprint

The SHA-1 fingerprint is a unique identifier for your app's signing certificate. Google uses this to verify that sign-in requests are coming from your actual app.

#### For Debug Builds (Development & Testing)

Run this command in your terminal:

```bash
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey -storepass android -keypass android
```

**On Windows:**
```cmd
keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore ^
  -alias androiddebugkey -storepass android -keypass android
```

**What to look for in the output:**
```
Certificate fingerprints:
    SHA1: EE:2E:34:33:6D:EB:B4:F4:2F:43:8F:3C:51:B5:C5:49:32:7F:AF:03
    SHA-256: ...
```

✏️ **Copy the SHA-1 value** (the part after "SHA1:")

#### For Release Builds (Production)

If you have a release keystore file:

```bash
keytool -list -v -keystore /path/to/your-release.keystore -alias your-key-alias
```

You'll be prompted for the keystore password. Then copy the SHA-1 from the output.

#### If You Don't Have ~/.android/debug.keystore

This means you haven't built the app yet. Build it first:

```bash
cd android-app
./gradlew assembleDebug
```

This will automatically create the debug keystore, then run the keytool command above.

### Step 2: Add SHA-1 to Firebase Console

Now we'll register your SHA-1 fingerprint with Firebase:

1. **Go to Firebase Console**
   - Visit: https://console.firebase.google.com/
   - Select your project: **journalforgeapp**

2. **Navigate to Project Settings**
   - Click the ⚙️ gear icon next to "Project Overview"
   - Select **Project settings**

3. **Find Your Android App**
   - Scroll down to "Your apps" section
   - You should see an Android app with package name: `com.journalforge.app`

4. **Add Your SHA-1 Fingerprint**
   - Under the Android app, find the "SHA certificate fingerprints" section
   - Click **"Add fingerprint"**
   - Paste your SHA-1 fingerprint (e.g., `EE:2E:34:33:6D:EB:B4:F4:2F:43:8F:3C:51:B5:C5:49:32:7F:AF:03`)
   - Click **Save**
   
   **Note:** If your SHA-1 is already listed, skip this step and move to Step 3.

5. **Download Updated google-services.json (Only if you added a NEW fingerprint)**
   - **If you just added a NEW SHA-1:** Click **"Download google-services.json"** and replace the existing file at `android-app/app/google-services.json`
   - **If your SHA-1 was already there:** You do NOT need to download a new file. The existing `google-services.json` is fine!

**Important Notes:**
- ⏰ Firebase takes **5-10 minutes** to propagate changes. Wait before testing!
- 📱 You need different SHA-1s for debug and release builds
- 💻 Each developer needs to add their own debug keystore SHA-1
- 🤖 CI/CD systems need their own SHA-1s too

### Step 3: Enable Google Sign-In in Firebase

1. **Go to Firebase Authentication**
   - In Firebase Console, click **Authentication** in the left sidebar
   - Click the **Sign-in method** tab at the top

2. **Enable Google Provider**
   - Find **Google** in the list of providers
   - If it shows "Disabled", click on it
   - Toggle the **Enable** switch to ON
   - Set your **Project support email** (required)
   - Click **Save**

3. **Verify Web Client ID**
   - Still in the Google provider settings
   - You should see a "Web SDK configuration" section
   - Note the **Web client ID** (format: `XXXXX-XXXXXXXX.apps.googleusercontent.com`)
   - This should match what's in your `strings.xml` file

### Step 4: Verify Your Configuration

Let's make sure everything is in the right place:

#### Option 1: Use the Verification Script (Recommended)

We've included a handy script that checks your setup automatically:

```bash
cd android-app
./verify-firebase-setup.sh
```

This script will:
- ✅ Check if `google-services.json` exists and has correct package name
- ✅ Verify Web Client ID is in `strings.xml`
- ✅ Find your debug keystore and extract SHA-1 fingerprint
- ✅ Verify all dependencies are configured
- 🎯 Show you exactly what needs to be fixed (if anything)

#### Option 2: Manual Verification

If you prefer to check manually:

##### Check 1: google-services.json Location
```bash
cd android-app
ls -la app/google-services.json
```
✅ Should show the file exists in `android-app/app/` directory

##### Check 2: Web Client ID in strings.xml
```bash
cat app/src/main/res/values/strings.xml | grep default_web_client_id
```
✅ Should show:
```xml
<string name="default_web_client_id">774563628600-v77alhhdahp5cvuka6krperk85beeg4h.apps.googleusercontent.com</string>
```

##### Check 3: Package Name Matches
- Firebase Console shows: `com.journalforge.app`
- In `android-app/app/build.gradle`:
  ```gradle
  applicationId "com.journalforge.app"
  ```
✅ These must match exactly!

### Step 5: Rebuild and Test

1. **Clean and rebuild the app:**
   ```bash
   cd android-app
   ./gradlew clean
   ./gradlew assembleDebug
   ```

2. **Install on your device:**
   ```bash
   ./gradlew installDebug
   ```

3. **Wait 5-10 minutes** after adding SHA-1 (Firebase propagation time)

4. **Test Google Sign-In:**
   - Open the app
   - Go to Settings (⚙️)
   - Tap "Sign In with Google"
   - Select your Google account
   - ✅ Should sign in successfully!

---

## Troubleshooting

### Setup Verification Passes But You're Uncertain What to Do Next

**If you ran `./verify-firebase-setup.sh` and all checks passed:**

This means your **local configuration is correct**. Now you need to ensure Firebase Console is also configured:

1. **Check Firebase Console Settings:**
   - Go to [Firebase Console](https://console.firebase.google.com/project/journalforgeapp)
   - Navigate to Project Settings → Your apps → Android app
   - Verify your SHA-1 fingerprint is listed
   - Go to Authentication → Sign-in method
   - Verify Google provider is **Enabled**

2. **If Everything Was Already Configured:**
   - SHA-1 was already in Firebase? ✅ Good!
   - Google Sign-In already enabled? ✅ Good!
   - `google-services.json` showed no changes? ✅ Good!
   
   **→ This means setup is complete. Just build and test:**
   ```bash
   cd android-app
   ./gradlew clean assembleDebug installDebug
   ```

3. **If You Just Made Changes in Firebase Console:**
   - Download the updated `google-services.json`
   - Replace the old file at `android-app/app/google-services.json`
   - Wait 5-10 minutes for Firebase to propagate changes
   - Then rebuild and test

**The key point:** If your SHA-1 was already in Firebase and Google Sign-In was already enabled, you don't need to download a new `google-services.json` or make any other changes. Just build the app and test it!

### Still Getting "Developer error: Please ensure SHA-1 fingerprint is configured"

**Checklist:**
- [ ] Did you add the correct SHA-1? (Run keytool command again to verify)
- [ ] Did you add it to the correct Firebase project? (`journalforgeapp`)
- [ ] Did you add it to the correct Android app? (`com.journalforge.app`)
- [ ] Did you wait 5-10 minutes after adding it?
- [ ] Did you download the updated `google-services.json` and replace the old one?
- [ ] Did you rebuild the app after updating `google-services.json`?

**Still not working?**

Try these:

1. **Verify SHA-1 in Firebase Console:**
   - Go back to Firebase Console → Project Settings → Your apps
   - Check if your SHA-1 is actually listed there
   - It should look like: `EE:2E:34:33:6D:EB:B4:F4:...`

2. **Make sure you're using the debug keystore:**
   ```bash
   # Check if debug keystore exists
   ls -la ~/.android/debug.keystore
   
   # Verify your app is using it
   cd android-app
   ./gradlew signingReport
   ```
   Look for the SHA-1 under "Variant: debug" - this should match what you added to Firebase.

3. **Clear app data and try again:**
   - Settings → Apps → JournalForge → Storage → Clear Data
   - Reinstall the app
   - Try signing in again

### Error: "Configuration error: Please check your Firebase setup"

This means there's an issue with your `google-services.json` file.

**Solutions:**
- Download the latest `google-services.json` from Firebase Console
- Make sure it's in the correct location: `android-app/app/google-services.json`
- Verify the package name in the file matches your app: `com.journalforge.app`
- Rebuild the app

### Error: "Network error: Please check your internet connection"

**Solutions:**
- Check your internet connection
- Try switching between WiFi and mobile data
- Check if your firewall is blocking Google services
- Try on a different network

### Sign-In Works, But Not Syncing

If you can sign in but data isn't syncing:
- Cloud sync features are still in development
- Sign-in is working correctly!
- Sync functionality will be added in a future update

---

## Multiple Developers / CI/CD

### For Each Developer

Each developer must:
1. Generate their own SHA-1 from their debug keystore
2. Add it to Firebase Console (you can have multiple SHA-1s)
3. **Important:** When adding additional SHA-1s, you do NOT need to download a new `google-services.json`. The file doesn't contain the SHA-1 fingerprints - those are stored in Firebase Console only.
4. Each developer can add their own without affecting others

### For CI/CD (GitHub Actions, etc.)

1. Your CI system will have its own keystore
2. Generate SHA-1 from the CI keystore
3. Add it to Firebase Console
4. Configure CI to use the same `google-services.json` that's already in the repo

---

## What's Already Done (Reference)

For your information, here's what's already configured in the codebase:

### ✅ Code Configuration

1. **GoogleAuthService.kt** - Fully implemented with:
   - Google Sign-In client setup
   - Firebase authentication
   - Error handling with specific error messages
   - Sign-out functionality

2. **LoginActivity.kt** - Handles sign-in flow:
   - Launch Google account picker
   - Process sign-in result
   - Display specific error messages to user
   - Navigate to MainActivity on success

3. **SettingsActivity.kt** - Settings UI:
   - Sign-in button
   - Sign-out functionality
   - Display current user info

4. **strings.xml** - Contains:
   - Web Client ID: `default_web_client_id`
   - All user-facing text and error messages

5. **build.gradle** - Dependencies:
   - Firebase Auth SDK
   - Google Play Services Auth
   - All required libraries

6. **google-services.json** - Firebase configuration:
   - Project ID: `journalforgeapp`
   - Package name: `com.journalforge.app`
   - API keys
   - OAuth client configurations

### ✅ What Works

- Firebase SDK is integrated
- Google Sign-In flow is implemented
- Error messages are specific and helpful
- Sign-in UI is ready
- Code is production-ready

### ❗ What YOU Configure

- **SHA-1 fingerprint(s)** in Firebase Console
- **Enable Google Sign-In** in Firebase Authentication
- **Download updated google-services.json** after changes

---

## Quick Reference

### Commands You'll Use

```bash
# Get debug SHA-1
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Build the app
cd android-app
./gradlew clean assembleDebug

# Install on device
./gradlew installDebug

# View signing report (shows all SHA-1s)
./gradlew signingReport
```

### Files You'll Modify

- `android-app/app/google-services.json` - **Only** needs to be replaced if you made changes to the Firebase project configuration (not when adding SHA-1 fingerprints)

### URLs You'll Need

- Firebase Console: https://console.firebase.google.com/
- Your project: https://console.firebase.google.com/project/journalforgeapp
- Firebase Auth Docs: https://firebase.google.com/docs/auth/android/google-signin

---

## Frequently Asked Questions

### Do I need to download a new google-services.json when I add an SHA-1?

**Short answer: Usually NO.**

The `google-services.json` file contains your Firebase project configuration (project ID, API keys, client IDs), but it does **NOT** contain the SHA-1 fingerprints. SHA-1 fingerprints are stored separately in Firebase's servers.

**You only need to download a new `google-services.json` if:**
- You're setting up the Firebase project for the first time
- You changed the package name
- You added/removed Firebase services (like Analytics, Storage, etc.)
- Firebase Console tells you to download it

**You do NOT need to download it when:**
- Adding additional SHA-1 fingerprints (most common case)
- Adding developers to the project
- Enabling/disabling authentication providers

### My SHA-1 was already in Firebase. Do I need to do anything?

**No!** If your SHA-1 fingerprint was already added to Firebase Console and Google Sign-In is already enabled, you're all set. Just build and test the app:

```bash
cd android-app
./gradlew clean assembleDebug installDebug
```

### The verify script passed. What should I do next?

If the verification script shows all checks passed, just build and test the app. The script verifies your local configuration is correct. As long as you've also checked that:
1. Your SHA-1 is in Firebase Console
2. Google Sign-In is enabled in Firebase

Then you're ready to test!

---

## Summary Checklist

Complete setup checklist:

- [ ] **Step 1**: Get SHA-1 fingerprint using keytool command
- [ ] **Step 2**: Check if SHA-1 is already in Firebase Console → Project Settings → Your apps
  - If yes: Skip to Step 5
  - If no: Add it now
- [ ] **Step 3**: Download updated `google-services.json` from Firebase (only if you added a NEW SHA-1)
- [ ] **Step 4**: Replace old `google-services.json` in `android-app/app/` (only if you downloaded a new one)
- [ ] **Step 5**: Enable Google Sign-In in Firebase Console → Authentication
  - If already enabled: ✅ Great! Move to next step
- [ ] **Step 6**: Set project support email in Google Sign-In settings
  - If already set: ✅ Great! Move to next step
- [ ] **Step 7**: Rebuild app: `./gradlew clean assembleDebug`
- [ ] **Step 8**: Install app: `./gradlew installDebug`
- [ ] **Step 9**: Wait 5-10 minutes for Firebase to propagate changes (only if you made changes)
  - If nothing changed: No need to wait!
- [ ] **Step 10**: Test sign-in in the app!

---

## Need Help?

If you're stuck:

1. **Check the error message** - The app now shows specific error codes
2. **Review this guide** - Make sure you completed all steps
3. **Check existing issues** - See if others had the same problem
4. **Ask for help** - Open a GitHub issue with:
   - The exact error message you're seeing
   - Output of `./gradlew signingReport`
   - Screenshot of your Firebase Console settings
   - Steps you've already tried

---

**Remember:** The SHA-1 fingerprint step is the most critical and most commonly missed step. Make sure you've added it to Firebase and waited 5-10 minutes!
