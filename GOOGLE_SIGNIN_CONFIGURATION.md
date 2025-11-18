# Google Sign-In Configuration Walkthrough

## Where Things Need to Be

This is a quick visual reference showing **exactly** what needs to be where for Google Sign-In to work.

---

## 🗂️ Files on Your Computer

### `android-app/app/google-services.json`
**Status:** ✅ Already present  
**What it contains:**
- Project ID: `journalforgeapp`
- Package name: `com.journalforge.app`
- Web Client ID: `774563628600-v77alhhdahp5cvuka6krperk85beeg4h.apps.googleusercontent.com`
- API keys

**What you need to do:**
- ❗ **Download the UPDATED version after adding SHA-1 to Firebase Console**
- Replace the existing file with the new one from Firebase
- Location: Place it at `android-app/app/google-services.json`

---

### `android-app/app/src/main/res/values/strings.xml`
**Status:** ✅ Already configured  
**What it contains:**
```xml
<string name="default_web_client_id">774563628600-v77alhhdahp5cvuka6krperk85beeg4h.apps.googleusercontent.com</string>
```

**What you need to do:**
- ✅ Nothing! This is already set up correctly.

---

### `~/.android/debug.keystore`
**Status:** ⚠️ Created automatically on first build  
**Location:** 
- macOS/Linux: `~/.android/debug.keystore`
- Windows: `%USERPROFILE%\.android\debug.keystore`

**What you need to do:**
1. Build the app if you haven't: `./gradlew assembleDebug`
2. Extract SHA-1 from this keystore:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore \
     -alias androiddebugkey -storepass android -keypass android
   ```
3. Copy the SHA-1 fingerprint (looks like: `EE:2E:34:33:6D:EB:B4:F4:...`)
4. Add it to Firebase Console (see below)

---

## 🔥 Firebase Console Configuration

Visit: https://console.firebase.google.com/project/journalforgeapp

### 1. Project Settings (⚙️ gear icon)

**Navigate to:** Project Overview → ⚙️ → Your apps → Android app (`com.journalforge.app`)

**What you see:**
- Package name: `com.journalforge.app`
- SHA certificate fingerprints section

**What you need to do:**
1. ❗ **Click "Add fingerprint"**
2. ❗ **Paste your SHA-1 from the keytool command**
3. ❗ **Click "Save"**
4. ❗ **Download the updated `google-services.json`**
5. ⏰ **Wait 5-10 minutes for Firebase to propagate changes**

**Example:**
```
SHA certificate fingerprints
✓ EE:2E:34:33:6D:EB:B4:F4:2F:43:8F:3C:51:B5:C5:49:32:7F:AF:03
✓ DD:CA:07:03:A8:5B:81:81:66:A1:E9:5E:7C:74:89:AD:45:D8:2D:23
+ Add fingerprint  <-- Click here and paste YOUR SHA-1
```

---

### 2. Authentication

**Navigate to:** Build → Authentication → Sign-in method

**What you see:**
- List of sign-in providers
- Google provider row

**What you need to do:**
1. ❗ **Click on "Google" provider**
2. ❗ **Toggle "Enable" to ON**
3. ❗ **Set "Project support email"** (select your email from dropdown)
4. ❗ **Verify Web client ID is shown** (should be: `774563628600-v77alhhdahp5cvuka6krperk85beeg4h.apps.googleusercontent.com`)
5. ❗ **Click "Save"**

**Example:**
```
Sign-in providers
─────────────────────────────────────────────
Google                    [Enabled ✓]  <-- Should be enabled
Email/Password            [Disabled]
Phone                     [Disabled]
...
```

---

## 📋 Configuration Checklist

Use this to verify everything is in place:

### On Your Computer
- [ ] `google-services.json` exists at `android-app/app/google-services.json`
- [ ] `strings.xml` has `default_web_client_id` (already done)
- [ ] Debug keystore exists at `~/.android/debug.keystore` (created on first build)
- [ ] You've extracted SHA-1 from your debug keystore

### In Firebase Console
- [ ] You're in the correct project: `journalforgeapp`
- [ ] Android app is registered with package name: `com.journalforge.app`
- [ ] Your SHA-1 fingerprint is added to the Android app
- [ ] Google Sign-In provider is ENABLED
- [ ] Project support email is set
- [ ] You've downloaded the updated `google-services.json` after adding SHA-1
- [ ] You've waited 5-10 minutes after making Firebase changes

### In Your App
- [ ] Old `google-services.json` replaced with updated version
- [ ] App rebuilt: `./gradlew clean assembleDebug`
- [ ] App installed: `./gradlew installDebug`

---

## 🔄 The Configuration Flow

Here's how everything connects:

```
1. You build the app
   └─> Creates ~/.android/debug.keystore

