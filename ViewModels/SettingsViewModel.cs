using System.Windows.Input;
using JournalForge.Services;

namespace JournalForge.ViewModels;

public class SettingsViewModel : BaseViewModel
{
    private readonly IGoogleAuthService _authService;
    private readonly ICloudSyncService _syncService;
    private bool _isSignedIn = false;
    private string _userEmail = string.Empty;
    private DateTime? _lastSyncTime;

    public SettingsViewModel(
        IGoogleAuthService authService,
        ICloudSyncService syncService)
    {
        _authService = authService;
        _syncService = syncService;

        Title = "Settings";

        SignInCommand = new Command(async () => await SignInAsync(), () => !IsBusy);
        SignOutCommand = new Command(async () => await SignOutAsync(), () => !IsBusy);
        SyncNowCommand = new Command(async () => await SyncNowAsync(), () => !IsBusy);

        // Subscribe to auth state changes
        _authService.AuthenticationStateChanged += OnAuthStateChanged;

        // Load initial state
        Task.Run(async () => await LoadAuthStateAsync());
    }

    public bool IsSignedIn
    {
        get => _isSignedIn;
        set => SetProperty(ref _isSignedIn, value);
    }

    public bool IsNotSignedIn => !IsSignedIn;

    public bool IsNotBusy => !IsBusy;

    public string UserEmail
    {
        get => _userEmail;
        set => SetProperty(ref _userEmail, value);
    }

    public string LastSyncText
    {
        get
        {
            if (_lastSyncTime == null)
                return "Never synced";
            
            var timeSpan = DateTime.Now - _lastSyncTime.Value;
            
            if (timeSpan.TotalMinutes < 1)
                return "Last synced: Just now";
            if (timeSpan.TotalMinutes < 60)
                return $"Last synced: {(int)timeSpan.TotalMinutes} minutes ago";
            if (timeSpan.TotalHours < 24)
                return $"Last synced: {(int)timeSpan.TotalHours} hours ago";
            
            return $"Last synced: {_lastSyncTime.Value:MMM dd, yyyy}";
        }
    }

    public ICommand SignInCommand { get; }
    public ICommand SignOutCommand { get; }
    public ICommand SyncNowCommand { get; }

    private async Task LoadAuthStateAsync()
    {
        try
        {
            IsSignedIn = await _authService.IsSignedInAsync();
            
            if (IsSignedIn)
            {
                var user = await _authService.GetCurrentUserAsync();
                UserEmail = user?.Email ?? "Unknown user";
                _lastSyncTime = await _syncService.GetLastSyncTimeAsync();
                OnPropertyChanged(nameof(LastSyncText));
            }
            
            OnPropertyChanged(nameof(IsNotSignedIn));
            OnPropertyChanged(nameof(IsNotBusy));
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"Error loading auth state: {ex.Message}");
        }
    }

    private async Task SignInAsync()
    {
        if (IsBusy) return;

        try
        {
            IsBusy = true;

            var success = await _authService.SignInAsync();
            
            if (success)
            {
                await LoadAuthStateAsync();
                await Application.Current?.MainPage?.DisplayAlert(
                    "Success", 
                    "Successfully signed in with Google! Your entries will now be backed up to the cloud.", 
                    "OK")!;
                
                // Trigger initial sync
                await SyncNowAsync();
            }
            else
            {
                await Application.Current?.MainPage?.DisplayAlert(
                    "Sign In Required", 
                    "Google Sign-In is not yet fully configured for this app.\n\n" +
                    "To enable Google Sign-In:\n" +
                    "1. Set up a Google Cloud project\n" +
                    "2. Enable Google Sign-In API\n" +
                    "3. Configure OAuth credentials\n" +
                    "4. Add Firebase SDK or Google Sign-In SDK\n\n" +
                    "For now, your entries are safely stored on this device.", 
                    "OK")!;
            }
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Error", 
                $"Failed to sign in: {ex.Message}", 
                "OK")!;
        }
        finally
        {
            IsBusy = false;
            OnPropertyChanged(nameof(IsNotBusy));
            ((Command)SignInCommand).ChangeCanExecute();
            ((Command)SignOutCommand).ChangeCanExecute();
            ((Command)SyncNowCommand).ChangeCanExecute();
        }
    }

    private async Task SignOutAsync()
    {
        if (IsBusy) return;

        try
        {
            var confirm = await Application.Current?.MainPage?.DisplayAlert(
                "Sign Out", 
                "Are you sure you want to sign out? Your entries will remain on this device but won't sync to the cloud.", 
                "Sign Out", 
                "Cancel")!;

            if (!confirm) return;

            IsBusy = true;

            await _authService.SignOutAsync();
            await LoadAuthStateAsync();
            
            await Application.Current?.MainPage?.DisplayAlert(
                "Signed Out", 
                "You have been signed out. Your entries are still available on this device.", 
                "OK")!;
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Error", 
                $"Failed to sign out: {ex.Message}", 
                "OK")!;
        }
        finally
        {
            IsBusy = false;
            OnPropertyChanged(nameof(IsNotBusy));
            ((Command)SignInCommand).ChangeCanExecute();
            ((Command)SignOutCommand).ChangeCanExecute();
            ((Command)SyncNowCommand).ChangeCanExecute();
        }
    }

    private async Task SyncNowAsync()
    {
        if (IsBusy) return;

        if (!IsSignedIn)
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Not Signed In", 
                "Please sign in with Google to sync your entries.", 
                "OK")!;
            return;
        }

        try
        {
            IsBusy = true;

            var success = await _syncService.SyncEntriesAsync();
            
            if (success)
            {
                _lastSyncTime = await _syncService.GetLastSyncTimeAsync();
                OnPropertyChanged(nameof(LastSyncText));
                
                await Application.Current?.MainPage?.DisplayAlert(
                    "Sync Complete", 
                    "Your entries have been synced with the cloud.", 
                    "OK")!;
            }
            else
            {
                await Application.Current?.MainPage?.DisplayAlert(
                    "Sync Info", 
                    "Cloud sync functionality is being prepared. Your entries are safely stored on this device.", 
                    "OK")!;
            }
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Error", 
                $"Failed to sync: {ex.Message}", 
                "OK")!;
        }
        finally
        {
            IsBusy = false;
            OnPropertyChanged(nameof(IsNotBusy));
            ((Command)SignInCommand).ChangeCanExecute();
            ((Command)SignOutCommand).ChangeCanExecute();
            ((Command)SyncNowCommand).ChangeCanExecute();
        }
    }

    private void OnAuthStateChanged(object? sender, bool isSignedIn)
    {
        Task.Run(async () => await LoadAuthStateAsync());
    }
}
