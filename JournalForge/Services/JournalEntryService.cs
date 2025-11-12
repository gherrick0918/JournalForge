using JournalForge.Models;

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

    public Task<List<JournalEntry>> GetAllEntriesAsync()
    {
        return Task.FromResult(_entries.OrderByDescending(e => e.CreatedDate).ToList());
    }

    public Task<JournalEntry?> GetEntryByIdAsync(string id)
    {
        var entry = _entries.FirstOrDefault(e => e.Id == id);
        return Task.FromResult(entry);
    }

    public Task<bool> SaveEntryAsync(JournalEntry entry)
    {
        var existing = _entries.FirstOrDefault(e => e.Id == entry.Id);
        if (existing != null)
        {
            _entries.Remove(existing);
        }
        
        _entries.Add(entry);
        return Task.FromResult(true);
    }

    public Task<bool> DeleteEntryAsync(string id)
    {
        var entry = _entries.FirstOrDefault(e => e.Id == id);
        if (entry != null)
        {
            _entries.Remove(entry);
            return Task.FromResult(true);
        }
        return Task.FromResult(false);
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
