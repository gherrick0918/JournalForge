using Android.Content;
using Android.Speech;
using JournalForge.Services;

namespace JournalForge.Platforms.Android;

public class SpeechToTextService : ISpeechToTextService
{
    private SpeechRecognitionListener? _listener;
    private SpeechRecognizer? _speechRecognizer;
    private TaskCompletionSource<string>? _tcs;

    public Task<bool> RequestPermissionsAsync()
    {
        return Task.FromResult(SpeechRecognizer.IsRecognitionAvailable(Platform.CurrentActivity));
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

        if (_speechRecognizer == null)
        {
            _speechRecognizer = SpeechRecognizer.CreateSpeechRecognizer(activity);
            _listener = new SpeechRecognitionListener(_tcs);
            _speechRecognizer.SetRecognitionListener(_listener);
        }

        var intent = new Intent(RecognizerIntent.ActionRecognizeSpeech);
        intent.PutExtra(RecognizerIntent.ExtraLanguageModel, RecognizerIntent.LanguageModelFreeForm);
        intent.PutExtra(RecognizerIntent.ExtraLanguage, Java.Util.Locale.Default);
        intent.PutExtra(RecognizerIntent.ExtraPartialResults, true);
        intent.PutExtra(RecognizerIntent.ExtraSpeechInputCompleteSilenceLengthMillis, 2000);
        intent.PutExtra(RecognizerIntent.ExtraSpeechInputPossiblyCompleteSilenceLengthMillis, 2000);

        _speechRecognizer.StartListening(intent);

        return _tcs.Task;
    }

    public void StopListening()
    {
        _speechRecognizer?.StopListening();
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
            var errorMessage = error switch
            {
                SpeechRecognizerError.NoMatch => "No speech detected. Please try again.",
                SpeechRecognizerError.NetworkTimeout => "Network timeout. Please check your connection.",
                SpeechRecognizerError.Network => "Network error. Please check your connection.",
                SpeechRecognizerError.Audio => "Audio recording error.",
                SpeechRecognizerError.Server => "Server error. Please try again.",
                SpeechRecognizerError.Client => "Client error.",
                SpeechRecognizerError.SpeechTimeout => "No speech detected. Please try again.",
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
