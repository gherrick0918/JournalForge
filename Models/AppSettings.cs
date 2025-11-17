namespace JournalForge.Models;

public class AppSettings
{
    /// <summary>
    /// OpenAI API Key. Set this to your OpenAI API key to enable AI-powered features.
    /// Get your API key from: https://platform.openai.com/api-keys
    /// </summary>
    public string OpenAIApiKey { get; set; } = string.Empty;
    
    /// <summary>
    /// OpenAI Model to use. Default is "gpt-4o-mini" for cost-effectiveness.
    /// Other options: "gpt-4o", "gpt-4-turbo", "gpt-3.5-turbo"
    /// </summary>
    public string OpenAIModel { get; set; } = "gpt-4o-mini";
}
