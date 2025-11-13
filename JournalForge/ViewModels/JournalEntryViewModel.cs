using System.Collections.ObjectModel;
using System.Windows.Input;
using JournalForge.Models;
using JournalForge.Services;

namespace JournalForge.ViewModels;

public class JournalEntryViewModel : BaseViewModel
{
    private readonly IAIService _aiService;
    private readonly IJournalEntryService _journalService;
    private readonly ISpeechToTextService _speechToTextService;
    private JournalEntry _currentEntry;
    private ObservableCollection<ConversationMessage> _conversationMessages = new();
    private string _currentMessage = string.Empty;
    private bool _isRecording;
    private string _recordingStatus = string.Empty;
    private bool _isViewMode;

    public JournalEntryViewModel(
        IAIService aiService,
        IJournalEntryService journalService,
        ISpeechToTextService speechToTextService)
    {
        _aiService = aiService;
        _journalService = journalService;
        _speechToTextService = speechToTextService;
        _currentEntry = new JournalEntry();

        Title = "New Journal Entry";

        SaveCommand = new Command(async () => await SaveEntryAsync());
        SendMessageCommand = new Command(async () => await SendMessageAsync(), () => !string.IsNullOrWhiteSpace(CurrentMessage));
        RequestAIQuestionCommand = new Command(async () => await RequestAIQuestionAsync());
        StartRecordingCommand = new Command(async () => await StartRecordingAsync());
        StopRecordingCommand = new Command(StopRecording);
        
        // Initial greeting will be set when page appears via LoadInitialGreeting
    }
    
    public async Task LoadInitialGreetingAsync()
    {
        try
        {
            // Get recent entries to provide context
            var recentEntries = await _journalService.GetRecentEntriesAsync(3);
            var greeting = await GeneratePersonalizedGreetingAsync(recentEntries);
            AddAIMessage(greeting);
        }
        catch
        {
            // Fallback to default greeting if something goes wrong
            AddAIMessage("Welcome, Chronicler! I'm here to help you explore your thoughts. What's on your mind today?");
        }
    }
    
    private async Task<string> GeneratePersonalizedGreetingAsync(List<JournalEntry> recentEntries)
    {
        if (recentEntries.Count == 0)
        {
            // No previous entries, use welcoming greetings
            var firstTimeGreetings = new[]
            {
                "Welcome, brave Chronicler! 📜 I'm here to help you explore your thoughts and feelings. What would you like to talk about today?",
                "Greetings, adventurer! ⚔️ Let's embark on a journey of self-discovery together. What's on your mind?",
                "Welcome to your journal! 🔮 I'm here to listen and help you dive deeper into your experiences. Where shall we begin?"
            };
            return firstTimeGreetings[new Random().Next(firstTimeGreetings.Length)];
        }

        // Get the most recent entry date
        var lastEntryDate = recentEntries[0].CreatedDate;
        var daysSinceLastEntry = (DateTime.Now - lastEntryDate).Days;
        
        if (daysSinceLastEntry == 0)
        {
            // Same day - acknowledge their dedication
            var sameDayGreetings = new[]
            {
                "Back already? I love your dedication! 📖 What else is on your mind today?",
                "Welcome back, Chronicler! What new thoughts have emerged since we last spoke?",
                "Great to see you again today! 🌟 Ready to explore more of what's happening in your world?"
            };
            return sameDayGreetings[new Random().Next(sameDayGreetings.Length)];
        }
        else if (daysSinceLastEntry == 1)
        {
            // Yesterday - warm welcome back
            var nextDayGreetings = new[]
            {
                "Welcome back! It's great to continue our journey together. What's new today?",
                "Good to see you again! 🌅 How has your day been unfolding?",
                "Hey there! Ready to reflect on another day's adventures?"
            };
            return nextDayGreetings[new Random().Next(nextDayGreetings.Length)];
        }
        else if (daysSinceLastEntry <= 7)
        {
            // This week - acknowledge the gap
            var thisWeekGreetings = new[]
            {
                $"Welcome back! It's been {daysSinceLastEntry} days. I'm curious to hear what's been happening! 📜",
                $"Great to see you again after {daysSinceLastEntry} days! What's been on your mind lately?",
                $"Welcome, Chronicler! What stories from the past {daysSinceLastEntry} days would you like to share?"
            };
            return thisWeekGreetings[new Random().Next(thisWeekGreetings.Length)];
        }
        else
        {
            // Longer absence - warm welcome
            var longAbsenceGreetings = new[]
            {
                "Welcome back, friend! It's been a while. I'm here whenever you need to reflect. What's on your mind?",
                "So good to see you again! 🌟 No judgment here - let's pick up where we left off. What would you like to talk about?",
                "Welcome back to your journal! I've missed our conversations. What's been happening in your world?"
            };
            return longAbsenceGreetings[new Random().Next(longAbsenceGreetings.Length)];
        }
    }

