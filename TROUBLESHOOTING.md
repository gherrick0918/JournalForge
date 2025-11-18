# JournalForge Troubleshooting Guide

## Google Sign-In Issues

### Error: "Developer error: Please ensure SHA-1 fingerprint is configured in Firebase Console"

This is the **most common** Google Sign-In error. It means your app's signing certificate isn't registered in Firebase.

**Solution:**
👉 See [FIREBASE_SETUP_GUIDE.md](FIREBASE_SETUP_GUIDE.md) for complete step-by-step instructions.

**Quick Fix:**
1. Get your SHA-1 fingerprint:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore \
     -alias androiddebugkey -storepass android -keypass android
   ```
2. Add it to Firebase Console → Project Settings → Your apps → Add fingerprint
3. Wait 5-10 minutes for changes to propagate
4. Rebuild and test

### Error: "Configuration error: Please check your Firebase setup"

**Cause:** Issue with `google-services.json` file

**Solution:**
1. Download the latest `google-services.json` from Firebase Console
2. Replace the file at `android-app/app/google-services.json`
3. Rebuild the app: `./gradlew clean assembleDebug`

### Error: "Network error: Please check your internet connection"

**Cause:** No internet connection or network restrictions

**Solutions:**
- Check WiFi/mobile data connection
- Try switching networks
- Check if firewall is blocking Google services
- Verify you can access google.com in a browser

### Google Sign-In Not Working After Following All Steps

**Checklist:**
- [ ] SHA-1 fingerprint added to Firebase Console
- [ ] Google Sign-In enabled in Firebase Authentication
- [ ] Project support email set in Firebase
- [ ] Waited 5-10 minutes after making changes
- [ ] Rebuilt app after configuration changes
- [ ] Using correct Firebase project (`journalforgeapp`)
- [ ] Package name matches: `com.journalforge.app`

For detailed setup instructions, see [FIREBASE_SETUP_GUIDE.md](FIREBASE_SETUP_GUIDE.md).

---

## Speech-to-Text Issues

### "No Speech Detected" Error

If you're experiencing "no speech detected" when trying to use voice dictation, try these solutions:

#### Quick Fixes
1. **Check Microphone Permission**
   - Go to Settings > Apps > JournalForge > Permissions
   - Ensure Microphone permission is granted
   - If denied, enable it and restart the app

2. **Speak Clearly and Loudly**
   - Speak directly into your phone's microphone
   - Speak louder than normal conversation volume
   - Reduce background noise
   - Hold the phone 6-12 inches from your mouth

3. **Update Google App**
   - Speech recognition uses Google's service
   - Go to Play Store > My Apps & Games
   - Update the "Google" app to the latest version
   - Restart your device after updating

4. **Check Internet Connection**
   - Speech recognition requires internet for best results
   - Connect to WiFi or mobile data
   - Test your connection in a browser

#### Advanced Solutions

##### For Pixel Devices (Pixel 10 Pro XL and others)
1. **Clear Google App Cache**
   - Settings > Apps > Google > Storage
   - Tap "Clear Cache" (NOT Clear Data)
   - Restart the app

2. **Check Google Speech Services**
   - Settings > Apps > Show system apps
   - Find "Google Speech Services"
   - Ensure it's enabled and updated
   - Clear cache if needed

3. **Disable Battery Optimization**
   - Settings > Battery > Battery Optimization
   - Find JournalForge
   - Select "Don't optimize"

##### Alternative Speech Recognition Methods
The app automatically tries different speech recognition methods:
- **Intent-Based (Default)**: Uses Google's speech UI dialog
  - Most reliable on real devices
  - Shows a Google speech recognition popup
  - Recommended for most users
  
- **Service-Based (Fallback)**: Background recognition
  - No UI popup
  - May be less reliable on some devices
  - Used automatically if Intent-based fails

#### Device-Specific Issues

##### Samsung Devices
- Some Samsung devices require Bixby Voice to be set up first
- Go to Settings > Apps > Bixby Voice
- Complete initial setup if prompted

##### OnePlus/Oppo Devices
- May need to grant "Display over other apps" permission
- Settings > Apps > JournalForge > Display over other apps
- Enable permission

##### Xiaomi/MIUI Devices
- MIUI has aggressive battery optimization
- Settings > Battery & Performance > App Battery Saver
- Find JournalForge and disable battery optimization
- Also check Settings > Permissions > Autostart

#### Still Not Working?

If speech recognition still doesn't work after trying all the above:

1. **Restart Your Device**
   - Often resolves service connection issues

2. **Test with Other Apps**
   - Try Google Assistant (long-press home button)
   - Try voice typing in Google Keep or Messages
   - If these don't work, it's a device/Google services issue

3. **Check Google Services**
   - Go to Settings > Google > Google Services
   - Ensure all services are enabled

4. **Factory Reset Google App**
   - Settings > Apps > Google > Storage
   - Tap "Clear Data" (this resets Google app)
   - Sign back in and retry

5. **Use Keyboard Instead**
   - JournalForge works great with manual typing
   - Use the message input field to type your thoughts

### Other Common Issues

#### App Crashes on Startup
- Clear app cache: Settings > Apps > JournalForge > Storage > Clear Cache
- Ensure you have the latest version
- Restart your device

#### Entries Not Saving
- Check available storage space
- Ensure app has storage permission
- Try restarting the app

#### AI Features Not Working
- AI features require OpenAI API key (optional)
- Configure your API key in `AIService.kt`
- App works without AI using fallback responses

## Getting More Help

If you're still experiencing issues:

1. **Check Debug Logs**
   - Connect device to computer
   - Use `adb logcat -s "JournalForge:*"` to view logs
   - Look for speech recognition error messages

2. **Report an Issue**
   - Go to GitHub repository
   - Create a new issue with:
     - Device model and Android version
     - Steps to reproduce the problem
     - Error messages if any
     - Screenshot of the issue

3. **Community Support**
   - Check existing GitHub issues
   - Ask in discussions

## Diagnostic Information

When reporting issues, please include:
- Device manufacturer and model (e.g., "Google Pixel 10 Pro XL")
- Android version (e.g., "Android 14")
- JournalForge version (found in app settings)
- Whether speech recognition works in other apps
- Whether you have Google app installed and updated
- Internet connection type (WiFi/Mobile Data/None)

---

**Note**: Speech recognition requires Google services and internet connectivity. Some features may not work in regions where Google services are restricted or on devices without Google Play Services.
