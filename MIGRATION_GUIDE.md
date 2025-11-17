# Migration Guide: .NET MAUI to Native Android

This document explains the migration from the .NET MAUI cross-platform app to a native Android application.

## Why Migrate?

### Problems with MAUI
1. **Slow Build Times**: MAUI builds are significantly slower, especially with Firebase dependencies
2. **Complex Setup**: Cross-platform complexity when only targeting Android
3. **Firebase Integration**: Better native support in Android
4. **Development Experience**: Android Studio provides better tools for Android-specific development

### Benefits of Native Android
1. **Fast Builds**: Native Android builds are 3-5x faster
2. **Better Firebase Integration**: First-class support for Firebase services
3. **Modern Kotlin**: Type-safe, concise language with great tooling
4. **Simplified Development**: Single platform, standard tools

## Architecture Comparison

### MAUI Structure
```
JournalForge/
├── Models/           # C# data models
├── Services/         # C# business logic
├── ViewModels/       # MVVM view models
├── Pages/            # XAML pages
└── Platforms/
    └── Android/      # Android-specific code
```

### Native Android Structure
```
android-app/
├── app/src/main/
│   ├── java/com/journalforge/app/
│   │   ├── models/          # Kotlin data classes
│   │   ├── services/        # Business logic
│   │   └── ui/              # Activities (replaces Pages)
│   ├── res/
│   │   ├── layout/          # XML layouts (replaces XAML)
│   │   ├── values/          # Strings, colors, themes
│   │   └── menu/
│   └── AndroidManifest.xml
└── build.gradle
```

## Key Changes

### 1. Language: C# → Kotlin

