# Google Sign-In Setup Guide

This guide explains how to set up Google Sign-In and cloud sync for JournalForge.

## Overview

JournalForge supports Google Sign-In to enable cloud backup and synchronization of journal entries. This feature allows users to:

- **Backup entries** to Google's cloud storage
- **Sync across devices** - access your journal from multiple devices
- **Prevent data loss** - entries are safely stored even if you lose your device
- **Secure authentication** - protected by Google's security infrastructure

## Current Status

⚠️ **Note**: The Google Sign-In infrastructure is implemented but requires additional configuration to be fully functional. The app includes:

- ✅ Authentication service interface (`IGoogleAuthService`)
- ✅ Cloud sync service interface (`ICloudSyncService`)
- ✅ Settings page with sign-in UI
- ✅ Sync status tracking
- ⚠️ Placeholder implementation (needs real OAuth integration)

## Setup Instructions

To enable Google Sign-In in JournalForge, follow these steps:

### 1. Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Note your project ID for later use

### 2. Enable Google Sign-In API

1. In the Google Cloud Console, navigate to **APIs & Services** > **Library**
2. Search for "Google Sign-In API"
3. Click **Enable**

### 3. Configure OAuth Consent Screen

1. Go to **APIs & Services** > **OAuth consent screen**
2. Select **External** user type (or Internal if for organization only)
3. Fill in required information:
   - App name: "JournalForge"
   - User support email: Your email
   - Developer contact: Your email
4. Add scopes (at minimum):
   - `openid`
   - `profile`
   - `email`
5. Save and continue

### 4. Create OAuth Credentials

#### For Android:

1. Go to **APIs & Services** > **Credentials**
2. Click **Create Credentials** > **OAuth client ID**
3. Select **Android** as application type
4. Enter:
   - Name: "JournalForge Android"
   - Package name: `com.journalforge.app` (from `JournalForge.csproj`)
   - SHA-1 certificate fingerprint (get from your keystore)
5. Click **Create**
6. Note the **Client ID** for later use

To get your SHA-1 fingerprint:
```bash
# For debug keystore (default location)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# For release keystore
keytool -list -v -keystore journalforge.keystore -alias journalforge
```

#### For iOS (Optional):

1. Create OAuth client ID for **iOS**
2. Enter:
   - Name: "JournalForge iOS"
   - Bundle ID: `com.journalforge.app`
3. Note the **Client ID**

### 5. Choose Integration Method

You have two main options for implementing Google Sign-In:

#### Option A: Firebase Authentication (Recommended)

Firebase provides a simpler integration path and includes additional features like Firestore for data storage.

1. **Add Firebase to your project:**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Add a new project or link to your Google Cloud project
   - Add Android app with package name `com.journalforge.app`
   - Download `google-services.json`

2. **Install NuGet packages:**
   ```bash
   dotnet add package Xamarin.Google.Android.Play.Services.Auth
   dotnet add package Plugin.FirebaseAuth
   ```

3. **Update `GoogleAuthService.cs`:**
   Replace the placeholder implementation with Firebase Auth code.

#### Option B: Google Sign-In SDK

Use the standalone Google Sign-In SDK without Firebase.

1. **Install NuGet packages:**
   ```bash
   dotnet add package Xamarin.Google.Android.Play.Services.Auth
   dotnet add package Xamarin.GooglePlayServices.Auth
   ```

2. **Update `GoogleAuthService.cs`:**
   Implement OAuth flow using Google Sign-In SDK.

### 6. Configure Cloud Storage

Choose a backend for storing journal entries:

#### Option A: Firebase Firestore (Recommended)

1. Enable Firestore in Firebase Console
2. Set up security rules for user-specific data
3. Install `Plugin.CloudFirestore` NuGet package
4. Update `CloudSyncService.cs` to use Firestore

