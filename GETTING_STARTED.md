# Getting Started with JournalForge

This guide will help you set up and run the JournalForge native Android application.

## Prerequisites

### Required Software

1. **Android Studio** (latest version recommended)
   - Download from: https://developer.android.com/studio
   - Includes Android SDK and build tools

2. **Java Development Kit (JDK) 17** or later
   - Usually bundled with Android Studio
   - Verify installation: `java -version`

3. **Git** (for cloning the repository)
   - Download from: https://git-scm.com/

### Android SDK Requirements
- Minimum SDK: API 26 (Android 8.0)
- Target SDK: API 36 (Android 14+)
- Build Tools: Latest version
- Android Emulator or physical Android device for testing

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/gherrick0918/JournalForge.git
cd JournalForge/android-app
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select **File → Open**
3. Navigate to the `android-app` directory
4. Click **OK**
5. Wait for Gradle sync to complete

### 3. Install Dependencies

Dependencies are managed by Gradle and will be automatically downloaded during sync.

```bash
# Restore NuGet packages
dotnet restore
# Verify Gradle sync completed
./gradlew tasks
```

### 4. Configure Firebase (for Google Sign-In)

See the [README.md](README.md#setting-up-google-sign-in) for detailed instructions on setting up Google Sign-In.

## Running the Application

### Using Android Studio (Recommended)

1. Click the **Run** button (green play icon) or press `Shift+F10`
2. Select a device or emulator from the dropdown
3. Wait for the app to build and install
4. The app will launch automatically

### Using Gradle Command Line

#### Build Debug APK
```bash
./gradlew assembleDebug
```

The APK will be in `app/build/outputs/apk/debug/app-debug.apk`

#### Install on Device
```bash
./gradlew installDebug
```

#### Build and Run
```bash
./gradlew installDebug
adb shell am start -n com.journalforge.app/.ui.MainActivity
```

#### Build Release APK (requires keystore)
```bash
./gradlew assembleRelease
```

### Using an Emulator

1. In Android Studio, go to **Tools → Device Manager**
2. Click **Create Device**
3. Select a device definition (e.g., Pixel 5)
4. Select a system image (API 26 or higher)
5. Click **Finish**
6. Run the app with the new emulator selected

## Project Structure Overview

```
android-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/journalforge/app/
│   │   │   ├── models/          # Data classes
│   │   │   ├── services/        # Business logic
│   │   │   └── ui/              # Activities and fragments
│   │   ├── res/
│   │   │   ├── layout/          # XML layouts
│   │   │   ├── values/          # Strings, colors, themes
│   │   │   └── drawable/        # Images and icons
│   │   └── AndroidManifest.xml  # App configuration
│   └── build.gradle             # App dependencies
├── build.gradle                 # Project configuration
└── settings.gradle              # Module settings
```

## Key Features to Explore

1. **Main Activity**: View daily prompts and recent entries
2. **Journal Entry**: Create new entries with AI assistance
3. **History**: View all your journal entries
4. **Time Capsule**: Seal entries for future opening
5. **Settings**: Google Sign-In and cloud sync
6. **RPG Theme**: Fantasy-themed UI with medieval aesthetics

## Troubleshooting

### Build Errors

#### "SDK location not found"
1. Create `local.properties` in the `android-app` directory
2. Add: `sdk.dir=/path/to/Android/Sdk`
   - Windows: `C:\\Users\\YourName\\AppData\\Local\\Android\\Sdk`
   - macOS: `/Users/YourName/Library/Android/sdk`
   - Linux: `/home/YourName/Android/Sdk`

#### "Could not resolve dependencies"
1. Check your internet connection
2. Try: `./gradlew --refresh-dependencies`
3. Invalidate caches in Android Studio: **File → Invalidate Caches / Restart**

#### "Gradle sync failed"
1. Open Xcode and accept license agreements
2. Install Xcode command line tools:
   ```bash
1. Check Gradle version compatibility
2. Update Gradle wrapper: `./gradlew wrapper --gradle-version=8.2`
3. Sync project with Gradle files in Android Studio

### Runtime Errors

#### "App crashes on launch"
- Check Logcat in Android Studio for error messages
- Look for `AndroidRuntime` or `FATAL EXCEPTION` tags
- Verify `google-services.json` is present and valid

#### "Google Sign-In fails"
- See [README.md](README.md#setting-up-google-sign-in) for configuration
- Check that SHA-1 fingerprint is registered in Firebase
- Verify Web Client ID is configured

### Common Issues

#### "Could not find google-services.json"
1. Ensure file is in `android-app/app/` directory
2. The file should be at the same level as `build.gradle`
3. Check that google-services plugin is applied in `build.gradle`

#### Android emulator slow
- Enable hardware acceleration (HAXM on Intel, WHPX on Windows)
- Use x86_64 system images on x86 machines
- Increase emulator RAM allocation
- Consider using a physical device for better performance

## Development Tips

### Debugging

1. **Logcat**: View logs in Android Studio's Logcat window
2. **Breakpoints**: Set breakpoints in Kotlin code for step-through debugging
3. **Layout Inspector**: Inspect view hierarchy at runtime

### Testing on Physical Devices

1. Enable **Developer Options** on your Android device:
   - Go to **Settings → About Phone**
   - Tap **Build Number** 7 times
2. Enable **USB Debugging** in Developer Options
3. Connect device via USB
4. Trust the computer on the device
5. Device should appear in Android Studio's device dropdown

### Performance Profiling

- Use Android Studio's **Profiler** to monitor CPU, memory, and network usage
- Check for memory leaks with the Memory Profiler
- Optimize database queries and API calls

## Next Steps

1. **Explore the Code**: Review models, services, and activities in `app/src/main/java`
2. **Customize the Theme**: Modify colors in `res/values/colors.xml`
3. **Add Features**: Extend services with Firestore integration
4. **Integrate AI**: Configure OpenAI API key in `AIService.kt`
5. **Configure Google Sign-In**: Follow the setup guide in [README.md](README.md)

## Resources

- [Android Developer Documentation](https://developer.android.com/docs)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Firebase for Android](https://firebase.google.com/docs/android/setup)
- [Material Design 3](https://m3.material.io/)

## Support

For issues or questions:
1. Check the [README.md](README.md) for setup details
2. Review [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
3. Open an issue on GitHub

---

Happy journaling! ⚔️📜
