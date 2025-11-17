using System.Collections.ObjectModel;
using System.Windows.Input;
using JournalForge.Models;
using JournalForge.Services;

namespace JournalForge.ViewModels;

public class HistoryViewModel : BaseViewModel
{
    private readonly IJournalEntryService _journalService;
    private readonly IExportService _exportService;
    private ObservableCollection<JournalEntry> _allEntries = new();
    private ObservableCollection<JournalEntry> _filteredEntries = new();
    private string _searchText = string.Empty;
    private bool _sortNewestFirst = true;

    public HistoryViewModel(
        IJournalEntryService journalService,
        IExportService exportService)
    {
        _journalService = journalService;
        _exportService = exportService;

        Title = "Chronicle History";

        ViewEntryCommand = new Command<JournalEntry>(async (entry) => await ViewEntry(entry));
        ExportEntryCommand = new Command<JournalEntry>(async (entry) => await ExportEntry(entry));
        ExportAllCommand = new Command(async () => await ExportAllEntries());
        DeleteEntryCommand = new Command<JournalEntry>(async (entry) => await DeleteEntry(entry));
        SearchCommand = new Command(async () => await ApplyFilter());
        ToggleSortCommand = new Command(async () => await ToggleSort());
        RefreshCommand = new Command(async () => await LoadEntriesAsync());

        Task.Run(async () => await LoadEntriesAsync());
    }

    public ObservableCollection<JournalEntry> FilteredEntries
    {
        get => _filteredEntries;
        set => SetProperty(ref _filteredEntries, value);
    }

    public string SearchText
    {
        get => _searchText;
        set
        {
            if (SetProperty(ref _searchText, value))
            {
                Task.Run(async () => await ApplyFilter());
            }
        }
    }

    public string EntriesCountText => FilteredEntries.Count == 1 
        ? "1 entry found" 
        : $"{FilteredEntries.Count} entries found";

    public ICommand ViewEntryCommand { get; }
    public ICommand ExportEntryCommand { get; }
    public ICommand ExportAllCommand { get; }
    public ICommand DeleteEntryCommand { get; }
    public ICommand SearchCommand { get; }
    public ICommand ToggleSortCommand { get; }
    public ICommand RefreshCommand { get; }

    private async Task LoadEntriesAsync()
    {
        if (IsBusy) return;

        try
        {
            IsBusy = true;

            var entries = await _journalService.GetAllEntriesAsync();
            _allEntries = new ObservableCollection<JournalEntry>(entries);
            
            await ApplyFilter();
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Error", 
                $"Failed to load entries: {ex.Message}", 
                "OK")!;
        }
        finally
        {
            IsBusy = false;
        }
    }

    private Task ApplyFilter()
    {
        var filtered = _allEntries.AsEnumerable();

        // Apply search filter
        if (!string.IsNullOrWhiteSpace(_searchText))
        {
            var searchLower = _searchText.ToLower();
            filtered = filtered.Where(e =>
                e.Title.ToLower().Contains(searchLower) ||
                e.Content.ToLower().Contains(searchLower));
        }

        // Apply sorting
        filtered = _sortNewestFirst
            ? filtered.OrderByDescending(e => e.CreatedDate)
            : filtered.OrderBy(e => e.CreatedDate);

        FilteredEntries = new ObservableCollection<JournalEntry>(filtered);
        OnPropertyChanged(nameof(EntriesCountText));

        return Task.CompletedTask;
    }

    private async Task ToggleSort()
    {
        _sortNewestFirst = !_sortNewestFirst;
        await ApplyFilter();
        
        var sortText = _sortNewestFirst ? "Newest First" : "Oldest First";
        await Application.Current?.MainPage?.DisplayAlert(
            "Sort Order", 
            $"Entries sorted: {sortText}", 
            "OK")!;
    }

    private async Task ViewEntry(JournalEntry? entry)
    {
        if (entry == null) return;

        try
        {
            // Navigate to journal entry page with the entry ID
            await Shell.Current.GoToAsync($"JournalEntryPage?entryId={entry.Id}");
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Error", 
                $"Failed to open entry: {ex.Message}", 
                "OK")!;
        }
    }

    private async Task ExportEntry(JournalEntry? entry)
    {
        if (entry == null) return;

        try
        {
            var result = await Application.Current?.MainPage?.DisplayActionSheet(
                "Export Entry", 
                "Cancel", 
                null, 
                "Plain Text", 
                "JSON Format")!;

            if (result == "Plain Text")
            {
                await _exportService.ExportEntryAsTextAsync(entry);
                await Application.Current?.MainPage?.DisplayAlert(
                    "Success", 
                    "Entry exported successfully!", 
                    "OK")!;
            }
            else if (result == "JSON Format")
            {
                await _exportService.ExportEntryAsJsonAsync(entry);
                await Application.Current?.MainPage?.DisplayAlert(
                    "Success", 
                    "Entry exported successfully!", 
                    "OK")!;
            }
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Error", 
                $"Failed to export entry: {ex.Message}", 
                "OK")!;
        }
    }

    private async Task ExportAllEntries()
    {
        try
        {
            if (FilteredEntries.Count == 0)
            {
                await Application.Current?.MainPage?.DisplayAlert(
                    "No Entries", 
                    "There are no entries to export.", 
                    "OK")!;
                return;
            }

            var result = await Application.Current?.MainPage?.DisplayActionSheet(
                "Export All Entries", 
                "Cancel", 
                null, 
                "Plain Text", 
                "JSON Format")!;

            if (result == "Plain Text")
            {
                await _exportService.ExportAllEntriesAsTextAsync(FilteredEntries.ToList());
                await Application.Current?.MainPage?.DisplayAlert(
                    "Success", 
                    $"{FilteredEntries.Count} entries exported successfully!", 
                    "OK")!;
            }
            else if (result == "JSON Format")
            {
                await _exportService.ExportAllEntriesAsJsonAsync(FilteredEntries.ToList());
                await Application.Current?.MainPage?.DisplayAlert(
                    "Success", 
                    $"{FilteredEntries.Count} entries exported successfully!", 
                    "OK")!;
            }
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Error", 
                $"Failed to export entries: {ex.Message}", 
                "OK")!;
        }
    }

    private async Task DeleteEntry(JournalEntry? entry)
    {
        if (entry == null) return;

        try
        {
            var confirm = await Application.Current?.MainPage?.DisplayAlert(
                "Delete Entry", 
                $"Are you sure you want to delete '{entry.Title}'? This action cannot be undone.", 
                "Delete", 
                "Cancel")!;

            if (confirm)
            {
                var success = await _journalService.DeleteEntryAsync(entry.Id);
                if (success)
                {
                    await LoadEntriesAsync();
                    await Application.Current?.MainPage?.DisplayAlert(
                        "Success", 
                        "Entry deleted successfully.", 
                        "OK")!;
                }
                else
                {
                    await Application.Current?.MainPage?.DisplayAlert(
                        "Error", 
                        "Failed to delete entry.", 
                        "OK")!;
                }
            }
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Error", 
                $"Failed to delete entry: {ex.Message}", 
                "OK")!;
        }
    }
}
