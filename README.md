# JournalForge - AI Journaling App

## 🔄 Important: Native Android Migration in Progress

**This project has been migrated from .NET MAUI to native Android** due to slow build times and better Firebase integration. 

- **Legacy MAUI Code**: The original .NET MAUI code is in the root directory (for reference)
- **New Native Android App**: The new native Android app is in the `android-app/` directory
- **Migration Guide**: See [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) for details
- **Firebase Setup**: See [FIREBASE_SIGNIN_COMPLETION.md](FIREBASE_SIGNIN_COMPLETION.md) to complete Google Sign-In

## Overview
JournalForge is an Android mobile application that provides an AI-powered journaling experience with an old-school RPG visual theme. Originally built with .NET MAUI, it has been migrated to native Android (Kotlin) for improved performance and Firebase integration.

## Features

### ✍️ Journal Entries
- Create and save journal entries with titles and content
- Voice dictation support with speech-to-text ✅
- AI-powered probing questions to help you explore your thoughts deeper
- AI suggestions for entry endings
- Conversation history with AI to guide reflection
- Full entry history with search and filtering
- Export entries to plain text or JSON format

### 📚 Chronicle History
- **New!** Complete history view of all journal entries
- Search functionality to find specific entries
- Sort by date (newest/oldest first)
- Filter entries by search terms
- View, export, or delete individual entries
- Export all entries at once
- Entry count and empty state messaging

### 📤 Export & Backup
- **New!** Export single or multiple entries
- Plain text format - Human-readable, formatted export
- JSON format - Machine-readable, structured export
- Uses MAUI Share API to save to any location
- Includes entry metadata (title, date, mood, tags, AI conversation)
- Bulk export of filtered entries

### ☁️ Cloud Sync (Setup Required)
- **New!** Google Sign-In infrastructure
- Settings page for account management
- Cloud backup preparation
- Sync status tracking
- Framework ready for Firebase or Google Drive integration
- 📘 See [GOOGLE_SIGNIN_SETUP.md](GOOGLE_SIGNIN_SETUP.md) for configuration

### ⏰ Time Capsule System
- Seal journal entries to be opened in the future
- Set custom unseal dates
- Preview sealed capsules
- Automatically track which capsules are ready to be opened
- Reveal messages from your past self

### 🤖 AI Features (OpenAI Powered!)
- **Real OpenAI Integration**: Use your OpenAI API key for truly dynamic, context-aware AI responses
- Daily writing prompts with RPG-themed language
- Context-aware probing questions based on your actual entry content
- Personalized entry ending suggestions
- Smart insights about journaling patterns and themes
- **Fallback Mode**: Works without API key using built-in responses
- 📘 See [OPENAI_SETUP.md](JournalForge/OPENAI_SETUP.md) for configuration instructions

### 🎨 RPG Visual Theme
- Medieval/fantasy color scheme with gold, brown, and parchment tones
- RPG-style UI elements and fonts
- Card-based layout reminiscent of old-school RPG menus
- Thematic navigation with flyout menu

## Project Structure

```
JournalForge/
├── JournalForge/
│   ├── Models/              # Data models
│   │   ├── JournalEntry.cs
│   │   ├── TimeCapsule.cs
│   │   └── AIPrompt.cs
│   ├── Services/            # Business logic and data services
│   │   ├── AIService.cs
│   │   ├── JournalEntryService.cs
│   │   ├── TimeCapsuleService.cs
│   │   ├── ExportService.cs
│   │   ├── GoogleAuthService.cs
│   │   └── CloudSyncService.cs
│   ├── ViewModels/          # MVVM ViewModels
│   │   ├── BaseViewModel.cs
│   │   ├── MainViewModel.cs
│   │   ├── JournalEntryViewModel.cs
│   │   ├── HistoryViewModel.cs
│   │   ├── TimeCapsuleViewModel.cs
│   │   └── SettingsViewModel.cs
│   ├── Pages/               # XAML pages
│   │   ├── MainPage.xaml
│   │   ├── JournalEntryPage.xaml
│   │   ├── HistoryPage.xaml
│   │   ├── TimeCapsulePage.xaml
│   │   └── SettingsPage.xaml
│   ├── Converters/          # Value converters for bindings
│   │   └── CommonConverters.cs
│   ├── Resources/           # App resources
│   │   ├── Styles/
│   │   │   ├── Colors.xaml  # RPG theme colors
│   │   │   └── Styles.xaml  # RPG UI styles
│   │   ├── Fonts/
│   │   ├── Images/
│   │   ├── AppIcon/
│   │   └── Splash/
│   ├── App.xaml             # Application resources
│   ├── AppShell.xaml        # Navigation shell
│   ├── MauiProgram.cs       # App configuration
│   └── JournalForge.csproj  # Project file
└── JournalForge.sln         # Solution file
```

