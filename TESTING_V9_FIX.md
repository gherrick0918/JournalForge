# Testing Guide - V9 Sign-In Crash Fix

## 🎯 What to Test

This fix resolves the navigation race condition that caused the app to crash after sign-in.

## ✅ Test Scenarios

### Scenario 1: Fresh Sign-In (Primary Issue)

**Steps**:
1. Uninstall the app or clear app data
2. Install and launch the app
3. Tap "Sign In" button
4. Select your Google account from account picker
5. Wait for "Sign in successful" message
6. **OBSERVE**: App should navigate to MainActivity
7. **VERIFY**: App stays in MainActivity (does NOT close)
8. **VERIFY**: MainActivity displays journal entries/prompts

**Expected Result**: ✅ App navigates to MainActivity and stays there

**V8 Behavior (Bug)**: ❌ App navigates to MainActivity then immediately closes

**Logcat Messages to Look For**:
```
LoginActivity: Sign-in button clicked, launching Google Sign-In
LoginActivity: Sign-in successful
LoginActivity: Auth state changed to Authenticated, navigating to MainActivity
MainActivity: Skipping auth state change during initialization: Authenticated
MainActivity: Auth state is Authenticated
```

---

### Scenario 2: App Reopen After Closing (Secondary Issue)

**Steps**:
1. Complete Scenario 1 successfully
2. Press Home button (or Recent Apps) to close the app
3. Wait a few seconds
4. Reopen the app from launcher
5. **OBSERVE**: App should go directly to MainActivity
6. **VERIFY**: No crash, no login screen shown

**Expected Result**: ✅ App opens directly to MainActivity

**V8 Behavior (Bug)**: ❌ App crashes or gets stuck in navigation loop

**Logcat Messages to Look For**:
```
LoginActivity: Already authenticated on startup, navigating to MainActivity
MainActivity: Skipping auth state change during initialization: Authenticated
MainActivity: Auth state is Authenticated
```

---

### Scenario 3: Sign Out

**Steps**:
1. From MainActivity, tap the menu (three dots) in top-right
2. Tap "Sign Out"
3. **OBSERVE**: App should navigate to LoginActivity
4. **VERIFY**: Login screen is shown with "Sign In" button

**Expected Result**: ✅ Clean navigation to LoginActivity

**Logcat Messages to Look For**:
```
MainActivity: Auth state changed to Unauthenticated, redirecting to LoginActivity
LoginActivity: Auth state is Unauthenticated
```

---

### Scenario 4: App Resume from Background

**Steps**:
1. Sign in and reach MainActivity
2. Press Home button
3. Wait 10 seconds
4. Reopen the app
5. **VERIFY**: MainActivity is still shown
6. **VERIFY**: No navigation occurs

**Expected Result**: ✅ App resumes normally in MainActivity

---

### Scenario 5: Screen Rotation During Sign-In

**Steps**:
1. Start sign-in process
2. While on account picker, rotate device
3. Complete sign-in
4. **VERIFY**: Navigation to MainActivity succeeds

**Expected Result**: ✅ Handles rotation gracefully

---

### Scenario 6: Multiple Rapid Sign-In Attempts

**Steps**:
1. Sign in successfully
2. Sign out
3. Immediately sign in again
4. Sign out
5. Sign in again
6. **VERIFY**: Each transition is clean, no crashes

**Expected Result**: ✅ All transitions work correctly

---

## 🔍 What Changed (For Reference)

### LoginActivity
- **Added**: `hasNavigated` flag to prevent duplicate navigation
- **Behavior**: Observer and sync check both try to navigate, but flag ensures only first one succeeds

### MainActivity  
- **Added**: `isInitializing` flag to defer observer during startup
- **Added**: `hasNavigatedToLogin` flag to prevent duplicate navigation
- **Behavior**: Observer is ignored during onCreate, sync check runs first, then observer is enabled

---

## 🐛 What to Watch For

### Signs the Fix Worked
✅ App navigates to MainActivity after sign-in and STAYS there
✅ App reopens to MainActivity without crash
✅ Sign-out navigates cleanly to LoginActivity
✅ No navigation loops or freezes

### Signs of Remaining Issues (Report These!)
❌ App still closes after sign-in success message
❌ App crashes when reopening
❌ Navigation loops (back and forth between activities)
❌ App freezes during sign-in
❌ "Sign In" button doesn't work

---

## 📊 Logcat Monitoring

### How to View Logs

**Using ADB**:
```bash
adb logcat | grep -E "LoginActivity|MainActivity|AuthStateManager"
```

**In Android Studio**:
1. Open Logcat tab at bottom
2. Filter by: `LoginActivity|MainActivity|AuthStateManager`
3. Watch for navigation messages

### Key Log Messages

**Good Signs (Expected)**:
- "Auth state changed to Authenticated, navigating to MainActivity"
- "Skipping auth state change during initialization"
- "Already navigated, skipping" (shows duplicate prevention working)
- "Already navigated, ignoring auth state change" (shows duplicate prevention working)

**Bad Signs (Report These)**:
- "User not authenticated on startup, redirecting to LoginActivity" (when you ARE signed in)
- Multiple "navigating to" messages in rapid succession
- Crash stack traces mentioning LoginActivity or MainActivity

---

## ✨ Testing Checklist

Print this and check off as you test:

```
□ Fresh sign-in works, app stays in MainActivity
□ App reopen goes to MainActivity without crash  
□ Sign-out navigates to LoginActivity cleanly
□ App resume from background works
□ Screen rotation during sign-in works
□ Multiple sign-in/sign-out cycles work
□ Logcat shows expected messages
□ No crashes observed
□ No navigation loops observed
```

---

## 🚨 If Issues Persist

If you still experience crashes or navigation issues:

1. **Capture Logcat**:
   ```bash
   adb logcat > crash_log.txt
   ```

2. **Note Exact Steps** that caused the issue

3. **Check for**:
   - What messages appeared in logcat?
   - Did you see "Already navigated, skipping"?
   - Did you see "Skipping auth state change during initialization"?
   - Was there a crash stack trace?

4. **Report** with:
   - Exact steps to reproduce
   - Logcat output
   - Which scenario failed

---

## 📝 Summary

This fix resolves navigation race conditions by:
1. Preventing duplicate navigation attempts with flags
2. Deferring MainActivity observer until initialization completes
3. Coordinating synchronous checks with reactive observers

**Expected outcome**: Clean, reliable sign-in flow with no crashes or navigation loops.

**Test thoroughly** and report any issues with logcat output!
