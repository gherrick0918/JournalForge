namespace JournalForge.Models;

public class AIPrompt
{
    public string Id { get; set; } = Guid.NewGuid().ToString();
    public DateTime Date { get; set; } = DateTime.Today;
    public string PromptText { get; set; } = string.Empty;
    public string Category { get; set; } = string.Empty;
    public bool IsUsed { get; set; }
}
