using JournalForge.Models;

namespace JournalForge.Services;

public interface IAIService
{
    Task<string> GenerateDailyPromptAsync();
    Task<string> GenerateProbingQuestionAsync(string entryContent);
    Task<string> SuggestEntryEndingAsync(string entryContent);
    Task<List<string>> GetDailyInsightsAsync(List<JournalEntry> recentEntries);
}

public class AIService : IAIService
{
    private readonly Random _random = new();
    
    private readonly List<string> _dailyPrompts = new()
    {
        "What adventure did you embark on today, brave chronicler?",
        "Describe a moment today that made you feel like a hero.",
        "What quest awaits you tomorrow?",
        "If today were a chapter in your life's saga, what would it be called?",
        "What wisdom have you gained from today's journey?",
        "Describe someone who acted as a guide or mentor today.",
        "What challenge did you overcome today?",
        "If you could relive one moment from today, which would it be?",
        "What spell would you cast to improve tomorrow?",
        "Reflect on your character growth today."
    };

    private readonly List<string> _probingQuestions = new()
    {
        "How did that make you feel in the moment?",
        "What might be the deeper meaning behind this experience?",
        "How does this connect to your larger journey?",
        "What would you tell your past self about this?",
        "What lesson is hidden within this experience?",
        "How will this shape your path forward?",
        "What surprised you most about this situation?",
        "If you could change one thing, what would it be?",
        "What strength did you discover in yourself?",
        "How does this relate to your values and goals?"
    };

    public Task<string> GenerateDailyPromptAsync()
    {
        var prompt = _dailyPrompts[_random.Next(_dailyPrompts.Count)];
        return Task.FromResult(prompt);
    }

    public Task<string> GenerateProbingQuestionAsync(string entryContent)
    {
        // Enhanced implementation - analyzes content for context-aware questions
        // In a real app, this would use AI to deeply analyze content
        
        var content = entryContent.ToLower();
        string question;
        
        // Context-aware question selection based on keywords - more conversational responses
        if (content.Contains("feel") || content.Contains("felt") || content.Contains("emotion"))
        {
            var responses = new[]
            {
                "I hear you. How did that make you feel in the moment?",
                "That sounds intense. What emotions were coming up for you?"
            };
            question = responses[_random.Next(responses.Length)];
        }
        else if (content.Contains("learned") || content.Contains("discovered") || content.Contains("realized"))
        {
            var responses = new[]
            {
                "That's an interesting realization. What do you think this means for you?",
                "Sounds like an important discovery. How might this change things going forward?"
            };
            question = responses[_random.Next(responses.Length)];
        }
        else if (content.Contains("rough") || content.Contains("bad") || content.Contains("tough"))
        {
            var responses = new[]
            {
                "That sounds really tough. What made the day especially rough?",
                "I'm sorry to hear it's been rough. What's been weighing on you most?"
            };
            question = responses[_random.Next(responses.Length)];
        }
        else if (content.Contains("friend") || content.Contains("family") || content.Contains("person") || content.Contains("someone"))
        {
            var responses = new[]
            {
                "How did that interaction feel for you?",
                "That's interesting. How did this change your perspective on things?"
            };
            question = responses[_random.Next(responses.Length)];
        }
        else if (content.Contains("work") || content.Contains("job") || content.Contains("career"))
        {
            var responses = new[]
            {
                "Work can be complex. How does this fit with what you value?",
                "How are you feeling about this work situation?"
            };
            question = responses[_random.Next(responses.Length)];
        }
        else if (content.Contains("challenge") || content.Contains("difficult") || content.Contains("hard") || content.Contains("struggle"))
        {
            var responses = new[]
            {
                "That sounds challenging. What strengths are you drawing on to get through this?",
                "I can hear that this is difficult. What's helping you cope?"
            };
            question = responses[_random.Next(responses.Length)];
        }
        else if (content.Contains("happy") || content.Contains("joy") || content.Contains("excited") || content.Contains("wonderful"))
        {
            var responses = new[]
            {
                "That's wonderful! What made this moment so special?",
                "I love hearing this! What's standing out most about this experience?"
            };
            question = responses[_random.Next(responses.Length)];
        }
        else if (content.Contains("sad") || content.Contains("upset") || content.Contains("angry") || content.Contains("frustrated"))
        {
            var responses = new[]
            {
                "That sounds really hard. What do you need right now?",
                "I'm here with you. What would help you process these feelings?"
            };
            question = responses[_random.Next(responses.Length)];
        }
        else if (content.Contains("future") || content.Contains("tomorrow") || content.Contains("next"))
        {
            var responses = new[]
            {
                "Interesting. How do you see this shaping your path forward?",
                "What are you hoping will happen next?"
            };
            question = responses[_random.Next(responses.Length)];
        }
        else if (content.Contains("past") || content.Contains("before") || content.Contains("ago"))
        {
            var responses = new[]
            {
                "Looking back can be powerful. What would you tell your past self about this?",
                "How do you see this differently now compared to then?"
            };
            question = responses[_random.Next(responses.Length)];
        }
        else
        {
            // Default to a varied question
            question = _probingQuestions[_random.Next(_probingQuestions.Count)];
        }
        
        return Task.FromResult(question);
    }

    public Task<string> SuggestEntryEndingAsync(string entryContent)
    {
        var endings = new List<string>
        {
            "As you close this chapter, remember that every journey begins with a single step.",
            "May tomorrow bring new adventures and discoveries.",
            "Your story continues to unfold in magnificent ways.",
            "End your entry knowing that you've grown stronger today.",
            "This moment is now part of your epic tale."
        };
        
        return Task.FromResult(endings[_random.Next(endings.Count)]);
    }

    public Task<List<string>> GetDailyInsightsAsync(List<JournalEntry> recentEntries)
    {
        var insights = new List<string>
        {
            $"📊 You've chronicled {recentEntries.Count} entries recently.",
            "🔥 Your consistency in journaling shows dedication to self-reflection.",
            "⚔️ Continue your journey with courage and curiosity.",
            "📖 Each entry adds to the legend you're creating."
        };

        return Task.FromResult(insights);
    }
}
