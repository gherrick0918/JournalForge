# Testing the Sign-In Fix

## Quick Test Guide

After building and installing the updated app, follow these test scenarios to verify the fix works correctly.

## Test Scenarios

### ✅ Test 1: Fresh Sign-In (Primary Issue Test)
**This tests the main reported issue**

1. Uninstall the app completely (or clear app data)
2. Install the updated app
3. Open the app → should see LoginActivity
4. Tap "Sign In with Google"
5. Select your Google account
6. **Expected Result**: 
   - ✅ See "signed in successfully" toast
   - ✅ Immediately land on MainActivity (Quest Log screen)
   - ✅ App stays open (does NOT go to home screen)
   - ✅ Can use the app normally

**Previous behavior**: App would go to home screen after sign-in

---

### ✅ Test 2: Reopen After Sign-In (Crash Test)
**This tests the crash issue**

1. Sign in successfully (as in Test 1)
2. Press the Home button to background the app
3. Open the app again from launcher
4. **Expected Result**:
   - ✅ App opens directly to MainActivity
   - ✅ No crash
   - ✅ User remains signed in

**Previous behavior**: App would crash on reopening

---

### ✅ Test 3: Multiple Sign-In Attempts
**Tests activity stack management**

1. Open app → LoginActivity shown
2. Tap "Sign In with Google"
3. Press back during Google account picker
4. Tap "Sign In with Google" again
5. Select account and complete sign-in
6. **Expected Result**:
   - ✅ Sign-in completes successfully
   - ✅ No crash or unexpected behavior

---

### ✅ Test 4: Already Signed-In Launch
**Tests initial app launch with existing session**

1. Sign in and use the app
2. Close the app completely (swipe away from recent apps)
3. Wait a few seconds
4. Open the app again
5. **Expected Result**:
   - ✅ App opens directly to MainActivity
   - ✅ No flash of LoginActivity
   - ✅ User remains signed in

---

### ✅ Test 5: Sign Out and Sign In Again
**Tests the complete auth cycle**

1. From MainActivity, tap menu (⋮) → Sign Out
2. **Expected Result**: Return to LoginActivity
3. Tap "Sign In with Google"
4. Select account
5. **Expected Result**:
   - ✅ Sign-in succeeds
   - ✅ Navigate to MainActivity
   - ✅ No crash or loop

---

### ✅ Test 6: Back Button Behavior
**Tests activity stack is properly managed**

1. Sign in and reach MainActivity
2. Press the device Back button
3. **Expected Result**:
   - ✅ App exits (goes to home screen)
   - ✅ Does NOT go back to LoginActivity
   - ✅ Task is removed from recent apps

**This confirms the activity stack is clean**

---

## What to Look For

### ✅ Success Indicators
- Sign-in completes and stays on MainActivity
- No unexpected exits to home screen
- No crashes when reopening app
- Smooth transitions between screens
- Back button exits app (not loop to LoginActivity)

### ❌ Failure Indicators
- App goes to home screen after sign-in
- Crash when reopening app
- Infinite loop between LoginActivity and MainActivity
- Multiple instances of activities in task
- Back button navigates to LoginActivity from MainActivity

## Logcat Monitoring (Optional)

If you want to see detailed logs during testing:

```bash
adb logcat | grep -E "LoginActivity|MainActivity|GoogleAuthService"
```

### What to Look For in Logs
- `LoginActivity: User already signed in, navigating to MainActivity` - Good, means quick redirect
- `LoginActivity: Sign-in successful, navigating to MainActivity` - Good, successful auth
- `MainActivity: onCreate` - Should only see once per app launch
- No errors about "Unable to add window" or "Activity not found"

## Build and Install Commands

```bash
cd android-app
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
```

Or use Android Studio:
1. Build → Clean Project
2. Build → Rebuild Project
3. Run → Run 'app'

## Common Issues (If Fix Doesn't Work)

### If app still goes to home screen:
- Make sure you're testing the updated build
- Check that Firebase authentication is properly configured
- Verify google-services.json is up to date

### If app still crashes:
- Clear app data: Settings → Apps → JournalForge → Storage → Clear Data
- Check logcat for specific error messages
- Verify SHA-1 fingerprint is registered in Firebase Console

### If sign-in fails completely:
- This fix doesn't change auth logic, so this would be a different issue
- Check Firebase configuration
- Verify internet connection
- Check google-services.json is present

## Summary

The main test is **Test 1** and **Test 2** - these directly address the reported issue:
1. ✅ Sign in should complete and stay on MainActivity (not go to home screen)
2. ✅ Reopening app should not crash

If these two tests pass, the issue is fixed! 🎉
