using JournalForge.Models;
using System.Text;
using System.Text.Json;

namespace JournalForge.Services;

public interface IExportService
{
    Task ExportEntryAsTextAsync(JournalEntry entry);
    Task ExportEntryAsJsonAsync(JournalEntry entry);
    Task ExportAllEntriesAsTextAsync(List<JournalEntry> entries);
    Task ExportAllEntriesAsJsonAsync(List<JournalEntry> entries);
}

public class ExportService : IExportService
{
    public async Task ExportEntryAsTextAsync(JournalEntry entry)
    {
        try
        {
            var content = FormatEntryAsText(entry);
            var fileName = $"journal_entry_{entry.CreatedDate:yyyy-MM-dd}_{SanitizeFileName(entry.Title)}.txt";
            
            await SaveAndShareFileAsync(fileName, content);
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"Error exporting entry as text: {ex.Message}");
            throw;
        }
    }

    public async Task ExportEntryAsJsonAsync(JournalEntry entry)
    {
        try
        {
            var json = JsonSerializer.Serialize(entry, new JsonSerializerOptions
            {
                WriteIndented = true
            });
            var fileName = $"journal_entry_{entry.CreatedDate:yyyy-MM-dd}_{SanitizeFileName(entry.Title)}.json";
            
            await SaveAndShareFileAsync(fileName, json);
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"Error exporting entry as JSON: {ex.Message}");
            throw;
        }
    }

    public async Task ExportAllEntriesAsTextAsync(List<JournalEntry> entries)
    {
        try
        {
            var sb = new StringBuilder();
            sb.AppendLine("═══════════════════════════════════════════════════");
            sb.AppendLine("           📚 JOURNAL EXPORT 📚");
            sb.AppendLine("═══════════════════════════════════════════════════");
            sb.AppendLine($"Export Date: {DateTime.Now:MMMM dd, yyyy}");
            sb.AppendLine($"Total Entries: {entries.Count}");
            sb.AppendLine("═══════════════════════════════════════════════════");
            sb.AppendLine();

            foreach (var entry in entries.OrderBy(e => e.CreatedDate))
            {
                sb.AppendLine(FormatEntryAsText(entry));
                sb.AppendLine();
                sb.AppendLine("───────────────────────────────────────────────────");
                sb.AppendLine();
            }

            var fileName = $"journal_export_{DateTime.Now:yyyy-MM-dd}.txt";
            await SaveAndShareFileAsync(fileName, sb.ToString());
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"Error exporting all entries as text: {ex.Message}");
            throw;
        }
    }

    public async Task ExportAllEntriesAsJsonAsync(List<JournalEntry> entries)
    {
        try
        {
            var json = JsonSerializer.Serialize(entries, new JsonSerializerOptions
            {
                WriteIndented = true
            });
            var fileName = $"journal_export_{DateTime.Now:yyyy-MM-dd}.json";
            
            await SaveAndShareFileAsync(fileName, json);
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"Error exporting all entries as JSON: {ex.Message}");
            throw;
        }
    }

    private string FormatEntryAsText(JournalEntry entry)
    {
        var sb = new StringBuilder();
        sb.AppendLine($"Title: {entry.Title}");
        sb.AppendLine($"Date: {entry.CreatedDate:MMMM dd, yyyy h:mm tt}");
        
        if (!string.IsNullOrWhiteSpace(entry.Mood))
        {
            sb.AppendLine($"Mood: {entry.Mood}");
        }
        
        if (entry.Tags != null && entry.Tags.Any())
        {
            sb.AppendLine($"Tags: {string.Join(", ", entry.Tags)}");
        }
        
        sb.AppendLine();
        sb.AppendLine("Content:");
        sb.AppendLine(entry.Content);
        
        if (entry.ConversationMessages != null && entry.ConversationMessages.Any())
        {
            sb.AppendLine();
            sb.AppendLine("AI Conversation:");
            foreach (var message in entry.ConversationMessages)
            {
                sb.AppendLine($"  [{message.Sender}]: {message.Content}");
            }
        }
        
        return sb.ToString();
    }

    private async Task SaveAndShareFileAsync(string fileName, string content)
    {
        try
        {
            // Save to app's cache directory
            var filePath = Path.Combine(FileSystem.CacheDirectory, fileName);
            await File.WriteAllTextAsync(filePath, content);

            // Use MAUI's Share API to let user choose where to save
            await Share.Default.RequestAsync(new ShareFileRequest
            {
                Title = "Export Journal Entry",
                File = new ShareFile(filePath)
            });
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"Error saving and sharing file: {ex.Message}");
            throw;
        }
    }

    private string SanitizeFileName(string fileName)
    {
        var invalidChars = Path.GetInvalidFileNameChars();
        var sanitized = string.Join("_", fileName.Split(invalidChars, StringSplitOptions.RemoveEmptyEntries));
        return sanitized.Length > 50 ? sanitized.Substring(0, 50) : sanitized;
    }
}
