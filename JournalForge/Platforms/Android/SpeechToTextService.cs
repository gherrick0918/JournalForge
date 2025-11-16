using Android.App;
using Android.Content;
using Android.Speech;
using JournalForge.Services;

namespace JournalForge.Platforms.Android;

public class SpeechToTextService : ISpeechToTextService
{
    private SpeechRecognitionListener? _listener;
    private SpeechRecognizer? _speechRecognizer;
    private TaskCompletionSource<string>? _tcs;
    private static TaskCompletionSource<string>? _intentTcs;
    private const int SPEECH_REQUEST_CODE = 1234;

    public async Task<bool> RequestPermissionsAsync()
    {
        try
        {
            var status = await Permissions.CheckStatusAsync<Permissions.Microphone>();
            
            if (status != PermissionStatus.Granted)
            {
                status = await Permissions.RequestAsync<Permissions.Microphone>();
            }

            if (status != PermissionStatus.Granted)
            {
                return false;
            }

            return SpeechRecognizer.IsRecognitionAvailable(Platform.CurrentActivity);
        }
        catch
        {
            return false;
        }
    }

    public bool IsMethodAvailable(SpeechRecognitionMethod method)
    {
        var activity = Platform.CurrentActivity;
        if (activity == null)
            return false;

        switch (method)
        {
            case SpeechRecognitionMethod.IntentBased:
                // Check if speech recognition intent is available
                var intent = new Intent(RecognizerIntent.ActionRecognizeSpeech);
                var packageManager = activity.PackageManager;
                return packageManager?.QueryIntentActivities(intent, 0)?.Count > 0;
                
            case SpeechRecognitionMethod.ServiceBased:
                return SpeechRecognizer.IsRecognitionAvailable(activity);
                
            case SpeechRecognitionMethod.Auto:
                return IsMethodAvailable(SpeechRecognitionMethod.IntentBased) || 
                       IsMethodAvailable(SpeechRecognitionMethod.ServiceBased);
                
            default:
                return false;
        }
    }

    public Task<string> ListenAsync(SpeechRecognitionMethod method = SpeechRecognitionMethod.Auto)
    {
        var activity = Platform.CurrentActivity;
        if (activity == null)
        {
            return Task.FromException<string>(new InvalidOperationException("Activity not available"));
        }

        // Determine which method to use
        SpeechRecognitionMethod actualMethod = method;
        if (method == SpeechRecognitionMethod.Auto)
        {
            // Prefer Intent-based as it's more reliable
            if (IsMethodAvailable(SpeechRecognitionMethod.IntentBased))
            {
                actualMethod = SpeechRecognitionMethod.IntentBased;
                System.Diagnostics.Debug.WriteLine("SpeechToTextService: Auto-selected IntentBased method");
            }
            else if (IsMethodAvailable(SpeechRecognitionMethod.ServiceBased))
            {
                actualMethod = SpeechRecognitionMethod.ServiceBased;
                System.Diagnostics.Debug.WriteLine("SpeechToTextService: Auto-selected ServiceBased method");
            }
            else
            {
                return Task.FromException<string>(new InvalidOperationException("No speech recognition method available"));
            }
        }

        // Use the determined method
        return actualMethod == SpeechRecognitionMethod.IntentBased 
            ? ListenWithIntentAsync(activity) 
            : ListenWithServiceAsync(activity);
    }

    private Task<string> ListenWithIntentAsync(Activity activity)
    {
        System.Diagnostics.Debug.WriteLine("SpeechToTextService: Starting Intent-based recognition");
        
        _intentTcs = new TaskCompletionSource<string>();

        try
        {
            var intent = new Intent(RecognizerIntent.ActionRecognizeSpeech);
            intent.PutExtra(RecognizerIntent.ExtraLanguageModel, RecognizerIntent.LanguageModelFreeForm);
            intent.PutExtra(RecognizerIntent.ExtraLanguage, Java.Util.Locale.Default);
            intent.PutExtra(RecognizerIntent.ExtraPrompt, "Speak your journal entry...");
            intent.PutExtra(RecognizerIntent.ExtraMaxResults, 1);
            // Don't prefer offline - let Google handle it for better reliability on real devices
            intent.PutExtra(RecognizerIntent.ExtraPreferOffline, false);

            // Register for result callback
            SpeechResultListener.Initialize(_intentTcs);
            
            activity.StartActivityForResult(intent, SPEECH_REQUEST_CODE);
            
            System.Diagnostics.Debug.WriteLine("SpeechToTextService: Intent-based recognition started");
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"SpeechToTextService: Intent-based recognition error: {ex.Message}");
            _intentTcs.TrySetException(new Exception($"Failed to start speech recognition: {ex.Message}"));
        }

