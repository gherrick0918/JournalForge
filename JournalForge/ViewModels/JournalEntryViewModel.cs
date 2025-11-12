using System.Collections.ObjectModel;
using System.Windows.Input;
using JournalForge.Models;
using JournalForge.Services;

namespace JournalForge.ViewModels;

public class JournalEntryViewModel : BaseViewModel
{
    private readonly IAIService _aiService;
    private readonly IJournalEntryService _journalService;
    private JournalEntry _currentEntry;
    private string _aiQuestion = string.Empty;
    private ObservableCollection<string> _conversation = new();
    private bool _isRecording;

    public JournalEntryViewModel(
        IAIService aiService,
        IJournalEntryService journalService)
    {
        _aiService = aiService;
        _journalService = journalService;
        _currentEntry = new JournalEntry();

        Title = "New Journal Entry";

        SaveCommand = new Command(async () => await SaveEntryAsync());
        GetAIQuestionCommand = new Command(async () => await GetAIQuestionAsync());
        SuggestEndingCommand = new Command(async () => await SuggestEndingAsync());
        StartRecordingCommand = new Command(StartRecording);
        StopRecordingCommand = new Command(StopRecording);
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

    public string EntryContent
    {
        get => _currentEntry.Content;
        set
        {
            _currentEntry.Content = value;
            OnPropertyChanged();
        }
    }

    public string AIQuestion
    {
        get => _aiQuestion;
        set => SetProperty(ref _aiQuestion, value);
    }

    public ObservableCollection<string> Conversation
    {
        get => _conversation;
        set => SetProperty(ref _conversation, value);
    }

    public bool IsRecording
    {
        get => _isRecording;
        set => SetProperty(ref _isRecording, value);
    }

    public ICommand SaveCommand { get; }
    public ICommand GetAIQuestionCommand { get; }
    public ICommand SuggestEndingCommand { get; }
    public ICommand StartRecordingCommand { get; }
    public ICommand StopRecordingCommand { get; }

    private async Task SaveEntryAsync()
    {
        if (string.IsNullOrWhiteSpace(EntryContent))
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

    private async Task GetAIQuestionAsync()
    {
        if (string.IsNullOrWhiteSpace(EntryContent))
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Info", 
                "Start writing first, and I'll help you explore deeper!", 
                "OK")!;
            return;
        }

        try
        {
            IsBusy = true;
            var question = await _aiService.GenerateProbingQuestionAsync(EntryContent);
            AIQuestion = question;
            Conversation.Add($"🤔 AI: {question}");
            _currentEntry.AIConversation.Add(question);
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

    private async Task SuggestEndingAsync()
    {
        try
        {
            IsBusy = true;
            var ending = await _aiService.SuggestEntryEndingAsync(EntryContent);
            
            await Application.Current?.MainPage?.DisplayAlert(
                "Suggested Ending", 
                ending, 
                "OK")!;
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

    private void StartRecording()
    {
        IsRecording = true;
        // In a real implementation, this would start voice recording
        // For now, it's a placeholder
    }

    private void StopRecording()
    {
        IsRecording = false;
        // In a real implementation, this would stop voice recording
        // and convert speech to text
    }
}
