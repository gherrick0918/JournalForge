using JournalForge.Models;
using System.Text.Json;

namespace JournalForge.Services;

public interface IJournalEntryService
{
    Task<List<JournalEntry>> GetAllEntriesAsync();
    Task<JournalEntry?> GetEntryByIdAsync(string id);
    Task<bool> SaveEntryAsync(JournalEntry entry);
    Task<bool> DeleteEntryAsync(string id);
    Task<List<JournalEntry>> GetRecentEntriesAsync(int count = 10);
}

public class JournalEntryService : IJournalEntryService
{
    private readonly List<JournalEntry> _entries = new();
    private readonly string _dataFilePath;
    private readonly SemaphoreSlim _fileLock = new(1, 1);

    public JournalEntryService()
    {
        var appDataPath = FileSystem.AppDataDirectory;
        _dataFilePath = Path.Combine(appDataPath, "journal_entries.json");
        
        // Load existing entries on initialization
        Task.Run(async () => await LoadEntriesAsync()).Wait();
    }

    public Task<List<JournalEntry>> GetAllEntriesAsync()
    {
        return Task.FromResult(_entries.OrderByDescending(e => e.CreatedDate).ToList());
    }

    public Task<JournalEntry?> GetEntryByIdAsync(string id)
    {
        System.Diagnostics.Debug.WriteLine($"JournalEntryService.GetEntryByIdAsync - Looking for entry ID: {id}");
        System.Diagnostics.Debug.WriteLine($"JournalEntryService.GetEntryByIdAsync - Total entries in memory: {_entries.Count}");
        
        var entry = _entries.FirstOrDefault(e => e.Id == id);
        
        if (entry != null)
        {
            System.Diagnostics.Debug.WriteLine($"JournalEntryService.GetEntryByIdAsync - Entry found: {entry.Title}");
        }
        else
        {
            System.Diagnostics.Debug.WriteLine($"JournalEntryService.GetEntryByIdAsync - Entry not found");
            // Log all entry IDs for debugging
            foreach (var e in _entries)
            {
                System.Diagnostics.Debug.WriteLine($"  Available entry ID: {e.Id}, Title: {e.Title}");
            }
        }
        
        return Task.FromResult(entry);
    }

    public async Task<bool> SaveEntryAsync(JournalEntry entry)
    {
        var existing = _entries.FirstOrDefault(e => e.Id == entry.Id);
        if (existing != null)
        {
            _entries.Remove(existing);
        }
        
        _entries.Add(entry);
        await SaveEntriesToFileAsync();
        return true;
    }

    public async Task<bool> DeleteEntryAsync(string id)
    {
        var entry = _entries.FirstOrDefault(e => e.Id == id);
        if (entry != null)
        {
            _entries.Remove(entry);
            await SaveEntriesToFileAsync();
            return true;
        }
        return false;
    }

    private async Task LoadEntriesAsync()
    {
        await _fileLock.WaitAsync();
        try
        {
            if (File.Exists(_dataFilePath))
            {
                var json = await File.ReadAllTextAsync(_dataFilePath);
                var entries = JsonSerializer.Deserialize<List<JournalEntry>>(json);
                if (entries != null)
                {
                    _entries.Clear();
                    _entries.AddRange(entries);
                }
            }
        }
        catch (Exception ex)
        {
            // Log error but don't throw - app should continue with empty list
            System.Diagnostics.Debug.WriteLine($"Error loading entries: {ex.Message}");
        }
        finally
        {
            _fileLock.Release();
        }
    }

    private async Task SaveEntriesToFileAsync()
    {
        await _fileLock.WaitAsync();
        try
        {
            var json = JsonSerializer.Serialize(_entries, new JsonSerializerOptions
            {
                WriteIndented = true
            });
            await File.WriteAllTextAsync(_dataFilePath, json);
        }
        catch (Exception ex)
        {
            // Log error but don't throw - this is a background operation
            System.Diagnostics.Debug.WriteLine($"Error saving entries: {ex.Message}");
        }
        finally
        {
            _fileLock.Release();
        }
    }

    public Task<List<JournalEntry>> GetRecentEntriesAsync(int count = 10)
    {
        var recent = _entries
            .OrderByDescending(e => e.CreatedDate)
            .Take(count)
            .ToList();
        
        return Task.FromResult(recent);
    }
}
