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
        
        // Add initial AI greeting
        AddAIMessage("Welcome, Chronicler! I'm here to help you explore your thoughts. What's on your mind today?");
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

    public void ResetForNewEntry()
    {
        // Reset to new entry state
        _currentEntry = new JournalEntry();
        Title = "New Journal Entry";
        EntryTitle = string.Empty;
        CurrentMessage = string.Empty;
        IsRecording = false;
        RecordingStatus = string.Empty;
        IsViewMode = false;
        
        // Clear and re-add initial AI greeting
        ConversationMessages.Clear();
        AddAIMessage("Welcome, Chronicler! I'm here to help you explore your thoughts. What's on your mind today?");
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
            
            await Application.Current?.MainPage?.DisplayAlert(
                "Success", 
                "Your entry has been saved to the chronicles!", 
                "OK")!;
            
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
