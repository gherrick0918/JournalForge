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
            var systemPrompt = @"You are a warm, empathetic journaling companion having a natural conversation. 
Respond conversationally with genuine interest and empathy. Your responses should feel like a supportive friend, not a therapist or interviewer.
- Acknowledge what they shared before asking a question
- Match their emotional tone (if they're excited, be enthusiastic; if they're struggling, be gentle)
- Keep responses natural and flowing, 1-3 sentences total
- Ask follow-up questions that show you're really listening
- Avoid formulaic patterns like always ending with a single question
Example good responses:
- 'That sounds really challenging. What's weighing on you most about it?'
- 'I can hear the frustration in that. Have you been able to talk to anyone else about it?'
- 'Wow, that must have felt amazing! What was going through your mind in that moment?'";
            var userPrompt = $"Here's what they just shared:\n\n{entryContent}\n\nRespond conversationally with empathy and a follow-up question.";
            
            var response = await CallOpenAIAsync(systemPrompt, userPrompt);
            return response ?? await _fallbackService.GenerateProbingQuestionAsync(entryContent);
        }
        catch
        {
            return await _fallbackService.GenerateProbingQuestionAsync(entryContent);
        }
    }

    public async Task<string> GenerateProbingQuestionAsync(List<Models.ConversationMessage> conversationHistory)
    {
        if (!IsConfigured)
        {
            return await _fallbackService.GenerateProbingQuestionAsync(conversationHistory);
        }

        try
        {
            var systemPrompt = @"You are a warm, empathetic journaling companion having a natural conversation. 
Respond conversationally with genuine interest and empathy. Your responses should feel like a supportive friend, not a therapist or interviewer.
- Acknowledge what they shared before asking a question
- Match their emotional tone (if they're excited, be enthusiastic; if they're struggling, be gentle)
- Keep responses natural and flowing, 1-3 sentences total
- Ask follow-up questions that show you're really listening and building on the conversation
- Vary your responses - don't repeat yourself or ask similar questions
- Reference earlier parts of the conversation when relevant
- Avoid formulaic patterns like always ending with a single question
IMPORTANT: Keep track of the full conversation. Don't repeat questions or responses you've already given.";
            
            var response = await CallOpenAIWithHistoryAsync(systemPrompt, conversationHistory);
            return response ?? await _fallbackService.GenerateProbingQuestionAsync(conversationHistory);
        }
        catch
        {
            return await _fallbackService.GenerateProbingQuestionAsync(conversationHistory);
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
            // Build comprehensive entry summary with conversation context
            var entrySummary = new StringBuilder();
            foreach (var (entry, index) in recentEntries.Select((e, i) => (e, i)))
            {
                entrySummary.AppendLine($"\nEntry {index + 1} ({entry.CreatedDate:MMM dd, yyyy}):");
                entrySummary.AppendLine($"Title: {entry.Title}");
                
                // Include conversation exchanges for richer context
                if (entry.ConversationMessages.Any())
                {
                    var userMessages = entry.ConversationMessages.Where(m => m.Sender == "User").ToList();
                    entrySummary.AppendLine($"Conversation depth: {userMessages.Count} exchanges");
                    
                    // Include first and last user messages for context
                    if (userMessages.Count > 0)
                    {
                        var firstMsg = userMessages.First().Content;
                        entrySummary.AppendLine($"Opening: {(firstMsg.Length > 100 ? firstMsg.Substring(0, 97) + "..." : firstMsg)}");
                        
                        if (userMessages.Count > 1)
                        {
                            var lastMsg = userMessages.Last().Content;
                            entrySummary.AppendLine($"Closing: {(lastMsg.Length > 100 ? lastMsg.Substring(0, 97) + "..." : lastMsg)}");
                        }
                    }
                }
            }
            
            var systemPrompt = @"You are a thoughtful journaling companion analyzing someone's recent journal entries. 
Based on their entries, provide 3-5 meaningful, personalized insights about:
- Patterns or themes in their reflections
- Growth or progress you notice
- Emotional journey or shifts
- Depth of self-reflection
- Consistency and engagement

Each insight should be:
- One concise sentence (max 15 words)
- Personal and specific to their entries
- Encouraging and supportive in tone
- Use RPG/fantasy themed emojis (⚔️📜🔮⏰📖🔥💭🌟)
- Varied in focus (don't repeat similar observations)

Example insights:
- '🔥 Your 5-day writing streak shows real commitment to self-discovery!'
- '💭 I notice you're exploring work-life balance themes deeply.'
- '⚔️ You're tackling challenging emotions with courage and honesty.'
- '🌟 Your reflections are becoming more nuanced and detailed.'";
            
            var userPrompt = $"Recent journal entries:\n{entrySummary}\n\nProvide 3-5 personalized insights about their journaling journey.";
            
            var response = await CallOpenAIAsync(systemPrompt, userPrompt, maxTokens: 250);
            
            if (response != null)
            {
                // Parse multi-line response into list
                var insights = response.Split('\n', StringSplitOptions.RemoveEmptyEntries)
                    .Select(s => s.Trim())
                    .Where(s => !string.IsNullOrWhiteSpace(s) && 
                               (s.Contains('•') || s.Contains('-') || 
                                s.Any(c => char.IsDigit(c) && c <= '9')))
                    .Select(s => s.TrimStart('•', '-', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', ' '))
                    .Where(s => s.Length > 10) // Filter out too-short lines
                    .Take(5)
                    .ToList();
                
                if (insights.Count >= 3)
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

    private async Task<string?> CallOpenAIAsync(string systemPrompt, string userPrompt, int maxTokens = 150)
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
            max_tokens = maxTokens
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

    private async Task<string?> CallOpenAIWithHistoryAsync(string systemPrompt, List<Models.ConversationMessage> conversationHistory)
    {
        // Build the messages array with full conversation history
        var messages = new List<object>
        {
            new { role = "system", content = systemPrompt }
        };
        
        // Add all conversation messages
        foreach (var msg in conversationHistory)
        {
            var role = msg.Sender == "User" ? "user" : "assistant";
            messages.Add(new { role, content = msg.Content });
        }

        var requestBody = new
        {
            model = _model,
            messages = messages.ToArray(),
            temperature = 0.8, // Slightly higher for more varied responses
            max_tokens = 200,
            presence_penalty = 0.6, // Discourage repetition
            frequency_penalty = 0.6  // Discourage using same phrases
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