2. You extract SHA-1 from keystore
   └─> Using keytool command

3. You add SHA-1 to Firebase Console
   └─> Project Settings → Your apps → Add fingerprint

4. Firebase updates configuration
   └─> Takes 5-10 minutes to propagate

5. You download updated google-services.json
   └─> Replace old file in android-app/app/

6. You rebuild the app
   └─> ./gradlew clean assembleDebug

7. You enable Google Sign-In in Firebase
   └─> Authentication → Sign-in method → Google → Enable

8. App can now authenticate!
   └─> User signs in → Google verifies SHA-1 → Success!
```

---

## 🎯 Quick Verification

Run this command to check your setup:
```bash
cd android-app
./verify-firebase-setup.sh
```

This will automatically verify:
- ✅ `google-services.json` location and content
- ✅ `strings.xml` Web Client ID
- ✅ Debug keystore exists
- ✅ SHA-1 fingerprint (extracted automatically)
- ✅ Build configuration

---

## 🔍 What Each File Does

### `google-services.json`
- **Purpose:** Tells your app which Firebase project to connect to
- **Used by:** Gradle build process (via google-services plugin)
- **Contains:** Project ID, API keys, OAuth client IDs
- **When to update:** After adding SHA-1 fingerprints in Firebase Console

### `strings.xml` (with `default_web_client_id`)
- **Purpose:** Tells Google Sign-In which OAuth client to use
- **Used by:** GoogleAuthService.kt at runtime
- **Contains:** Web Client ID for OAuth authentication
- **When to update:** Almost never (already configured)

### `debug.keystore`
- **Purpose:** Signs your debug builds with a certificate
- **Used by:** Gradle build process
- **Contains:** Private key and certificate for signing
- **When to extract SHA-1:** Once per developer, or when keystore changes

### SHA-1 in Firebase Console
- **Purpose:** Tells Firebase which apps are authorized to authenticate
- **Used by:** Google servers during sign-in verification
- **Contains:** Public fingerprint of your signing certificate
- **When to add:** For each developer, each keystore (debug/release)

---

## 🚨 Common Confusion Points

### "I have google-services.json, why doesn't it work?"
**Answer:** The SHA-1 fingerprint is NOT in `google-services.json`. It must be added separately in Firebase Console. The file only tells your app which project to use, not which certificates are authorized.

### "I added SHA-1 but still getting error"
**Answer:** Firebase takes 5-10 minutes to propagate changes. Wait, then try again.

### "Which SHA-1 do I use?"
**Answer:** 
- For development/testing: Extract from `~/.android/debug.keystore`
- For production: Extract from your release keystore
- You can (and should) add both to Firebase Console

### "Do I need to add SHA-256?"
**Answer:** No, only SHA-1 is required for Google Sign-In. SHA-256 is optional.

### "My teammate can't sign in"
**Answer:** Each developer has their own `debug.keystore` with a different SHA-1. Your teammate needs to:
1. Extract their SHA-1 from their keystore
2. Add it to Firebase Console (you can have multiple SHA-1s)
3. Wait 5-10 minutes

---

## 📚 More Help

- **Complete setup guide:** [FIREBASE_SETUP_GUIDE.md](FIREBASE_SETUP_GUIDE.md)
- **Troubleshooting:** [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
- **Quick reference:** [README.md](README.md#setting-up-google-sign-in)
- **Verification script:** `android-app/verify-firebase-setup.sh`

---

**TL;DR:** Get SHA-1 from keystore → Add to Firebase Console → Download updated google-services.json → Enable Google Sign-In in Firebase → Wait 5-10 min → Rebuild app → Test!
