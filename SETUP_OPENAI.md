# OpenAI API Setup Guide

This guide will help you configure your OpenAI API key for JournalForge.

## Quick Setup

### 1. Get Your OpenAI API Key

If you don't have an API key yet:
1. Go to [OpenAI Platform](https://platform.openai.com/api-keys)
2. Sign in or create an account
3. Click "Create new secret key"
4. Copy the key (it starts with `sk-`)

### 2. Configure the App

**Option A: Using local.properties (Recommended for Development)**

1. Navigate to the `android-app` directory
2. Open or create `local.properties` file
3. Add your API key:
   ```properties
   openai.api.key=sk-your-actual-api-key-here
   openai.model=gpt-4o-mini
   ```
4. Save the file

**Option B: Using Environment Variables (CI/CD)**

Set the following environment variables:
```bash
export OPENAI_API_KEY="sk-your-actual-api-key-here"
export OPENAI_MODEL="gpt-4o-mini"
```

### 3. Build the App

```bash
cd android-app
./gradlew assembleDebug
```

The API key will be securely injected into `BuildConfig` during the build process.

### 4. Verify Configuration

1. Install and run the app
2. Go to Settings (⚙️)
3. Check the "AI Configuration" section
4. You should see: **OpenAI: ✓ Configured**

## Security Notes

⚠️ **IMPORTANT**: Never commit your API key to version control!

- `local.properties` is in `.gitignore` - it won't be committed
- The API key is only stored in your local file and BuildConfig at build time
- For production builds, use environment variables or CI/CD secrets

## Troubleshooting

### "OpenAI: ✗ Not Configured"

This means the app is using mock responses. Check:
1. Did you add your API key to `local.properties`?
2. Did you rebuild the app after adding the key?
3. Is your API key valid and starts with `sk-`?

### API Errors

If you see API errors:
1. Verify your API key is active at [OpenAI Platform](https://platform.openai.com/account/api-keys)
2. Check you have credits/billing set up
3. Ensure your API key has the necessary permissions

### Mock Responses Instead of Real AI

If you're still seeing repetitive/generic responses:
1. Check the AI Configuration in Settings
2. Rebuild the app: `./gradlew clean assembleDebug`
3. Uninstall and reinstall the app
4. Check the logcat for API errors: `adb logcat | grep AIService`

## Available Models

You can configure different OpenAI models in `local.properties`:

| Model | Description | Cost |
|-------|-------------|------|
| gpt-4o-mini | Fast, affordable (default) | Lowest |
| gpt-4o | Latest, most capable | Medium |
| gpt-4 | Powerful, slower | Higher |
| gpt-3.5-turbo | Fast, older | Low |

Example:
```properties
openai.model=gpt-4o
```

## Testing Your Configuration

After setup, test the AI features:

1. **Daily Prompt** - Main screen should show a unique AI-generated prompt
2. **Probing Questions** - Write an entry and tap "Ask AI"
3. **Suggestions** - Tap "Suggest Ending" for AI-generated conclusions
4. **Insights** - Check the daily insight on the main screen

All responses should be contextual and varied, not repetitive.

## Need Help?

- Check application logs: `adb logcat | grep "AIService\|OpenAI"`
- Verify your OpenAI account status
- See [README.md](README.md) for more details
