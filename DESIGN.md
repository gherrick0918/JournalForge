# JournalForge Design Document

## Application Overview

JournalForge is a cross-platform mobile journaling application with AI assistance and an old-school RPG visual theme. This document outlines the design decisions, architecture, and visual style.

## Design Philosophy

### Core Principles

1. **Engaging Experience**: Make journaling feel like an adventure
2. **AI-Assisted Reflection**: Help users explore their thoughts deeper
3. **Privacy-First**: Keep personal journals secure and private
4. **Nostalgic Aesthetics**: Evoke feelings of classic RPG games
5. **Simple Yet Powerful**: Easy to use, rich in features

## Visual Design

### Theme: Old-School RPG

Inspired by classic RPG games like Final Fantasy, Dragon Quest, and early Dungeons & Dragons interfaces.

#### Color Palette

```
Primary Colors:
├── RPG Gold       #D4AF37 (Buttons, accents, treasure)
├── Dark Gold      #8B7000 (Borders, shadows)
└── Light Gold     #FFD700 (Highlights, success)

Background Colors:
├── Parchment      #F4E4C1 (Paper-like backgrounds)
├── Background     #E8DCC4 (Page backgrounds)
└── Card BG        #F5F0E8 (Card surfaces)

Text Colors:
├── Dark Brown     #3E2723 (Primary text)
├── Brown          #5D4037 (Secondary text)
└── Light Text     #F4E4C1 (On dark backgrounds)

Accent Colors:
├── Stone Gray     #78909C (Secondary elements)
├── RPG Red        #8B0000 (Warnings, important)
├── RPG Blue       #1E3A5F (Info, AI elements)
├── RPG Green      #2E7D32 (Success, growth)
└── RPG Purple     #4A148C (Special, magical)
```

#### Typography

```
Headers (Large):   OpenSans Semibold, 24pt
Headers (Medium):  OpenSans Semibold, 20pt
Headers (Small):   OpenSans Semibold, 16pt
Body Text:         OpenSans Regular, 14pt
Captions:          OpenSans Regular, 12pt

Future: Add Press Start 2P for true retro feel
```

#### UI Elements

**Buttons**
- Gold background for primary actions
- Stone gray for secondary actions
- 2px borders for that classic look
- 5px rounded corners (subtle)

**Cards/Frames**
- Parchment-colored backgrounds
- Dark brown borders
- Drop shadows for depth
- 10px rounded corners

**Inputs**
- Parchment backgrounds (like writing on paper)
- Dark brown text
- Placeholder text in lighter brown

### Navigation Structure

```
AppShell (Flyout Menu)
├── Home/Dashboard
│   ├── Daily Prompt
│   ├── Daily Insights
│   └── Recent Entries
├── New Entry
│   ├── Title Input
│   ├── Voice Dictation
│   ├── Content Editor
│   ├── AI Assistant
│   └── Save Button
└── Time Capsule
    ├── Capsule List
    ├── Create New Form
    └── Unseal Interface
```

## Architecture

### MVVM Pattern

```
┌─────────────────────────────────────────────────┐
│                    View (XAML)                   │
│  - MainPage                                      │
│  - JournalEntryPage                              │
│  - TimeCapsulePage                               │
└────────────┬────────────────────────────────────┘
             │ Data Binding
             ↓
┌─────────────────────────────────────────────────┐
│                  ViewModel                       │
│  - MainViewModel                                 │
│  - JournalEntryViewModel                         │
│  - TimeCapsuleViewModel                          │
│  - BaseViewModel (INotifyPropertyChanged)        │
└────────────┬────────────────────────────────────┘
             │ Commands & Business Logic
             ↓
┌─────────────────────────────────────────────────┐
│                   Services                       │
│  - IAIService / AIService                        │
│  - IJournalEntryService / JournalEntryService   │
│  - ITimeCapsuleService / TimeCapsuleService     │
└────────────┬────────────────────────────────────┘
             │ Data Operations
             ↓
┌─────────────────────────────────────────────────┐
│                    Models                        │
│  - JournalEntry                                  │
│  - TimeCapsule                                   │
│  - AIPrompt                                      │
└─────────────────────────────────────────────────┘
```

### Dependency Injection

Configured in `MauiProgram.cs`:

```csharp
// Services (Singleton - one instance for app lifetime)
builder.Services.AddSingleton<IAIService, AIService>();
builder.Services.AddSingleton<ITimeCapsuleService, TimeCapsuleService>();
builder.Services.AddSingleton<IJournalEntryService, JournalEntryService>();

// ViewModels (Transient - new instance each time)
builder.Services.AddTransient<MainViewModel>();
builder.Services.AddTransient<JournalEntryViewModel>();
builder.Services.AddTransient<TimeCapsuleViewModel>();

// Pages (Transient - new instance each time)
builder.Services.AddTransient<MainPage>();
builder.Services.AddTransient<JournalEntryPage>();
builder.Services.AddTransient<TimeCapsulePage>();
```

## Data Models

### JournalEntry
```csharp
public class JournalEntry
{
    string Id;                      // Unique identifier
    DateTime CreatedDate;           // When entry was created
    string Title;                   // Entry title
    string Content;                 // Main entry text
    List<string> Tags;              // Future: categorization
    string Mood;                    // Future: mood tracking
    List<string> AIConversation;    // AI interaction history
    string VoiceRecordingPath;      // Future: audio file path
    bool IsTimeCapsule;             // Is this a time capsule?
    DateTime? UnsealDate;           // When to unseal (if capsule)
}
```