**MAUI (C#)**
```csharp
public class JournalEntry
{
    public string Id { get; set; } = Guid.NewGuid().ToString();
    public string Title { get; set; } = string.Empty;
    public DateTime CreatedDate { get; set; } = DateTime.Now;
}
```

**Android (Kotlin)**
```kotlin
data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    val createdDate: Date = Date()
)
```

### 2. UI: XAML → XML Layouts

**MAUI (XAML)**
```xml
<ContentPage xmlns="http://schemas.microsoft.com/dotnet/2021/maui">
    <Button Text="Save" Clicked="OnSaveClicked" />
</ContentPage>
```

**Android (XML)**
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
    <Button 
        android:id="@+id/btn_save"
        android:text="@string/btn_save" />
</LinearLayout>
```

### 3. Architecture: MVVM → Activity/Fragment

**MAUI**: Uses ViewModels with data binding
**Android**: Activities with direct view manipulation (or can use ViewModel if desired)

### 4. Firebase Integration

**MAUI**
```csharp
// Required Plugin.Firebase NuGet package
// Complex configuration across platforms
```

**Android**
```kotlin
// Native Firebase SDK
implementation 'com.google.firebase:firebase-auth-ktx'
implementation 'com.google.android.gms:play-services-auth:20.7.0'
```

## Component Migration

### Services

All services were ported to Kotlin:

| MAUI Service | Android Service | Status |
|--------------|----------------|---------|
| GoogleAuthService.cs | GoogleAuthService.kt | ✅ Completed |
| JournalEntryService.cs | JournalEntryService.kt | ✅ Completed |
| AIService.cs | AIService.kt | ✅ Completed |
| CloudSyncService.cs | - | ⏳ Not yet implemented |
| TimeCapsuleService.cs | - | ⏳ Not yet implemented |

### UI Pages

| MAUI Page | Android Activity | Status |
|-----------|-----------------|---------|
| MainPage.xaml | MainActivity.kt | ✅ Completed |
| JournalEntryPage.xaml | JournalEntryActivity.kt | ✅ Completed |
| SettingsPage.xaml | SettingsActivity.kt | ✅ Completed |
| HistoryPage.xaml | HistoryActivity.kt | 🚧 Stub |
| TimeCapsulePage.xaml | TimeCapsuleActivity.kt | 🚧 Stub |

### Features

| Feature | MAUI | Android | Notes |
|---------|------|---------|-------|
| Journal Entries | ✅ | ✅ | Fully migrated |
| Google Sign-In | 🚧 Partial | ✅ | Completed in Android with Firebase |
| AI Integration | ✅ | ✅ | OpenAI API support |
| Local Storage | ✅ JSON | ✅ JSON | Same approach |
| Speech-to-Text | ✅ | ⏳ | Not yet migrated |
| Time Capsules | ✅ | 🚧 | Stub implementation |
| History/Search | ✅ | 🚧 | Stub implementation |

## Firebase Google Sign-In Setup

### What's Already Done

1. ✅ Firebase SDK integrated in `build.gradle`
2. ✅ `google-services.json` copied from MAUI project
3. ✅ `GoogleAuthService.kt` implemented with Firebase Auth
4. ✅ `SettingsActivity.kt` with sign-in UI
5. ✅ Activity result handling for sign-in flow

### What You Need to Do

1. **Get Web Client ID from Firebase Console**
   ```
   Firebase Console → Project Settings → General
   → Your apps → Web app
   → Copy the Web Client ID
   ```

2. **Update GoogleAuthService.kt**
   ```kotlin
   // Line 27 in GoogleAuthService.kt
   .requestIdToken("YOUR_WEB_CLIENT_ID_HERE")
   ```

3. **Enable Google Sign-In in Firebase**
   ```
   Firebase Console → Authentication → Sign-in method
   → Enable "Google"
   ```

4. **Add SHA-1 fingerprint (for debug builds)**
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore \
     -alias androiddebugkey -storepass android -keypass android
   
   # Copy SHA-1 and add to Firebase Console:
   # Project Settings → Your apps → Android app → Add fingerprint
   ```

### Testing Google Sign-In

1. Build and install the app
2. Navigate to Settings
3. Tap "Sign In with Google"
4. Complete OAuth flow in browser/Google prompt
5. Verify signed-in status shows your email

## Build Comparison

### MAUI Build
```bash
# Takes 5-10 minutes on first build
# Slow incremental builds (2-3 minutes)
dotnet build -f net9.0-android
```

### Android Build
```bash
# Takes 1-2 minutes on first build  
# Fast incremental builds (10-30 seconds)
./gradlew assembleDebug
```

## Development Workflow

### MAUI
1. Edit C# code or XAML
2. Wait 2-3 minutes for rebuild
3. Deploy to device/emulator
4. Repeat

### Android
1. Edit Kotlin code or XML
2. Wait 10-30 seconds for rebuild
3. Hot swap changes if possible (instant)
4. Repeat

## Next Steps

1. **Complete Firebase Setup**
   - Configure Web Client ID
   - Test Google Sign-In flow
   - Implement cloud sync with Firestore

2. **Complete Missing Features**
   - Implement HistoryActivity with full entry list
   - Implement TimeCapsuleActivity with sealing/unsealing
   - Port speech-to-text functionality

3. **Optimize**
   - Add app icon and splash screen
   - Implement proper error handling
   - Add loading states and progress indicators

4. **Test**
   - Test all features on real devices
   - Verify Firebase authentication
   - Test local storage persistence

## Troubleshooting

### Build Errors

**"Could not find google-services.json"**
- Ensure file is in `app/` directory
- Check build.gradle has `id 'com.google.gms.google-services'`

**"Failed to resolve: firebase-auth-ktx"**
- Check internet connection
- Run `./gradlew --refresh-dependencies`

### Sign-In Issues

**"Sign-in failed" or error 10**
- Verify Web Client ID is correct
- Check SHA-1 fingerprint is added to Firebase
- Ensure Google Sign-In is enabled in Firebase Console

**"IdpResponse is null"**
- User cancelled sign-in
- Normal behavior, not an error

## Resources

- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Firebase Authentication](https://firebase.google.com/docs/auth/android/google-signin)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Android Developer Guide](https://developer.android.com/guide)

## Questions?

Check the README.md in the `android-app/` directory for more details on the project structure and configuration.
