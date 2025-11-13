namespace JournalForge.Services;

public interface ISpeechToTextService
{
    Task<bool> RequestPermissionsAsync();
    Task<string> ListenAsync();
    void StopListening();
}
