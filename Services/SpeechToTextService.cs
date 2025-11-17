namespace JournalForge.Services;

// Default implementation for platforms without speech-to-text support
public class SpeechToTextService : ISpeechToTextService
{
    public Task<bool> RequestPermissionsAsync()
    {
        return Task.FromResult(false);
    }

    public Task<string> ListenAsync(SpeechRecognitionMethod method = SpeechRecognitionMethod.Auto)
    {
        throw new PlatformNotSupportedException("Speech-to-text is not supported on this platform.");
    }

    public void StopListening()
    {
        // No-op for unsupported platforms
    }

    public bool IsMethodAvailable(SpeechRecognitionMethod method)
    {
        return false;
    }

    public void Dispose()
    {
        // No-op for unsupported platforms
    }
}
