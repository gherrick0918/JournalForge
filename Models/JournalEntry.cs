namespace JournalForge.Models;

public class ConversationMessage
{
    public string Sender { get; set; } = string.Empty; // "User" or "AI"
    public string Content { get; set; } = string.Empty;
    public DateTime Timestamp { get; set; } = DateTime.Now;
}

public class JournalEntry
{
    public string Id { get; set; } = Guid.NewGuid().ToString();
    public DateTime CreatedDate { get; set; } = DateTime.Now;
    public string Title { get; set; } = string.Empty;
    public string Content { get; set; } = string.Empty;
    public List<string> Tags { get; set; } = new();
    public string Mood { get; set; } = string.Empty;
    public List<string> AIConversation { get; set; } = new();
    public List<ConversationMessage> ConversationMessages { get; set; } = new();
    public string VoiceRecordingPath { get; set; } = string.Empty;
    public bool IsTimeCapsule { get; set; }
    public DateTime? UnsealDate { get; set; }
}
