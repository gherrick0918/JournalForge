using System.Text;
using System.Text.Json;
using JournalForge.Models;

namespace JournalForge.Services;

/// <summary>
/// OpenAI-powered implementation of IAIService using the OpenAI API.
/// Falls back to mock responses if API key is not configured or requests fail.
/// </summary>
public class OpenAIService : IAIService
{
    private readonly HttpClient _httpClient;
    private readonly string _apiKey;
    private readonly string _model;
    private readonly AIService _fallbackService;
    
    public OpenAIService(AppSettings settings)
    {
        _apiKey = settings.OpenAIApiKey;
        _model = settings.OpenAIModel;
        _httpClient = new HttpClient
        {
            BaseAddress = new Uri("https://api.openai.com/v1/"),
            Timeout = TimeSpan.FromSeconds(30)
        };
        _httpClient.DefaultRequestHeaders.Add("Authorization", $"Bearer {_apiKey}");
        
        // Fallback service for when API is not configured or fails
        _fallbackService = new AIService();
    }

    private bool IsConfigured => !string.IsNullOrWhiteSpace(_apiKey);

    public async Task<string> GenerateDailyPromptAsync()
    {
        if (!IsConfigured)
        {
            return await _fallbackService.GenerateDailyPromptAsync();
        }

        try
        {
            var systemPrompt = "You are a thoughtful journaling companion with an RPG/fantasy theme. Generate a single creative, inspiring daily journaling prompt that encourages self-reflection. Keep it concise (one sentence) and use fantasy/RPG language like 'quest', 'journey', 'chronicle', 'adventure', etc.";
            var userPrompt = "Generate a daily journaling prompt.";
            
            var response = await CallOpenAIAsync(systemPrompt, userPrompt);
            return response ?? await _fallbackService.GenerateDailyPromptAsync();
        }
        catch
        {
            return await _fallbackService.GenerateDailyPromptAsync();
        }
    }

    public async Task<string> GenerateProbingQuestionAsync(string entryContent)
    {
        if (!IsConfigured)
        {
            return await _fallbackService.GenerateProbingQuestionAsync(entryContent);
        }

        try
        {
            var systemPrompt = "You are a thoughtful journaling companion helping someone explore their thoughts deeper. Based on their journal entry, ask ONE insightful, open-ended question that encourages deeper reflection. Keep the question concise and focused. Make it personal and specific to their content.";
            var userPrompt = $"Here's the journal entry:\n\n{entryContent}\n\nAsk one probing question to help them explore this deeper.";
            
            var response = await CallOpenAIAsync(systemPrompt, userPrompt);
            return response ?? await _fallbackService.GenerateProbingQuestionAsync(entryContent);
        }
        catch
        {
            return await _fallbackService.GenerateProbingQuestionAsync(entryContent);
        }
    }

    public async Task<string> SuggestEntryEndingAsync(string entryContent)
    {
        if (!IsConfigured)
        {
            return await _fallbackService.SuggestEntryEndingAsync(entryContent);
        }

        try
        {
            var systemPrompt = "You are a thoughtful journaling companion with an RPG/fantasy theme. Based on the journal entry, suggest a meaningful, reflective closing sentence that ties together their thoughts. Use warm, encouraging language with subtle RPG/fantasy themes. Keep it to 1-2 sentences.";
            var userPrompt = $"Here's the journal entry:\n\n{entryContent}\n\nSuggest a closing sentence for this entry.";
            
            var response = await CallOpenAIAsync(systemPrompt, userPrompt);
            return response ?? await _fallbackService.SuggestEntryEndingAsync(entryContent);
        }
        catch
        {
            return await _fallbackService.SuggestEntryEndingAsync(entryContent);
        }
    }

    public async Task<List<string>> GetDailyInsightsAsync(List<JournalEntry> recentEntries)
    {
        if (!IsConfigured || recentEntries.Count == 0)
        {
            return await _fallbackService.GetDailyInsightsAsync(recentEntries);
        }

        try
        {
            var entrySummary = string.Join("\n", recentEntries.Select((e, i) => 
                $"Entry {i + 1} ({e.CreatedDate:MMM dd}): {e.Title}"));
            
            var systemPrompt = "You are a thoughtful journaling companion. Based on recent journal entries, provide 3-4 brief insights about their journaling patterns, growth, or themes. Each insight should be one sentence. Use encouraging language and RPG/fantasy themed emojis (⚔️📜🔮⏰📖🔥).";
            var userPrompt = $"Recent entries:\n{entrySummary}\n\nProvide insights about their journaling journey.";
            
            var response = await CallOpenAIAsync(systemPrompt, userPrompt);
            
            if (response != null)
            {
                // Parse multi-line response into list
                var insights = response.Split('\n', StringSplitOptions.RemoveEmptyEntries)
                    .Select(s => s.Trim())
                    .Where(s => !string.IsNullOrWhiteSpace(s))
                    .ToList();
                
                if (insights.Count > 0)
                {
                    return insights;
                }
            }
            
            return await _fallbackService.GetDailyInsightsAsync(recentEntries);
        }
        catch
        {
            return await _fallbackService.GetDailyInsightsAsync(recentEntries);
        }
    }

    private async Task<string?> CallOpenAIAsync(string systemPrompt, string userPrompt)
    {
        var requestBody = new
        {
            model = _model,
            messages = new[]
            {
                new { role = "system", content = systemPrompt },
                new { role = "user", content = userPrompt }
            },
            temperature = 0.7,
            max_tokens = 150
        };

        var json = JsonSerializer.Serialize(requestBody);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        var response = await _httpClient.PostAsync("chat/completions", content);
        
        if (!response.IsSuccessStatusCode)
        {
            return null;
        }

        var responseJson = await response.Content.ReadAsStringAsync();
        var result = JsonSerializer.Deserialize<JsonDocument>(responseJson);
        
        return result?.RootElement
            .GetProperty("choices")[0]
            .GetProperty("message")
            .GetProperty("content")
            .GetString()?.Trim();
    }
}
