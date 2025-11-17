# Testing JournalForge Android App

This document describes how to test the JournalForge Android application.

## Prerequisites

- Android Studio (latest version)
- Android SDK 26 or higher
- Physical Android device or emulator
- JDK 17 or higher

## Testing Steps

### 1. Clean the Project
```bash
cd android-app

# Clean build artifacts
./gradlew clean

# Or manually delete build directories
rm -rf app/build .gradle
```

### 2. Restore Dependencies
```bash
# Gradle will automatically download dependencies during build
./gradlew dependencies
```

### 3. Build the App
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires keystore)
./gradlew assembleRelease
```

### 4. Run Tests
```bash
# Run unit tests
./gradlew test

# Run instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Generate test coverage report
./gradlew jacocoTestReport
```

## Manual Testing Checklist

### Basic Functionality
- [ ] App launches successfully
- [ ] Main activity displays correctly
- [ ] Navigation between screens works
- [ ] Journal entries can be created
- [ ] Journal entries can be saved
- [ ] Journal entries can be viewed in history
- [ ] Entries can be deleted

### Google Sign-In
- [ ] Settings page opens
- [ ] "Sign In with Google" button is visible
- [ ] Clicking sign-in opens Google account picker
- [ ] Selecting account completes authentication
- [ ] User email displays after sign-in
- [ ] Sign-out works correctly

### AI Features
- [ ] Daily prompts display on main page
- [ ] AI suggestions can be requested
- [ ] AI conversation appears in journal entry

### Time Capsule
- [ ] Time capsules can be created
- [ ] Capsules show sealed status
- [ ] Future dates can be set
- [ ] Capsules can be unsealed when ready

### Voice Dictation (Android only)
- [ ] Microphone permission is requested
- [ ] Voice recording starts
- [ ] Speech is transcribed to text
- [ ] Transcription appears in entry field

## Test on Multiple Devices

Test the app on various configurations:

### Device Types
- Phone (small screen)
- Tablet (large screen)
- Foldable device (if available)

### Android Versions
- Android 8.0 (API 26) - Minimum supported
- Android 10 (API 29)
- Android 12 (API 31)
- Android 14+ (API 34+) - Latest

### Screen Densities
- mdpi (160dpi)
- hdpi (240dpi)
- xhdpi (320dpi)
- xxhdpi (480dpi)
- xxxhdpi (640dpi)

## Expected Behavior

### Successful Build
- No compilation errors
- No lint warnings (or acceptable warnings documented)
- APK generated in `app/build/outputs/apk/debug/`

### Successful Installation
- App installs without errors
- App icon appears in launcher
- App can be opened from launcher

### Runtime Stability
- No crashes on normal usage
- No ANR (Application Not Responding) errors
- Smooth scrolling and transitions
- Fast response times

## Common Test Failures

### Build Failures

**Error: "SDK location not found"**
- Create `local.properties` with SDK path
- Example: `sdk.dir=/Users/yourname/Library/Android/sdk`

**Error: "Could not resolve dependencies"**
- Check internet connection
- Run `./gradlew --refresh-dependencies`
- Check proxy settings if behind corporate firewall

### Test Failures

**Instrumentation tests fail**
- Ensure device/emulator is connected: `adb devices`
- Unlock the device screen
- Grant necessary permissions before running tests

**Unit tests fail**
- Check test assertions are correct
- Verify mock data is properly set up
- Review test logs for specific failure reasons

## Debugging Tests

### View Test Results
```bash
# Test results are in:
app/build/reports/tests/testDebugUnitTest/index.html

# Open in browser:
open app/build/reports/tests/testDebugUnitTest/index.html
```

### Run Specific Test
```bash
# Run a specific test class
./gradlew test --tests "com.journalforge.app.ExampleTest"

# Run a specific test method
./gradlew test --tests "com.journalforge.app.ExampleTest.testMethod"
```

### Debug with Android Studio
1. Right-click on test class or method
2. Select "Debug 'TestName'"
3. Set breakpoints as needed
4. Step through code execution

## Performance Testing

### Memory Profiling
1. Open Android Studio Profiler
2. Select your running app
3. Monitor memory usage
4. Look for memory leaks

### Network Profiling
1. Monitor API calls in Profiler
2. Check request/response times
3. Verify proper error handling

### Battery Usage
1. Settings → Battery → Battery Usage
2. Check JournalForge battery consumption
3. Should be minimal when app is in background

## Continuous Integration

When setting up CI/CD:

```yaml
# Example GitHub Actions workflow
- name: Run Tests
  run: |
    cd android-app
    ./gradlew test
    ./gradlew connectedAndroidTest

- name: Upload Test Reports
  uses: actions/upload-artifact@v2
  with:
    name: test-reports
    path: android-app/app/build/reports/
```

## Additional Notes

- Always test on both debug and release builds
- Test with and without Google Play Services
- Test with different network conditions (WiFi, mobile data, offline)
- Test with different system languages if app is localized
- Test accessibility features (TalkBack, large text, etc.)

## Resources

- [Android Testing Documentation](https://developer.android.com/training/testing)
- [JUnit Documentation](https://junit.org/junit4/)
- [Espresso Testing](https://developer.android.com/training/testing/espresso)
- [Mockito Documentation](https://site.mockito.org/)

---

For issues or questions about testing, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md) or open an issue on GitHub.
