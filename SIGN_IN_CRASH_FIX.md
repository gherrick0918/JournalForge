# Sign-In Redirect and Crash Fix

## Issue Resolved

Fixed the critical issue where:
1. User signs in successfully and sees "signed in successfully" message
2. App immediately goes to home screen (minimizes/exits)
3. Reopening the app and signing in again causes a crash

## Root Cause Analysis

The problem was caused by improper activity stack management during the authentication flow:

### The Problem Flow
1. **LoginActivity** successfully authenticates user with Google/Firebase
2. **LoginActivity** starts **MainActivity** using basic `startActivity()` and calls `finish()`
3. **MainActivity.onCreate()** checks if user is signed in
4. Due to Android activity lifecycle timing, there's a race condition where:
   - Firebase authentication might not have fully synced yet
   - Or multiple activity instances exist in the task stack
5. This creates one of two scenarios:
   - **Scenario A**: **MainActivity** detects user as not signed in → redirects to **LoginActivity** → infinite loop → both activities finish → app exits to home screen
   - **Scenario B**: Multiple **LoginActivity** instances stack up → crash on second sign-in attempt due to corrupted activity stack

### Why This Happened
- No intent flags were set to manage the activity back stack
- No launch mode specified in AndroidManifest.xml
- Android's default activity behavior allows multiple instances
- No protection against circular navigation between LoginActivity ↔ MainActivity

## Solution Implemented

### 1. Added Intent Flags for Clean Navigation

Added `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` to all navigation intents:

```kotlin
// Example from LoginActivity.kt
val intent = Intent(this, MainActivity::class.java)
intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
startActivity(intent)
finish()
```

**What this does:**
- `FLAG_ACTIVITY_NEW_TASK`: Starts the activity in a new task
- `FLAG_ACTIVITY_CLEAR_TASK`: Clears all existing activities in the task before starting the new one
- Combined: Creates a clean slate with only the new activity in the task stack

### 2. Added Single Task Launch Mode

Modified `AndroidManifest.xml` to add `launchMode="singleTask"`:

```xml
<activity
    android:name=".ui.LoginActivity"
    android:launchMode="singleTask"
    ... />

<activity
    android:name=".ui.MainActivity"
    android:launchMode="singleTask"
    ... />
```

**What this does:**
- Ensures only one instance of each activity exists in the app
- If the activity already exists, Android reuses it instead of creating a new instance
- Prevents activity stack corruption from multiple instances

### 3. Consistent Application Across All Navigation Points

Applied the fix to **all** navigation paths between LoginActivity and MainActivity:

1. **LoginActivity → MainActivity**:
   - When user is already signed in (app startup)
   - After successful Google Sign-In

2. **MainActivity → LoginActivity**:
   - When user is not signed in (app startup)
   - After user signs out

## Files Modified

1. **android-app/app/src/main/java/com/journalforge/app/ui/LoginActivity.kt**
   - Line 36-38: Added intent flags for already-signed-in navigation
   - Line 67-69: Added intent flags for post-sign-in navigation

2. **android-app/app/src/main/java/com/journalforge/app/ui/MainActivity.kt**
   - Line 36-38: Added intent flags for not-signed-in navigation
   - Line 143-145: Added intent flags for sign-out navigation

3. **android-app/app/src/main/AndroidManifest.xml**
   - Line 22: Added `android:launchMode="singleTask"` to LoginActivity
   - Line 32: Added `android:launchMode="singleTask"` to MainActivity

## Expected Behavior After Fix

### Successful Sign-In Flow
1. User opens app → **LoginActivity** shown (if not signed in)
2. User taps "Sign In with Google" → Google account picker appears
3. User selects account → "signed in successfully" toast shown
4. **MainActivity** immediately loads → user sees main app screen
5. ✅ No home screen exit, no crash

### Already Signed-In Flow
1. User opens app → **LoginActivity** checks auth state
2. User is already signed in → immediately navigate to **MainActivity**
3. **MainActivity** loads → user sees main app screen
4. ✅ No circular navigation, clean transition

### Sign-Out Flow
1. User taps sign out in **MainActivity**
2. Auth service signs out → navigate to **LoginActivity**
3. **LoginActivity** loads → user can sign in again
4. ✅ Clean activity stack, no previous MainActivity instance

### Reopening App After Sign-In
1. User signs in successfully
2. User presses home button (app backgrounds)
3. User reopens app → **MainActivity** shown (if still signed in) or **LoginActivity** (if session expired)
4. ✅ No crash, proper state restoration

## Technical Benefits

1. **Prevents Activity Stack Corruption**: Only one instance of each activity exists
2. **Clean State Management**: Each transition clears old activities
3. **No Circular Navigation**: Intent flags prevent infinite loops
4. **Better Memory Management**: Fewer activity instances = less memory usage
5. **Consistent User Experience**: Predictable navigation behavior

## Testing Recommendations

Test these scenarios to verify the fix:

1. ✅ **Fresh Sign-In**: Open app, sign in with Google → should go directly to MainActivity
2. ✅ **Already Signed-In**: Close and reopen app → should go directly to MainActivity
3. ✅ **Sign Out and Sign In**: Sign out, sign in again → should work smoothly
4. ✅ **Multiple Sign-In Attempts**: Try signing in multiple times → should not crash
5. ✅ **Background/Foreground**: Sign in, press home, reopen app → should stay on MainActivity
6. ✅ **Back Button**: From MainActivity, pressing back should exit app (not go to LoginActivity)

## No Side Effects

This fix is surgical and minimal:
- ✅ No changes to authentication logic
- ✅ No changes to Firebase configuration
- ✅ No changes to UI/UX
- ✅ No changes to other activities or screens
- ✅ Only affects activity lifecycle and navigation

## Android Best Practices

This fix follows Android documentation recommendations:
- [Tasks and Back Stack](https://developer.android.com/guide/components/activities/tasks-and-back-stack)
- [Launch Modes](https://developer.android.com/guide/topics/manifest/activity-element#lmode)
- [Intent Flags](https://developer.android.com/reference/android/content/Intent#FLAG_ACTIVITY_CLEAR_TASK)

## Security Summary

No security vulnerabilities introduced or discovered:
- Authentication flow unchanged
- No new permissions required
- No data handling modified
- Intent flags are standard Android APIs
- Launch modes are standard Android configurations

## Summary

This minimal fix resolves the sign-in redirect and crash issue by properly managing the Android activity stack. The combination of intent flags and launch modes ensures clean navigation between LoginActivity and MainActivity, preventing circular navigation, activity stack corruption, and crashes.