    public async Task LoadEntryAsync(string entryId)
    {
        var entry = await _journalService.GetEntryByIdAsync(entryId);
        if (entry != null)
        {
            _currentEntry = entry;
            Title = "View Entry";
            EntryTitle = entry.Title;
            IsViewMode = true;
            
            // Load conversation messages
            ConversationMessages.Clear();
            foreach (var message in entry.ConversationMessages)
            {
                ConversationMessages.Add(message);
            }
        }
    }

    public async Task ResetForNewEntryAsync()
    {
        // Reset to new entry state
        _currentEntry = new JournalEntry();
        Title = "New Journal Entry";
        EntryTitle = string.Empty;
        CurrentMessage = string.Empty;
        IsRecording = false;
        RecordingStatus = string.Empty;
        IsViewMode = false;
        
        // Clear and load personalized AI greeting
        ConversationMessages.Clear();
        await LoadInitialGreetingAsync();
    }

    public string EntryTitle
    {
        get => _currentEntry.Title;
        set
        {
            _currentEntry.Title = value;
            OnPropertyChanged();
        }
    }

    public ObservableCollection<ConversationMessage> ConversationMessages
    {
        get => _conversationMessages;
        set => SetProperty(ref _conversationMessages, value);
    }

    public string CurrentMessage
    {
        get => _currentMessage;
        set
        {
            SetProperty(ref _currentMessage, value);
            ((Command)SendMessageCommand).ChangeCanExecute();
        }
    }

    public bool IsRecording
    {
        get => _isRecording;
        set => SetProperty(ref _isRecording, value);
    }

    public string RecordingStatus
    {
        get => _recordingStatus;
        set => SetProperty(ref _recordingStatus, value);
    }

    public bool IsViewMode
    {
        get => _isViewMode;
        set => SetProperty(ref _isViewMode, value);
    }

    public ICommand SaveCommand { get; }
    public ICommand SendMessageCommand { get; }
    public ICommand RequestAIQuestionCommand { get; }
    public ICommand StartRecordingCommand { get; }
    public ICommand StopRecordingCommand { get; }

    private async Task SendMessageAsync()
    {
        if (string.IsNullOrWhiteSpace(CurrentMessage))
            return;

        // Add user message to conversation
        var userMessage = new ConversationMessage
        {
            Sender = "User",
            Content = CurrentMessage.Trim(),
            Timestamp = DateTime.Now
        };
        ConversationMessages.Add(userMessage);
        _currentEntry.ConversationMessages.Add(userMessage);

        // Build content from all user messages for context
        var allUserContent = string.Join(" ", 
            _currentEntry.ConversationMessages
                .Where(m => m.Sender == "User")
                .Select(m => m.Content));

        // Clear input
        var messageText = CurrentMessage;
        CurrentMessage = string.Empty;

        try
        {
            IsBusy = true;

            // Get AI response based on the conversation
            var aiResponse = await _aiService.GenerateProbingQuestionAsync(allUserContent);
            AddAIMessage(aiResponse);
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert("Error", ex.Message, "OK")!;
        }
        finally
        {
            IsBusy = false;
        }
    }

