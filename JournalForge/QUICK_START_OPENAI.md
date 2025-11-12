# Quick Start: Enable OpenAI in 30 Seconds

## Step 1: Get Your API Key
Visit https://platform.openai.com/api-keys and create a new API key.

## Step 2: Set Environment Variable

**Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY="sk-your-api-key-here"
```

**Windows (Command Prompt):**
```cmd
set OPENAI_API_KEY=sk-your-api-key-here
```

**macOS/Linux:**
```bash
export OPENAI_API_KEY=sk-your-api-key-here
```

## Step 3: Run the App
```bash
dotnet build -f net9.0-android
```

That's it! The app will automatically detect your API key and use OpenAI for all AI features.

## Verify It's Working
1. Go to "New Entry"
2. Write something like "I felt happy today"
3. Click "🤔 Probe Deeper"
4. You should see a highly contextual question about your specific content

## Alternative: Direct Configuration
If you prefer not to use environment variables, edit `JournalForge/MauiProgram.cs` line 26:
```csharp
OpenAIApiKey = "sk-your-api-key-here",
```

**⚠️ Security Warning**: Don't commit this file to git if you hardcode the key!

## Need Help?
See [OPENAI_SETUP.md](OPENAI_SETUP.md) for:
- Detailed setup instructions
- Model options and pricing
- Troubleshooting common issues
- Security best practices
- How the fallback system works

## Cost Estimate
With typical journaling usage and `gpt-4o-mini`:
- **~$0.01-0.05 per day**
- Daily prompt: ~$0.0001
- Each probing question: ~$0.0003
- Entry ending: ~$0.0003
- Daily insights: ~$0.0005

Much cheaper than a coffee, and your thoughts are worth it! ☕✨