Example security rules:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/entries/{entryId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

#### Option B: Google Drive API

1. Enable Google Drive API in Google Cloud Console
2. Add scopes: `https://www.googleapis.com/auth/drive.file`
3. Install Google Drive API client
4. Update `CloudSyncService.cs` to use Drive API

### 7. Update Configuration Files

#### For Android:

1. Place `google-services.json` in the Android project folder
2. Update `JournalForge.csproj` to include the file:
   ```xml
   <ItemGroup Condition="$(TargetFramework.Contains('android'))">
     <GoogleServicesJson Include="google-services.json" />
   </ItemGroup>
   ```

3. Add initialization code to Android `MainActivity.cs` or platform-specific code.

### 8. Implement Authentication Flow

Update `Services/GoogleAuthService.cs` with actual OAuth implementation:

```csharp
public async Task<bool> SignInAsync()
{
    try
    {
        // Example using Firebase Auth
        var googleSignIn = CrossFirebaseAuth.Current.GoogleSignIn;
        var credential = await googleSignIn.SignInAsync();
        
        var result = await CrossFirebaseAuth.Current.SignInWithCredentialAsync(credential);
        
        if (result.User != null)
        {
            _currentUser = new UserProfile
            {
                Id = result.User.Uid,
                Email = result.User.Email,
                Name = result.User.DisplayName,
                PhotoUrl = result.User.PhotoUrl?.ToString()
            };
            
            _isSignedIn = true;
            AuthenticationStateChanged?.Invoke(this, true);
            return true;
        }
        
        return false;
    }
    catch (Exception ex)
    {
        System.Diagnostics.Debug.WriteLine($"Error signing in: {ex.Message}");
        return false;
    }
}
```

### 9. Implement Cloud Sync

Update `Services/CloudSyncService.cs` with actual cloud storage implementation:

```csharp
public async Task<bool> SyncEntriesAsync()
{
    try
    {
        var user = await _authService.GetCurrentUserAsync();
        if (user == null) return false;
        
        var localEntries = await _journalService.GetAllEntriesAsync();
        
        // Upload to Firestore
        var firestore = CrossCloudFirestore.Current.Instance;
        var collection = firestore.Collection($"users/{user.Id}/entries");
        
        foreach (var entry in localEntries)
        {
            await collection.Document(entry.Id).SetAsync(entry);
        }
        
        _lastSyncTime = DateTime.Now;
        await SaveSyncMetadataAsync();
        
        return true;
    }
    catch (Exception ex)
    {
        System.Diagnostics.Debug.WriteLine($"Error syncing: {ex.Message}");
        return false;
    }
}
```

## Testing

1. **Build and run the app** on an Android device or emulator
2. **Navigate to Settings** from the app menu
3. **Tap "Sign In with Google"**
4. **Complete the OAuth flow** in the browser or Google Sign-In UI
5. **Verify authentication** - you should see your email in the Settings page
6. **Tap "Sync Now"** to test cloud synchronization
7. **Check cloud storage** (Firestore or Drive) to verify entries are uploaded

## Troubleshooting

### "Sign-In Failed" Error

- Verify OAuth client ID is correct
- Check SHA-1 fingerprint matches your keystore
- Ensure Google Sign-In API is enabled
- Check package name matches in all configurations

### "Permission Denied" Error

- Review Firestore security rules
- Ensure user is authenticated before syncing
- Check OAuth scopes include necessary permissions

### "Network Error" During Sync

- Verify internet connection
- Check API quotas in Google Cloud Console
- Ensure Cloud Storage (Firestore/Drive) API is enabled

## Security Considerations

1. **Never commit credentials** to version control
   - `google-services.json` should be in `.gitignore`
   - OAuth client secrets should not be in code

2. **Use secure storage** for sensitive data
   - Store auth tokens in secure storage
   - Encrypt local data if needed

3. **Implement proper error handling**
   - Handle authentication failures gracefully
   - Provide clear user feedback

4. **Follow OAuth best practices**
   - Use PKCE flow where applicable
   - Implement token refresh
   - Handle token expiration

## Cost Considerations

- **Firebase Free Tier** includes:
  - 50,000 reads/day
  - 20,000 writes/day
  - 1 GB storage
  - Authentication: unlimited

- **Google Drive API** (Free tier):
  - 1 billion queries/day
  - 15 GB storage per user

For a personal journaling app, the free tier should be sufficient.

## Additional Resources

- [Firebase Authentication Documentation](https://firebase.google.com/docs/auth)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
- [Cloud Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Google Drive API Documentation](https://developers.google.com/drive)

## Support

If you encounter issues setting up Google Sign-In, please:
1. Check the troubleshooting section above
2. Review Google Cloud Console logs
3. Check Firebase Console for authentication logs
4. Open an issue on GitHub with detailed error messages

---

**Note**: This is a complex setup that requires external configuration. The app is fully functional without Google Sign-In, storing all data locally on the device.