## Architecture

### MVVM Pattern
The app follows the Model-View-ViewModel (MVVM) pattern:
- **Models**: Define data structures for journal entries, time capsules, and AI prompts
- **Views**: XAML pages that define the UI
- **ViewModels**: Handle business logic and data binding

### Services
Service interfaces and implementations provide:
- **AIService**: Generates prompts, questions, and insights
- **JournalEntryService**: Manages journal entries (CRUD operations)
- **TimeCapsuleService**: Handles time capsule sealing and unsealing
- **ExportService**: Exports entries in various formats (text, JSON)
- **GoogleAuthService**: Manages Google Sign-In authentication
- **CloudSyncService**: Handles cloud backup and synchronization

### Dependency Injection
Services and ViewModels are registered in `MauiProgram.cs` for dependency injection.

## Key Pages

### 1. MainPage (Home/Dashboard)
- Displays daily AI-generated prompt
- Shows daily insights
- Lists recent journal entries (last 5)
- Quick access buttons to create new entry, view full history, or manage time capsules

### 2. JournalEntryPage
- Text editor for writing journal entries
- Voice dictation controls (start/stop recording)
- AI conversation panel with probing questions
- Suggest ending button
- Save functionality

### 3. HistoryPage (New!)
- Complete list of all journal entries
- Search bar for filtering entries
- Sort by date (newest/oldest first)
- Individual entry actions: view, export, delete
- Bulk export functionality
- Entry count display

### 4. TimeCapsulePage
- List of sealed time capsules
- Create new time capsule form
- Unseal functionality for capsules that are ready
- Status indicators (sealed/unsealed)

### 5. SettingsPage (New!)
- Google Sign-In integration
- Account management (sign in/sign out)
- Cloud sync controls
- Last sync time display
- App information and version

## RPG Theme

### Color Palette
- **Gold**: `#D4AF37` - Primary accent color
- **Dark Brown**: `#3E2723` - Main text and borders
- **Parchment**: `#F4E4C1` - Input backgrounds
- **Stone Gray**: `#78909C` - Secondary elements

### Typography
- **Headers**: OpenSans Semibold with larger sizes
- **Body**: OpenSans Regular
- **Theme**: Fantasy/medieval style with emoji icons (⚔️, 📜, 🔮, etc.)

## Building the App

### Prerequisites
- .NET 9.0 SDK
- .NET MAUI workload installed: `dotnet workload install maui`
- Android SDK (for Android development)
- Xcode (for iOS development on macOS)

### Build Commands
```bash
# Restore dependencies
dotnet restore

# Build the solution
dotnet build

# Run on Android
dotnet build -t:Run -f net9.0-android

# Run on iOS
dotnet build -t:Run -f net9.0-ios
```

## Future Enhancements

### Completed Features ✅
1. ~~**Voice Integration**: Implement actual speech-to-text for vocal dictation~~ ✅ **COMPLETED**
2. ~~**AI Integration**: Connect to real AI service (OpenAI, Azure AI, etc.)~~ ✅ **COMPLETED**
3. ~~**Export/Backup**: Allow exporting entries~~ ✅ **COMPLETED** - Export to plain text and JSON formats
4. ~~**Full History View**: Complete entry history with search~~ ✅ **COMPLETED** - History page with filtering and search
5. ~~**Cloud Sync Infrastructure**: Google Sign-In and cloud backup~~ ✅ **COMPLETED** - Framework ready for OAuth configuration