    private async Task RequestAIQuestionAsync()
    {
        // Build content from all user messages
        var allUserContent = string.Join(" ", 
            _currentEntry.ConversationMessages
                .Where(m => m.Sender == "User")
                .Select(m => m.Content));

        if (string.IsNullOrWhiteSpace(allUserContent))
        {
            AddAIMessage("Start sharing your thoughts, and I'll help you explore them deeper!");
            return;
        }

        try
        {
            IsBusy = true;
            var question = await _aiService.GenerateProbingQuestionAsync(allUserContent);
            AddAIMessage(question);
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert("Error", ex.Message, "OK")!;
        }
        finally
        {
            IsBusy = false;
        }
    }

    private void AddAIMessage(string content)
    {
        var aiMessage = new ConversationMessage
        {
            Sender = "AI",
            Content = content,
            Timestamp = DateTime.Now
        };
        ConversationMessages.Add(aiMessage);
        _currentEntry.ConversationMessages.Add(aiMessage);
    }

    private async Task SaveEntryAsync()
    {
        if (_currentEntry.ConversationMessages.Count == 0 || 
            !_currentEntry.ConversationMessages.Any(m => m.Sender == "User"))
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Validation", 
                "Please write something before saving.", 
                "OK")!;
            return;
        }

        try
        {
            IsBusy = true;

            // Build content from all user messages
            _currentEntry.Content = string.Join("\n\n", 
                _currentEntry.ConversationMessages
                    .Where(m => m.Sender == "User")
                    .Select(m => m.Content));

            // If no title, generate one from first message
            if (string.IsNullOrWhiteSpace(_currentEntry.Title))
            {
                var firstUserMessage = _currentEntry.ConversationMessages
                    .FirstOrDefault(m => m.Sender == "User")?.Content ?? "Untitled Entry";
                
                // Use first 50 characters or up to first sentence
                var title = firstUserMessage.Length > 50 
                    ? firstUserMessage.Substring(0, 47) + "..." 
                    : firstUserMessage;
                
                // Trim at sentence if there's a period before the cut
                var periodIndex = title.IndexOf('.');
                if (periodIndex > 0 && periodIndex < title.Length - 3)
                {
                    title = title.Substring(0, periodIndex);
                }
                
                _currentEntry.Title = title;
            }

            await _journalService.SaveEntryAsync(_currentEntry);
            
            // Generate and display immediate insights before navigating away
            await ShowEntryInsightsAsync();
            
            await Shell.Current.GoToAsync("..");
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert("Error", ex.Message, "OK")!;
        }
        finally
        {
            IsBusy = false;
        }
    }
    
    private async Task ShowEntryInsightsAsync()
    {
        try
        {
            // Generate insights based on this entry
            var insights = await GenerateEntryInsightsAsync(_currentEntry);
            
            var insightsMessage = "✨ Entry Saved! ✨\n\n" + string.Join("\n\n", insights);
            
            await Application.Current?.MainPage?.DisplayAlert(
                "Reflection Complete", 
                insightsMessage, 
                "Continue")!;
        }
        catch
        {
            // If insights generation fails, just show simple success message
            await Application.Current?.MainPage?.DisplayAlert(
                "Success", 
                "Your entry has been saved to the chronicles! 📜", 
                "OK")!;
        }
    }
    
    private async Task<List<string>> GenerateEntryInsightsAsync(JournalEntry entry)
    {
        var insights = new List<string>();
        
        // Word count insight
        var wordCount = entry.Content.Split(new[] { ' ', '\n', '\r' }, StringSplitOptions.RemoveEmptyEntries).Length;
        insights.Add($"📝 You wrote {wordCount} words in this reflection.");
        
        // Conversation depth insight
        var userMessageCount = entry.ConversationMessages.Count(m => m.Sender == "User");
        if (userMessageCount > 3)
        {
            insights.Add($"💭 Great job exploring your thoughts through {userMessageCount} exchanges!");
        }
        else if (userMessageCount > 1)
        {
            insights.Add($"🌱 You shared {userMessageCount} thoughts - nice reflection!");
        }
        
        // Time spent insight
        if (entry.ConversationMessages.Count > 0)
        {
            var firstMessage = entry.ConversationMessages[0].Timestamp;
            var lastMessage = entry.ConversationMessages[^1].Timestamp;
            var duration = (lastMessage - firstMessage).TotalMinutes;
            
            if (duration >= 5)
            {
                insights.Add($"⏰ You spent {(int)duration} minutes on this entry - dedicated reflection!");
            }
        }
        
        // Content-based insight
        var content = entry.Content.ToLower();
        if (content.Contains("grateful") || content.Contains("thankful") || content.Contains("appreciate"))
        {
            insights.Add("🙏 I noticed gratitude in your words - powerful practice!");
        }
        else if (content.Contains("challenge") || content.Contains("difficult") || content.Contains("struggle"))
        {
            insights.Add("💪 Acknowledging challenges is the first step to overcoming them.");
        }
        else if (content.Contains("happy") || content.Contains("joy") || content.Contains("excited"))
        {
            insights.Add("😊 It's wonderful to capture these positive moments!");
        }
        else if (content.Contains("learn") || content.Contains("realize") || content.Contains("understand"))
        {
            insights.Add("🎓 Self-awareness through learning - that's growth!");
        }
        
        // Fallback insight if we only have basic ones
        if (insights.Count < 3)
        {
            insights.Add("🔮 Continue your journaling journey - each entry adds to your story!");
        }
        
        return insights;
    }

    private async Task StartRecordingAsync()
    {
        try
        {
            // Request permissions
            var hasPermission = await _speechToTextService.RequestPermissionsAsync();
            if (!hasPermission)
            {
                RecordingStatus = "Microphone permission is required for speech-to-text.";
                await Task.Delay(3000);
                RecordingStatus = string.Empty;
                return;
            }

            IsRecording = true;
            RecordingStatus = "Listening... Speak now";
            
            // Start listening
            var transcribedText = await _speechToTextService.ListenAsync();
            
            // Add transcribed text to current message
            if (!string.IsNullOrWhiteSpace(transcribedText))
            {
                if (string.IsNullOrWhiteSpace(CurrentMessage))
                {
                    CurrentMessage = transcribedText;
                }
                else
                {
                    CurrentMessage += " " + transcribedText;
                }
                RecordingStatus = $"✅ Text added: \"{transcribedText.Substring(0, Math.Min(50, transcribedText.Length))}{(transcribedText.Length > 50 ? "..." : "")}\" - Tap send when ready!";
            }
            else
            {
                RecordingStatus = "❌ No speech detected. Please try again.";
            }
            
            IsRecording = false;
            
            // Clear status after a longer delay to give user time to see the message
            await Task.Delay(5000);
            RecordingStatus = string.Empty;
        }
        catch (PlatformNotSupportedException)
        {
            IsRecording = false;
            RecordingStatus = "Speech-to-text is not supported on this platform.";
            await Task.Delay(3000);
            RecordingStatus = string.Empty;
        }
        catch (Exception ex)
        {
            IsRecording = false;
            RecordingStatus = $"Error: {ex.Message}";
            await Task.Delay(3000);
            RecordingStatus = string.Empty;
        }
    }

    private void StopRecording()
    {
        _speechToTextService.StopListening();
        IsRecording = false;
        RecordingStatus = "Recording stopped.";
        
        // Clear status after a delay
        Task.Run(async () =>
        {
            await Task.Delay(2000);
            RecordingStatus = string.Empty;
        });
    }
}