### TimeCapsule
```csharp
public class TimeCapsule
{
    string Id;                      // Unique identifier
    string EntryId;                 // Linked journal entry
    DateTime SealedDate;            // When capsule was sealed
    DateTime UnsealDate;            // When to unseal
    string Title;                   // Capsule title
    string PreviewText;             // Short preview
    bool IsUnsealed;                // Has it been opened?
    string Message;                 // Message to future self
}
```

### AIPrompt
```csharp
public class AIPrompt
{
    string Id;                      // Unique identifier
    DateTime Date;                  // Prompt date
    string PromptText;              // The prompt
    string Category;                // Type of prompt
    bool IsUsed;                    // Has user responded?
}
```

## User Flows

### Creating a Journal Entry

```
1. User taps "New Entry" button
   ↓
2. Navigate to JournalEntryPage
   ↓
3. User enters title and content
   ↓
4. (Optional) User taps "Probe Deeper"
   ↓
5. AI generates question based on content
   ↓
6. User continues writing
   ↓
7. (Optional) User taps "Suggest Ending"
   ↓
8. AI suggests thoughtful conclusion
   ↓
9. User taps "Save Entry"
   ↓
10. Validation checks content exists
    ↓
11. Entry saved via JournalEntryService
    ↓
12. Success message displayed
    ↓
13. Navigate back to MainPage
```

### Creating a Time Capsule

```
1. User taps "Time Capsule" button
   ↓
2. Navigate to TimeCapsulePage
   ↓
3. User taps "Seal New Capsule"
   ↓
4. Form appears
   ↓
5. User enters title and message
   ↓
6. User selects unseal date
   ↓
7. User taps "Seal It!"
   ↓
8. Validation checks all fields
   ↓
9. Capsule saved via TimeCapsuleService
   ↓
10. Success message with unseal date
    ↓
11. Form closes
    ↓
12. Capsule appears in list
```

### Unsealing a Time Capsule

```
1. User views TimeCapsulePage
   ↓
2. Capsule list shows sealed capsules
   ↓
3. User taps "Open" on a capsule
   ↓
4. System checks if unseal date has passed
   ↓
5a. If too early:
    - Show days remaining message
    ↓
5b. If ready:
    - Mark capsule as unsealed
    - Display full message in alert
    - Update list to show unsealed status
```

## AI Integration Design

### Current Implementation (Mock)

```
AIService (Mock)
├── Daily Prompts (10 variations)
├── Probing Questions (10 variations)
├── Entry Endings (5 variations)
└── Insights (4 variations)

Selection: Random from predefined lists
```

### Future Implementation (Real AI)

```
AIService (Real)
├── OpenAI Integration
│   ├── GPT-4 for conversations
│   ├── Whisper for voice-to-text
│   └── Embeddings for semantic search
├── Prompt Engineering
│   ├── System prompts for journaling context
│   ├── User context from past entries
│   └── Conversation memory
└── Local Processing
    ├── Sentiment analysis
    ├── Topic extraction
    └── Keyword identification
```

## Platform Considerations

### Android (Primary Focus)
- Material Design influences (cards, FABs)
- Navigation drawer (flyout)
- System back button support
- Share functionality
- Notifications for capsule unsealing

### iOS (Secondary)
- Native navigation patterns
- Swipe gestures
- iOS share sheet
- Haptic feedback
- Push notifications

### Windows (Tertiary)
- Desktop layout adjustments
- Keyboard shortcuts
- Window resizing support
- File system integration

## Performance Considerations

### Optimization Strategies

1. **Lazy Loading**
   - Load entries on demand
   - Virtual scrolling for long lists

2. **Caching**
   - Cache recent entries in memory
   - Cache AI responses

3. **Async Operations**
   - All I/O operations async
   - Non-blocking UI updates

4. **Memory Management**
   - Dispose of resources properly
   - Weak references for large objects

## Accessibility

### Current Support
- High contrast colors
- Large touch targets (minimum 44x44 pts)
- Clear focus indicators

### Planned Support
- Screen reader optimization
- Dynamic text sizing
- Voice control
- Reduced motion options
- Color blind friendly modes

## Security & Privacy

### Data Protection
- Local-only storage (no cloud by default)
- Future: Encryption at rest
- Future: Secure backup encryption

### User Control
- No tracking or analytics
- No required account
- Full data export capability
- Easy data deletion

## Testing Strategy

### Unit Tests
- ViewModels: Command logic, property changes
- Services: Business logic, data operations
- Converters: Value conversion accuracy

### Integration Tests
- Service interactions
- Navigation flows
- Data persistence

### UI Tests
- Critical user flows
- Cross-platform consistency
- Accessibility compliance

### Manual Testing
- User experience flows
- Visual design verification
- Performance on real devices

## Development Roadmap

### Phase 1: Foundation (Current)
- ✅ MVVM architecture
- ✅ Basic navigation
- ✅ RPG visual theme
- ✅ Mock AI service
- ✅ In-memory data

### Phase 2: Persistence
- ⏳ SQLite database
- ⏳ CRUD operations
- ⏳ Data migrations
- ⏳ Local backups

### Phase 3: Voice Features
- ⏳ Audio recording
- ⏳ Speech-to-text
- ⏳ Audio playback
- ⏳ Transcription storage

### Phase 4: Real AI
- ⏳ OpenAI integration
- ⏳ Context-aware responses
- ⏳ Conversation memory
- ⏳ Sentiment analysis

### Phase 5: Polish
- ⏳ Animations
- ⏳ Sound effects
- ⏳ Advanced themes
- ⏳ Widgets

### Phase 6: Cloud (Optional)
- ⏳ User accounts
- ⏳ Cloud sync
- ⏳ Multi-device
- ⏳ Backup/restore

---

Last Updated: 2025-11-12
Version: 1.0.0
