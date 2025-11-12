# JournalForge - AI Journaling MAUI App

## Overview
JournalForge is a cross-platform (Android-focused) mobile application built with .NET MAUI that provides an AI-powered journaling experience with an old-school RPG visual theme.

## Features

### ✍️ Journal Entries
- Create and save journal entries with titles and content
- Voice dictation support (placeholder for recording functionality)
- AI-powered probing questions to help you explore your thoughts deeper
- AI suggestions for entry endings
- Conversation history with AI to guide reflection

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
│   │   └── TimeCapsuleService.cs
│   ├── ViewModels/          # MVVM ViewModels
│   │   ├── BaseViewModel.cs
│   │   ├── MainViewModel.cs
│   │   ├── JournalEntryViewModel.cs
│   │   └── TimeCapsuleViewModel.cs
│   ├── Pages/               # XAML pages
│   │   ├── MainPage.xaml
│   │   ├── JournalEntryPage.xaml
│   │   └── TimeCapsulePage.xaml
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

### Dependency Injection
Services and ViewModels are registered in `MauiProgram.cs` for dependency injection.

## Key Pages

### 1. MainPage (Home/Dashboard)
- Displays daily AI-generated prompt
- Shows daily insights
- Lists recent journal entries
- Quick access buttons to create new entry or view time capsules

### 2. JournalEntryPage
- Text editor for writing journal entries
- Voice dictation controls (start/stop recording)
- AI conversation panel with probing questions
- Suggest ending button
- Save functionality

### 3. TimeCapsulePage
- List of sealed time capsules
- Create new time capsule form
- Unseal functionality for capsules that are ready
- Status indicators (sealed/unsealed)

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

### Planned Features
1. **Voice Integration**: Implement actual speech-to-text for vocal dictation
2. **AI Integration**: Connect to real AI service (OpenAI, Azure AI, etc.)
3. **Data Persistence**: Add local database (SQLite) for storing entries
4. **Export/Backup**: Allow exporting entries to PDF or cloud storage
5. **Tags & Search**: Add tagging system and search functionality
6. **Mood Tracking**: Visual mood tracking over time
7. **Reminders**: Notification system for unsealing time capsules
8. **Themes**: Additional theme options (dark mode, other RPG styles)
9. **Media Attachments**: Support for images and audio recordings
10. **Cloud Sync**: Multi-device synchronization

### Technical Improvements
- Unit tests for ViewModels and Services
- Integration tests for UI flows
- Accessibility improvements
- Performance optimization
- Localization support

## Development Notes

### Voice Recording
The current implementation includes UI for voice recording but uses placeholder methods. To implement:
1. Use platform-specific audio recording APIs
2. Integrate speech-to-text service
3. Store audio files locally
4. Link recordings to journal entries

### AI Service
✅ **OpenAI Integration Complete!** The app now supports real AI with OpenAI's API:
- Set your API key via environment variable: `OPENAI_API_KEY`
- Or configure directly in `MauiProgram.cs`
- Automatically falls back to mock responses if API key is not set
- Uses `gpt-4o-mini` by default for cost-effectiveness
- See [OPENAI_SETUP.md](JournalForge/OPENAI_SETUP.md) for detailed setup instructions

Future enhancements:
1. Add conversation history to API calls for context
2. Support for other AI providers (Azure OpenAI, Anthropic, etc.)
3. Local AI option for privacy-focused users

### Data Persistence
Current services store data in memory. To persist:
1. Add SQLite database
2. Create repository pattern
3. Implement data migration
4. Add backup/restore functionality

## License
[Your License Here]

## Contact
[Your Contact Information]
