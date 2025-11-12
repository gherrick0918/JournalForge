# Testing the Android Build Directory Fix

This document describes how to test the fix for the XAGR7023 and XARDF7024 Android build errors.

## Prerequisites

Ensure you have the .NET MAUI workload installed:
```bash
dotnet workload install maui
```

## Testing Steps

### 1. Clean the Project
```bash
# Remove any existing build artifacts
dotnet clean
# Or manually delete obj and bin directories
rm -rf JournalForge/obj JournalForge/bin
```

### 2. Restore Dependencies
```bash
dotnet restore
```

### 3. Build for Android
```bash
# Build the Android target specifically
dotnet build -f net9.0-android
```

### 4. Verify Success
The build should complete without the following errors:
- **XAGR7023**: `System.IO.DirectoryNotFoundException: Could not find a part of the path '...\obj\Debug\net9.0-android\res'`
- **XARDF7024**: `System.IO.IOException: The directory is not empty`

## What the Fix Does

The changes in `JournalForge.csproj` add:

1. **Directory Pre-Creation**: The `CreateAndroidResourceDirectories` target ensures that required directories (`res` and `lp`) exist before Android build tasks try to access them.

2. **Clean Error Handling**: The `CleanAndroidDirectories` target handles directory cleanup issues by continuing on error, preventing build failures when directories are locked or in use.

3. **Resource File Configuration**: The `AndroidResgenFile` property ensures proper resource designer file generation.

## Expected Behavior

### Before the Fix
- Build fails with `DirectoryNotFoundException` when `GenerateRtxt` task tries to enumerate the `res` directory
- Clean operation fails with `IOException` when trying to delete non-empty directories

### After the Fix
- Required directories are created automatically before build tasks need them
- Clean operation handles errors gracefully
- Build completes successfully for Android target

## Additional Notes

- These changes only affect the Android target (`net9.0-android`)
- Other targets (iOS, Windows, MacCatalyst) are not affected
- The fix is minimal and follows MSBuild best practices for handling intermediate build directories
