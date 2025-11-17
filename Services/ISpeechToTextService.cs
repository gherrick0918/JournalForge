namespace JournalForge.Services;

public enum SpeechRecognitionMethod
{
    /// <summary>
    /// Uses Android's Intent-based speech recognition with Google's UI.
    /// Most reliable but shows a dialog.
    /// </summary>
    IntentBased,
    
    /// <summary>
    /// Uses Android's SpeechRecognizer service in the background.
    /// No UI but can be less reliable.
    /// </summary>
    ServiceBased,
    
    /// <summary>
    /// Automatically chooses the best available method.
    /// </summary>
    Auto
}

public interface ISpeechToTextService : IDisposable
{
    Task<bool> RequestPermissionsAsync();
    
    /// <summary>
    /// Start listening for speech using the specified method.
    /// </summary>
    /// <param name="method">The recognition method to use</param>
    /// <returns>The transcribed text</returns>
    Task<string> ListenAsync(SpeechRecognitionMethod method = SpeechRecognitionMethod.Auto);
    
    /// <summary>
    /// Stop listening (only applies to ServiceBased method)
    /// </summary>
    void StopListening();
    
    /// <summary>
    /// Check if a specific recognition method is available on this device
    /// </summary>
    bool IsMethodAvailable(SpeechRecognitionMethod method);
}
