# OpenAI Integration Setup Guide

JournalForge now supports real AI-powered features using OpenAI's API! This guide will help you configure your API key.

## Quick Start

### Option 1: Environment Variable (Recommended)

1. **Get your API key** from [OpenAI Platform](https://platform.openai.com/api-keys)

2. **Set the environment variable** before running the app:

   **Windows (Command Prompt):**
   ```cmd
   set OPENAI_API_KEY=sk-your-api-key-here
   ```

   **Windows (PowerShell):**
   ```powershell
   $env:OPENAI_API_KEY="sk-your-api-key-here"
   ```

   **macOS/Linux:**
   ```bash
   export OPENAI_API_KEY=sk-your-api-key-here
   ```

3. **Run the app** - it will automatically use OpenAI for AI features!

### Option 2: Code Configuration

1. **Get your API key** from [OpenAI Platform](https://platform.openai.com/api-keys)

2. **Edit `MauiProgram.cs`** and replace the empty string with your API key:

   ```csharp
   var appSettings = new AppSettings
   {
       // Replace this line with your actual API key
       OpenAIApiKey = "sk-your-api-key-here",
       OpenAIModel = "gpt-4o-mini"
   };
   ```

3. **Save and rebuild** the app

⚠️ **Security Warning:** If you use Option 2, **DO NOT commit your API key** to version control!

## How It Works

- **With API Key**: The app uses OpenAI's GPT models for truly dynamic, context-aware responses
- **Without API Key**: The app falls back to the built-in mock AI service (keyword-based responses)

The OpenAI integration provides:
- ✨ **Dynamic Daily Prompts**: Unique prompts every time, tailored to journaling
- 🤔 **Context-Aware Questions**: AI analyzes your entire entry to ask relevant, insightful questions
- ✍️ **Personalized Endings**: Thoughtful closings based on your specific content
- 📊 **Smart Insights**: AI-generated insights about your journaling patterns and themes

## Models Available

The default model is `gpt-4o-mini` which is cost-effective and fast. You can change the model in `MauiProgram.cs`:

| Model | Best For | Cost |
|-------|----------|------|
| `gpt-4o-mini` | Fast, cost-effective (default) | Lowest |
| `gpt-4o` | Best quality responses | Medium |
| `gpt-4-turbo` | High quality, faster | Medium-High |
| `gpt-3.5-turbo` | Budget option | Very Low |

## Cost Considerations

OpenAI charges per token (roughly per word). With `gpt-4o-mini`:
- Daily prompt: ~$0.0001 per generation
- Probing question: ~$0.0003 per question (depends on entry length)
- Suggested ending: ~$0.0003 per suggestion
- Daily insights: ~$0.0005 per insight

**Estimated cost:** $0.01-0.05 per day for typical usage.

## Fallback Behavior

The app is designed to be resilient:
- If the API key is not set, it uses the built-in mock AI
- If the API request fails (network issues, rate limits), it falls back to mock responses
- You'll never lose functionality - the app always works!

## Testing Your Setup

1. Launch the app
2. Go to "New Entry"
3. Click "🤔 Probe Deeper" after writing something
4. If you see highly contextual responses that vary each time, OpenAI is working!
5. Check the console logs (in debug mode) for any API errors

## Troubleshooting

### "Still seeing generic responses"
- Verify your API key is correctly set (starts with `sk-`)
- Check the console for error messages
- Ensure you have a valid OpenAI account with credits

### "API requests timing out"
- Check your internet connection
- The app has a 30-second timeout - longer entries might need more time
- Consider using a faster model like `gpt-4o-mini`

### "Getting rate limit errors"
- OpenAI has usage limits based on your account tier
- Wait a few minutes and try again
- Consider upgrading your OpenAI account tier

## Security Best Practices

1. **Never commit API keys** to version control
2. **Use environment variables** in production
3. **Rotate keys regularly** if exposed
4. **Set usage limits** in your OpenAI dashboard
5. **Monitor usage** to avoid unexpected charges

## Additional Resources

- [OpenAI API Documentation](https://platform.openai.com/docs/api-reference)
- [OpenAI Pricing](https://openai.com/pricing)
- [Usage Dashboard](https://platform.openai.com/usage)

---

**Questions?** Check the project README or open an issue on GitHub.
