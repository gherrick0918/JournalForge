namespace JournalForge.Models;

public class TimeCapsule
{
    public string Id { get; set; } = Guid.NewGuid().ToString();
    public string EntryId { get; set; } = string.Empty;
    public DateTime SealedDate { get; set; } = DateTime.Now;
    public DateTime UnsealDate { get; set; }
    public string Title { get; set; } = string.Empty;
    public string PreviewText { get; set; } = string.Empty;
    public bool IsUnsealed { get; set; }
    public string Message { get; set; } = string.Empty;
}
