# Quick Start: Enable OpenAI in 30 Seconds

## Step 1: Get Your API Key
Visit https://platform.openai.com/api-keys and create a new API key.

## Step 2: Create Local Config File

In the `JournalForge` project folder, create a file named `appsettings.local.json`:

```json
{
  "OpenAI": {
    "ApiKey": "sk-your-actual-api-key-here",
    "Model": "gpt-4o-mini"
  }
}
```

Or copy the example file:
```bash
cp appsettings.local.json.example appsettings.local.json
# Then edit appsettings.local.json with your API key
```

## Step 3: Build and Run
```bash
dotnet build -f net9.0-android
```

That's it! The app will automatically load your settings and use OpenAI for all AI features.

✅ **Works on Android!** The config file is packaged into your APK and stays private (automatically excluded from git).

## Verify It's Working
1. Go to "New Entry"
2. Write something like "I felt happy today"
3. Click "🤔 Probe Deeper"
4. You should see a highly contextual question about your specific content

## Why This Method?

**Perfect for Android packaging:**
- Config file is included in your APK at build time
- Each developer has their own private config file
- **Never committed to git** - your API key stays secure
- No need for environment variables that don't work in mobile apps

The `appsettings.local.json` file is automatically ignored by git, so you can't accidentally commit your API key!

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
