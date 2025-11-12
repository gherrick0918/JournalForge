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
        
        // Context-aware question selection based on keywords
        if (content.Contains("feel") || content.Contains("felt") || content.Contains("emotion"))
        {
            question = "How did that make you feel in the moment?";
        }
        else if (content.Contains("learned") || content.Contains("discovered") || content.Contains("realized"))
        {
            question = "What lesson is hidden within this experience?";
        }
        else if (content.Contains("today") || content.Contains("day"))
        {
            question = "How does this connect to your larger journey?";
        }
        else if (content.Contains("friend") || content.Contains("family") || content.Contains("person") || content.Contains("someone"))
        {
            question = "How did this interaction change your perspective?";
        }
        else if (content.Contains("work") || content.Contains("job") || content.Contains("career"))
        {
            question = "How does this relate to your values and goals?";
        }
        else if (content.Contains("challenge") || content.Contains("difficult") || content.Contains("hard") || content.Contains("struggle"))
        {
            question = "What strength did you discover in yourself?";
        }
        else if (content.Contains("happy") || content.Contains("joy") || content.Contains("excited") || content.Contains("wonderful"))
        {
            question = "What made this moment particularly special for you?";
        }
        else if (content.Contains("sad") || content.Contains("upset") || content.Contains("angry") || content.Contains("frustrated"))
        {
            question = "What do you need right now to process these feelings?";
        }
        else if (content.Contains("future") || content.Contains("tomorrow") || content.Contains("next"))
        {
            question = "How will this shape your path forward?";
        }
        else if (content.Contains("past") || content.Contains("before") || content.Contains("ago"))
        {
            question = "What would you tell your past self about this?";
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
