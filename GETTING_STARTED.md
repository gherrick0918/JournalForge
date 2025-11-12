# Getting Started with JournalForge

This guide will help you set up and run the JournalForge MAUI application.

## Prerequisites

### Required Software

1. **.NET 9.0 SDK** (or later)
   - Download from: https://dotnet.microsoft.com/download
   - Verify installation: `dotnet --version`

2. **.NET MAUI Workload**
   ```bash
   dotnet workload install maui
   ```

3. **IDE (Choose one)**
   - Visual Studio 2022 (17.8 or later) with MAUI workload
   - Visual Studio Code with C# Dev Kit extension
   - JetBrains Rider (2023.3 or later)

### Platform-Specific Requirements

#### For Android Development
- Android SDK (API 21 or higher)
- Android Emulator or physical Android device
- Java Development Kit (JDK) 11 or later

#### For iOS Development (macOS only)
- Xcode 15 or later
- iOS Simulator or physical iOS device
- Apple Developer account (for device testing)

#### For Windows Development
- Windows 10 version 1809 or higher
- Windows App SDK

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/gherrick0918/JournalForge.git
cd JournalForge
```

### 2. Install Dependencies

```bash
# Restore NuGet packages
dotnet restore
```

### 3. Add Required Fonts (Optional)

The app uses OpenSans fonts. To include them:

1. Download OpenSans fonts from [Google Fonts](https://fonts.google.com/specimen/Open+Sans)
2. Place the following files in `JournalForge/Resources/Fonts/`:
   - `OpenSans-Regular.ttf`
   - `OpenSans-Semibold.ttf`

If fonts are not added, the app will use system default fonts.

### 4. Verify Setup

Check that MAUI is properly installed:

```bash
dotnet workload list
```

You should see `maui` in the installed workloads list.

## Running the Application

### Using .NET CLI

#### Android
```bash
# Build for Android
dotnet build -t:Run -f net9.0-android

# Or specify a device
dotnet build -t:Run -f net9.0-android -p:AndroidEmulator="pixel_5"
```

#### iOS (macOS only)
```bash
# Build for iOS simulator
dotnet build -t:Run -f net9.0-ios

# Build for iOS device
dotnet build -t:Run -f net9.0-ios -p:RuntimeIdentifier=ios-arm64
```

#### Windows
```bash
# Build for Windows
dotnet build -t:Run -f net9.0-windows10.0.19041.0
```

### Using Visual Studio 2022

1. Open `JournalForge.sln`
2. Select target framework from dropdown (Android, iOS, Windows)
3. Select device/emulator
4. Press F5 or click "Start Debugging"

### Using Visual Studio Code

1. Open the project folder
2. Install recommended extensions (C# Dev Kit)
3. Open Command Palette (Ctrl+Shift+P / Cmd+Shift+P)
4. Run "MAUI: Pick Android Device"
5. Press F5 to start debugging

## Project Structure Overview

```
JournalForge/
├── Models/              # Data models for the application
├── Services/            # Business logic and data services
├── ViewModels/          # MVVM view models
├── Pages/               # XAML UI pages
├── Converters/          # Data binding converters
└── Resources/           # App resources (styles, images, fonts)
```

## Key Features to Explore

1. **Home Page**: View daily prompts and recent entries
2. **Journal Entry**: Create new entries with AI assistance
3. **Time Capsule**: Seal entries for future opening
4. **Voice Dictation**: Placeholder UI for voice recording
5. **RPG Theme**: Fantasy-themed UI with medieval aesthetics

## Troubleshooting

### Build Errors

#### "MAUI workload not installed"
```bash
dotnet workload install maui
```

#### "Android SDK not found"
1. Install Android SDK through Android Studio or Visual Studio
2. Set `ANDROID_HOME` environment variable
3. Add Android SDK tools to PATH

#### "iOS build failed" (macOS)
1. Open Xcode and accept license agreements
2. Install Xcode command line tools:
   ```bash
   xcode-select --install
   ```

### Runtime Errors

#### "Font not found"
- This is expected if you haven't added custom fonts
- The app will use system default fonts

#### "Service not registered"
- Ensure all services are registered in `MauiProgram.cs`
- Check for typos in service/viewmodel names

### Common Issues

#### Hot Reload not working
```bash
# Clean and rebuild
dotnet clean
dotnet build
```

#### Android emulator slow
- Enable hardware acceleration in BIOS
- Use x86 emulator images on x86 machines
- Increase emulator RAM allocation

## Development Tips

### Debugging

1. **Debug Output**: Check the Debug Output window for logs
2. **Breakpoints**: Set breakpoints in ViewModels and Services
3. **XAML Live Preview**: Use Hot Reload for UI changes

### Testing on Physical Devices

#### Android
1. Enable Developer Options on your device
2. Enable USB Debugging
3. Connect device via USB
4. Trust the computer on the device

#### iOS (macOS only)
1. Connect device via USB
2. Trust the computer on the device
3. Sign the app with your Apple Developer account

## Next Steps

1. **Explore the Code**: Review Models, Services, and ViewModels
2. **Customize the Theme**: Modify colors in `Resources/Styles/Colors.xaml`
3. **Add Features**: Extend services with database persistence
4. **Integrate AI**: Connect to OpenAI or other AI services

## Resources

- [.NET MAUI Documentation](https://docs.microsoft.com/dotnet/maui/)
- [MAUI Community Toolkit](https://github.com/CommunityToolkit/Maui)
- [MVVM Pattern](https://docs.microsoft.com/xamarin/xamarin-forms/enterprise-application-patterns/mvvm)

## Support

For issues or questions:
1. Check the [README.md](README.md) for architecture details
2. Review the code comments
3. Open an issue on GitHub

---

Happy journaling! ⚔️📜
