using System.Collections.ObjectModel;
using System.Windows.Input;
using JournalForge.Models;
using JournalForge.Services;

namespace JournalForge.ViewModels;

public class MainViewModel : BaseViewModel
{
    private readonly IAIService _aiService;
    private readonly IJournalEntryService _journalService;
    private readonly ITimeCapsuleService _timeCapsuleService;
    private string _dailyPrompt = string.Empty;
    private ObservableCollection<JournalEntry> _recentEntries = new();
    private ObservableCollection<string> _dailyInsights = new();

    public MainViewModel(
        IAIService aiService,
        IJournalEntryService journalService,
        ITimeCapsuleService timeCapsuleService)
    {
        _aiService = aiService;
        _journalService = journalService;
        _timeCapsuleService = timeCapsuleService;

        Title = "JournalForge";
        
        NewEntryCommand = new Command(async () => await NavigateToNewEntry());
        ViewTimeCapsuleCommand = new Command(async () => await NavigateToTimeCapsule());
        RefreshCommand = new Command(async () => await LoadDataAsync());
        
        Task.Run(async () => await LoadDataAsync());
    }

    public string DailyPrompt
    {
        get => _dailyPrompt;
        set => SetProperty(ref _dailyPrompt, value);
    }

    public ObservableCollection<JournalEntry> RecentEntries
    {
        get => _recentEntries;
        set => SetProperty(ref _recentEntries, value);
    }

    public ObservableCollection<string> DailyInsights
    {
        get => _dailyInsights;
        set => SetProperty(ref _dailyInsights, value);
    }

    public ICommand NewEntryCommand { get; }
    public ICommand ViewTimeCapsuleCommand { get; }
    public ICommand RefreshCommand { get; }

    private async Task LoadDataAsync()
    {
        if (IsBusy) return;

        try
        {
            IsBusy = true;

            // Load daily prompt
            DailyPrompt = await _aiService.GenerateDailyPromptAsync();

            // Load recent entries
            var entries = await _journalService.GetRecentEntriesAsync(5);
            RecentEntries = new ObservableCollection<JournalEntry>(entries);

            // Load daily insights
            var insights = await _aiService.GetDailyInsightsAsync(entries);
            DailyInsights = new ObservableCollection<string>(insights);
        }
        catch (Exception ex)
        {
            // Handle error
            await Application.Current?.MainPage?.DisplayAlert("Error", ex.Message, "OK")!;
        }
        finally
        {
            IsBusy = false;
        }
    }

    private async Task NavigateToNewEntry()
    {
        await Shell.Current.GoToAsync(nameof(Pages.JournalEntryPage));
    }

    private async Task NavigateToTimeCapsule()
    {
        await Shell.Current.GoToAsync(nameof(Pages.TimeCapsulePage));
    }
}
