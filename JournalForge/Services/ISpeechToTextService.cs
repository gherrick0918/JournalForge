namespace JournalForge.Services;

public interface ISpeechToTextService : IDisposable
{
    Task<bool> RequestPermissionsAsync();
    Task<string> ListenAsync();
    void StopListening();
}