        return _intentTcs.Task;
    }

    private Task<string> ListenWithServiceAsync(Activity activity)
    {
        System.Diagnostics.Debug.WriteLine("SpeechToTextService: Starting Service-based recognition");
        
        _tcs = new TaskCompletionSource<string>();

        try
        {
            // Dispose of the old recognizer and create a fresh one to avoid state issues
            if (_speechRecognizer != null)
            {
                _speechRecognizer.Destroy();
                _speechRecognizer = null;
            }

            _speechRecognizer = SpeechRecognizer.CreateSpeechRecognizer(activity);
            _listener = new SpeechRecognitionListener(_tcs);
            _speechRecognizer.SetRecognitionListener(_listener);

            var intent = new Intent(RecognizerIntent.ActionRecognizeSpeech);
            intent.PutExtra(RecognizerIntent.ExtraLanguageModel, RecognizerIntent.LanguageModelFreeForm);
            intent.PutExtra(RecognizerIntent.ExtraLanguage, Java.Util.Locale.Default);
            intent.PutExtra(RecognizerIntent.ExtraPartialResults, true);
            // Increased silence times for better user experience
            intent.PutExtra(RecognizerIntent.ExtraSpeechInputCompleteSilenceLengthMillis, 8000);
            intent.PutExtra(RecognizerIntent.ExtraSpeechInputPossiblyCompleteSilenceLengthMillis, 5000);
            intent.PutExtra(RecognizerIntent.ExtraSpeechInputMinimumLengthMillis, 10000);
            intent.PutExtra(RecognizerIntent.ExtraMaxResults, 5);
            // For service-based, try online first for real devices
            intent.PutExtra(RecognizerIntent.ExtraPreferOffline, false);

            _speechRecognizer.StartListening(intent);
            System.Diagnostics.Debug.WriteLine("SpeechToTextService: Service-based recognition started");
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"SpeechToTextService: Service-based recognition error: {ex.Message}");
            _tcs.TrySetException(new Exception($"Failed to start speech recognition: {ex.Message}"));
        }

        return _tcs.Task;
    }

    // Static method to handle activity result for Intent-based recognition
    public static void HandleActivityResult(int requestCode, Result resultCode, Intent? data)
    {
        if (requestCode != SPEECH_REQUEST_CODE || _intentTcs == null)
            return;

        System.Diagnostics.Debug.WriteLine($"SpeechToTextService: HandleActivityResult called with resultCode: {resultCode}");

        try
        {
            if (resultCode == Result.Ok && data != null)
            {
                var matches = data.GetStringArrayListExtra(RecognizerIntent.ExtraResults);
                if (matches != null && matches.Count > 0)
                {
                    var text = matches[0] ?? string.Empty;
                    System.Diagnostics.Debug.WriteLine($"SpeechToTextService: Intent recognition successful: '{text}'");
                    _intentTcs.TrySetResult(text);
                }
                else
                {
                    System.Diagnostics.Debug.WriteLine("SpeechToTextService: No matches in intent result");
                    _intentTcs.TrySetResult(string.Empty);
                }
            }
            else
            {
                System.Diagnostics.Debug.WriteLine($"SpeechToTextService: Intent recognition cancelled or failed");
                _intentTcs.TrySetResult(string.Empty);
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"SpeechToTextService: Error processing intent result: {ex.Message}");
            _intentTcs.TrySetException(ex);
        }
        finally
        {
            _intentTcs = null;
        }
    }

    public void StopListening()
    {
        try
        {
            _speechRecognizer?.StopListening();
        }
        catch (System.Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"SpeechToTextService.StopListening error: {ex.Message}");
        }
    }

    public void Dispose()
    {
        try
        {
            if (_speechRecognizer != null)
            {
                _speechRecognizer.Destroy();
                _speechRecognizer.Dispose();
                _speechRecognizer = null;
            }

            if (_listener != null)
            {
                _listener.Dispose();
                _listener = null;
            }
        }
        catch (System.Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"SpeechToTextService.Dispose error: {ex.Message}");
        }
    }

    private class SpeechRecognitionListener : Java.Lang.Object, IRecognitionListener
    {
        private readonly TaskCompletionSource<string> _tcs;

        public SpeechRecognitionListener(TaskCompletionSource<string> tcs)
        {
            _tcs = tcs;
        }

        public void OnBeginningOfSpeech()
        {
            System.Diagnostics.Debug.WriteLine("SpeechRecognitionListener.OnBeginningOfSpeech - User has started speaking");
        }

        public void OnBufferReceived(byte[]? buffer)
        {
            System.Diagnostics.Debug.WriteLine($"SpeechRecognitionListener.OnBufferReceived - Received audio buffer");
        }

        public void OnEndOfSpeech()
        {
            System.Diagnostics.Debug.WriteLine("SpeechRecognitionListener.OnEndOfSpeech - User has stopped speaking");
        }

        public void OnError(SpeechRecognizerError error)
        {
            System.Diagnostics.Debug.WriteLine($"SpeechRecognitionListener.OnError: {error}");
            
            // For NoMatch and SpeechTimeout, return empty string instead of error
            // This allows the UI to show a friendlier message
            if (error == SpeechRecognizerError.NoMatch || error == SpeechRecognizerError.SpeechTimeout)
            {
                _tcs.TrySetResult(string.Empty);
                return;
            }
            
            // Handle ServerDisconnected - common on emulators
            // Return empty string so user gets the "no speech detected" message
            if (error == SpeechRecognizerError.ServerDisconnected)
            {
                System.Diagnostics.Debug.WriteLine("ServerDisconnected - this is common on emulators. Speech recognition may need device setup.");
                _tcs.TrySetResult(string.Empty);
                return;
            }
            
            var errorMessage = error switch
            {
                SpeechRecognizerError.NetworkTimeout => "Network timeout. Please check your connection.",
                SpeechRecognizerError.Network => "Network error. Please check your connection.",
                SpeechRecognizerError.Audio => "Audio recording error. Please check your microphone.",
                SpeechRecognizerError.Server => "Server error. Please try again.",
                SpeechRecognizerError.Client => "Client error.",
                SpeechRecognizerError.RecognizerBusy => "Speech recognizer is busy. Please wait and try again.",
                SpeechRecognizerError.InsufficientPermissions => "Microphone permission required.",
                _ => $"Speech recognition error: {error}."
            };
            System.Diagnostics.Debug.WriteLine($"SpeechRecognitionListener error message: {errorMessage}");
            _tcs.TrySetException(new Exception(errorMessage));
        }

        public void OnEvent(int eventType, global::Android.OS.Bundle? @params)
        {
        }

        public void OnPartialResults(global::Android.OS.Bundle? partialResults)
        {
        }

        public void OnReadyForSpeech(global::Android.OS.Bundle? @params)
        {
            System.Diagnostics.Debug.WriteLine("SpeechRecognitionListener.OnReadyForSpeech - Ready to listen");
        }

        public void OnResults(global::Android.OS.Bundle? results)
        {
            System.Diagnostics.Debug.WriteLine("SpeechRecognitionListener.OnResults - Received final results");
            if (results == null)
            {
                System.Diagnostics.Debug.WriteLine("SpeechRecognitionListener.OnResults - Results bundle is null");
                _tcs.TrySetResult(string.Empty);
                return;
            }

            var matches = results.GetStringArrayList(SpeechRecognizer.ResultsRecognition);
            if (matches != null && matches.Count > 0)
            {
                var text = matches[0] ?? string.Empty;
                System.Diagnostics.Debug.WriteLine($"SpeechRecognitionListener.OnResults - Transcribed: '{text}'");
                _tcs.TrySetResult(text);
            }
            else
            {
                System.Diagnostics.Debug.WriteLine("SpeechRecognitionListener.OnResults - No matches found");
                _tcs.TrySetResult(string.Empty);
            }
        }

        public void OnRmsChanged(float rmsdB)
        {
            // Log periodically to show audio is being received
            // Only log every ~0.5 seconds to avoid spam (RMS updates frequently)
            if (DateTime.Now.Millisecond < 100)
            {
                System.Diagnostics.Debug.WriteLine($"SpeechRecognitionListener.OnRmsChanged - Audio level: {rmsdB} dB");
            }
        }
    }

    // Helper class to manage Intent-based speech recognition callback
    internal static class SpeechResultListener
    {
        private static TaskCompletionSource<string>? _currentTcs;

        public static void Initialize(TaskCompletionSource<string> tcs)
        {
            _currentTcs = tcs;
        }

        public static TaskCompletionSource<string>? GetCurrentTcs()
        {
            return _currentTcs;
        }

        public static void Clear()
        {
            _currentTcs = null;
        }
    }
}
