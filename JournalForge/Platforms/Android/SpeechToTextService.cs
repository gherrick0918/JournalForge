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
        intent.PutExtra(RecognizerIntent.ExtraSpeechInputCompleteSilenceLengthMillis, 5000);
        intent.PutExtra(RecognizerIntent.ExtraSpeechInputPossiblyCompleteSilenceLengthMillis, 3000);
        intent.PutExtra(RecognizerIntent.ExtraSpeechInputMinimumLengthMillis, 8000);
        intent.PutExtra(RecognizerIntent.ExtraMaxResults, 5);
        // Prefer online for better accuracy
        intent.PutExtra(RecognizerIntent.ExtraPreferOffline, false);

        _speechRecognizer.StartListening(intent);

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
        }

        public void OnBufferReceived(byte[]? buffer)
        {
        }

        public void OnEndOfSpeech()
        {
        }

        public void OnError(SpeechRecognizerError error)
        {
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
                SpeechRecognizerError.Audio => "Audio recording error.",
                SpeechRecognizerError.Server => "Server error. Please try again.",
                SpeechRecognizerError.Client => "Client error.",
                SpeechRecognizerError.RecognizerBusy => "Speech recognizer is busy.",
                SpeechRecognizerError.InsufficientPermissions => "Microphone permission required.",
                _ => "Speech recognition error."
            };
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
        }

        public void OnResults(global::Android.OS.Bundle? results)
        {
            if (results == null)
            {
                _tcs.TrySetResult(string.Empty);
                return;
            }

            var matches = results.GetStringArrayList(SpeechRecognizer.ResultsRecognition);
            if (matches != null && matches.Count > 0)
            {
                var text = matches[0] ?? string.Empty;
                _tcs.TrySetResult(text);
            }
            else
            {
                _tcs.TrySetResult(string.Empty);
            }
        }

        public void OnRmsChanged(float rmsdB)
        {
        }
    }
}