### Planned Features
1. **Data Persistence**: Add local database (SQLite) for storing entries
2. **Tags & Search**: Add tagging system and advanced search functionality
3. **Mood Tracking**: Visual mood tracking over time
4. **Reminders**: Notification system for unsealing time capsules
5. **Themes**: Additional theme options (dark mode, other RPG styles)
6. **Media Attachments**: Support for images and audio recordings
7. **Full Cloud Sync**: Complete Google Drive/Firebase integration with OAuth
8. **Enhanced Voice Features**: Offline mode, multi-language support, custom vocabulary
9. **PDF Export**: Export journal entries as formatted PDF documents

### Technical Improvements
- Unit tests for ViewModels and Services
- Integration tests for UI flows
- Accessibility improvements
- Performance optimization
- Localization support

## Development Notes

### Voice Recording
✅ **Speech-to-Text Implemented!** The app now supports voice dictation with multiple recognition methods:
- **Intent-Based Recognition (Primary)**: Uses Google's speech UI for maximum reliability
- **Service-Based Recognition (Fallback)**: Background recognition without UI
- **Auto Selection**: Automatically chooses the best available method
- Works on Android devices with Google services
- Requires microphone permission and internet connection
- 📘 See [TROUBLESHOOTING.md](TROUBLESHOOTING.md) if experiencing issues

Future enhancements:
1. Offline speech recognition
2. Support for additional languages
3. Custom vocabulary for better accuracy
4. Integration with other speech services (Azure, AWS)

### AI Service
✅ **OpenAI Integration Complete!** The app now supports real AI with OpenAI's API:
- **Secure local config**: Create `appsettings.local.json` with your API key
- **Works on Android**: Config file is packaged into APK at build time
- **Git-safe**: Local config is automatically excluded from version control
- Automatically falls back to mock responses if API key is not set
- Uses `gpt-4o-mini` by default for cost-effectiveness
- See [OPENAI_SETUP.md](JournalForge/OPENAI_SETUP.md) for detailed setup instructions

Future enhancements:
1. Add conversation history to API calls for context
2. Support for other AI providers (Azure OpenAI, Anthropic, etc.)
3. Local AI option for privacy-focused users

### Data Persistence
✅ **Local JSON Storage Implemented!** The app now persists data:
- Journal entries are saved to JSON files in app data directory
- Automatic loading on app start
- Thread-safe file operations
- Data survives app restarts

Current storage uses JSON files. Future enhancement with SQLite database will provide:
1. Better performance for large datasets
2. Advanced querying capabilities
3. Relationships between data
4. Efficient indexing for search

### Export Functionality
✅ **Export Feature Implemented!** Export your journal entries:
- **Plain Text Format**: Human-readable, formatted export with metadata
- **JSON Format**: Machine-readable, structured export for backup
- **Single Entry Export**: Export individual entries
- **Bulk Export**: Export all filtered entries at once
- Uses MAUI Share API to save files anywhere (Google Drive, email, etc.)
- Automatic filename generation with sanitization

### Cloud Sync
✅ **Google Sign-In Infrastructure Ready!** Framework implemented for cloud backup:
- **Google Authentication Service**: Interface and structure ready for OAuth
- **Cloud Sync Service**: Framework for uploading/downloading entries
- **Settings UI**: Complete user interface for account management
- **Sync Status Tracking**: Last sync time and status display
- **Ready for Integration**: See [GOOGLE_SIGNIN_SETUP.md](GOOGLE_SIGNIN_SETUP.md) for OAuth configuration

To fully enable cloud sync:
1. Set up Google Cloud project with OAuth credentials
2. Choose Firebase or Google Sign-In SDK
3. Install required NuGet packages
4. Update services with actual OAuth implementation
5. Configure cloud storage (Firestore or Google Drive)

## Troubleshooting

Having issues with speech recognition or other features? Check out our comprehensive [TROUBLESHOOTING.md](TROUBLESHOOTING.md) guide.

Common issues:
- "No speech detected" error → [See troubleshooting guide](TROUBLESHOOTING.md#no-speech-detected-error)
- Microphone permission → [See troubleshooting guide](TROUBLESHOOTING.md#check-microphone-permission)
- Device-specific issues → [See troubleshooting guide](TROUBLESHOOTING.md#device-specific-issues)

## License
[Your License Here]

## Contact
[Your Contact Information]
