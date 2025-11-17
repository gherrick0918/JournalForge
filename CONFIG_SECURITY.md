# Configuration Security - How Your API Key Stays Safe

This document explains how JournalForge keeps your OpenAI API key secure and why it will never be committed to git.

## The Problem We Solved

1. **Environment Variables Don't Work on Android**: Environment variables set on your dev machine don't transfer to the Android APK
2. **Can't Hardcode Keys**: Hardcoding API keys in source code gets them committed to git where OpenAI detects and disables them
3. **Need Per-Developer Config**: Each team member needs their own API key without affecting others

## The Solution: Local Configuration Files

### What We Did

Created a secure configuration system using local JSON files:

```
appsettings.json              ← Template (checked into git, no secrets)
appsettings.local.json        ← YOUR config (excluded from git, has your API key)
appsettings.local.json.example ← Example to copy (checked into git, fake key)
```

### How It Works

1. **You create** `appsettings.local.json` with your real API key
2. **At build time**, MAUI packages it into your APK as an embedded asset
3. **At runtime**, the app reads from the packaged file
4. **Git ignores it** - the file is listed in `.gitignore` so it never gets committed

### Why This Is Secure

#### Multiple Layers of Protection

1. **`.gitignore` Entries**:
   ```
   **/appsettings.local.json      # Your specific file
   **/appsettings.*.local.json    # Any variation
   **/*secret*.json               # Anything with "secret" in name
   **/*key*.json                  # Anything with "key" in name
   ```

2. **File Naming Convention**: The `.local.json` extension is universally recognized as "do not commit"

3. **Template System**: 
   - `appsettings.json` (in git) has empty API key
   - `appsettings.local.json` (not in git) has your real key
   - App prefers local over template

4. **No Source Code Changes**: Your API key never appears in `.cs` files that could be committed

#### For Android Specifically

- **Build-time packaging**: Config is included when you build the APK on your machine
- **Not runtime loading**: No need for environment variables or external files
- **Per-build isolation**: Each APK build includes the config from that developer's machine
- **Distribution safety**: If you share the APK, it has your key, but if you share the source code, it doesn't

### What Gets Committed to Git

✅ **Safe to commit:**
- `appsettings.json` - Template with empty API key
- `appsettings.local.json.example` - Example with fake key
- `ConfigurationService.cs` - Code that reads config
- `.gitignore` - Rules that exclude local config

❌ **Never committed:**
- `appsettings.local.json` - Your actual config with real API key
- Any file matching `*.local.json`
- Any file with "secret" or "key" in the name

### Developer Workflow

**First Time Setup:**
```bash
# Clone the repo
git clone https://github.com/your-repo/JournalForge.git
cd JournalForge/JournalForge

# Create your private config
cp appsettings.local.json.example appsettings.local.json

# Edit with your API key
nano appsettings.local.json  # or use your favorite editor

# Build and run
dotnet build -f net9.0-android
```

**Daily Development:**
```bash
# Pull updates - your local config is never affected
git pull

# Build - your config is automatically included
dotnet build -f net9.0-android

# Your appsettings.local.json stays on your machine, never pushed
git push  # Only pushes code changes, not your config
```

**Sharing with Team:**
- They clone the repo (no API key included)
- They create their own `appsettings.local.json` with their own key
- Everyone works independently with their own config

### Verification

To verify your setup is secure:

```bash
# This should show your local config file exists
ls -la appsettings.local.json

# This should NOT list your local config (it's ignored)
git status

# This should NOT show your local config
git ls-files | grep appsettings

# This should show it's ignored
git check-ignore -v appsettings.local.json
```

Expected output of last command:
```
.gitignore:57:**/appsettings.local.json    appsettings.local.json
```

### What If I Accidentally Try to Commit It?

Git will ignore it! Even if you do:
```bash
git add appsettings.local.json
```

Git responds:
```
The following paths are ignored by one of your .gitignore files:
appsettings.local.json
```

### For Production Deployment

If you're deploying to a store or enterprise:

1. **App Store Builds**: Use a dedicated API key for production builds, configure it in your CI/CD pipeline
2. **Enterprise Distribution**: Each deployment environment has its own config file
3. **Never in Source Control**: Production keys stay in build systems, not in git

### Summary

✅ Your API key is safe because:
- It's in a file that's explicitly excluded from git
- Multiple .gitignore patterns prevent accidental commits
- OpenAI won't find it in your public repo
- Each developer has their own private copy
- Works perfectly with Android APK packaging

✅ You can safely:
- Push code changes to GitHub
- Share the repository publicly
- Collaborate with teammates
- Build and distribute Android APKs

❌ Your API key will NEVER:
- Appear in git history
- Be pushed to GitHub
- Be visible in public repos
- Be detected by OpenAI's scanners

## Questions?

See `OPENAI_SETUP.md` for setup instructions or `QUICK_START_OPENAI.md` for the 30-second version.
