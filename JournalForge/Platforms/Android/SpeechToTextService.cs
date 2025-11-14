using Android.Content;
using Android.Speech;
using JournalForge.Services;

namespace JournalForge.Platforms.Android;

public class SpeechToTextService : ISpeechToTextService
{
    private SpeechRecognitionListener? _listener;
    private SpeechRecognizer? _speechRecognizer;
    private TaskCompletionSource<string>? _tcs;

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

    public Task<string> ListenAsync()
    {
        _tcs = new TaskCompletionSource<string>();

        var activity = Platform.CurrentActivity;
        if (activity == null)
        {
            _tcs.SetException(new InvalidOperationException("Activity not available"));
            return _tcs.Task;
        }

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
        // Significantly increased silence times to give users more time to speak
        // This helps with longer pauses and reduces "no speech detected" errors
        // These values are especially important for emulators where audio input may have delays
        intent.PutExtra(RecognizerIntent.ExtraSpeechInputCompleteSilenceLengthMillis, 8000);
        intent.PutExtra(RecognizerIntent.ExtraSpeechInputPossiblyCompleteSilenceLengthMillis, 5000);
        intent.PutExtra(RecognizerIntent.ExtraSpeechInputMinimumLengthMillis, 10000);
        intent.PutExtra(RecognizerIntent.ExtraMaxResults, 5);
        // Prefer online for better accuracy, but allow offline fallback
        intent.PutExtra(RecognizerIntent.ExtraPreferOffline, false);
        // Add prompt for better user experience
        intent.PutExtra(RecognizerIntent.ExtraPrompt, "Speak now...");
        // Enable secure on-device recognition when available
        intent.PutExtra(RecognizerIntent.ExtraSecureOnDevice, false);

        try
        {
            _speechRecognizer.StartListening(intent);
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"SpeechToTextService.ListenAsync error: {ex.Message}");
            _tcs.TrySetException(new Exception($"Failed to start speech recognition: {ex.Message}"));
        }

        return _tcs.Task;
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
}
