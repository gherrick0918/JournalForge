using System.Collections.ObjectModel;
using System.Windows.Input;
using JournalForge.Models;
using JournalForge.Services;

namespace JournalForge.ViewModels;

public class TimeCapsuleViewModel : BaseViewModel
{
    private readonly ITimeCapsuleService _timeCapsuleService;
    private readonly IJournalEntryService _journalService;
    private ObservableCollection<TimeCapsule> _capsules = new();
    private TimeCapsule? _selectedCapsule;
    private bool _isCreatingNew;
    private string _newCapsuleTitle = string.Empty;
    private string _newCapsuleMessage = string.Empty;
    private DateTime _unsealDate = DateTime.Now.AddMonths(1);

    public TimeCapsuleViewModel(
        ITimeCapsuleService timeCapsuleService,
        IJournalEntryService journalService)
    {
        _timeCapsuleService = timeCapsuleService;
        _journalService = journalService;

        Title = "Time Capsule";

        CreateNewCommand = new Command(() => IsCreatingNew = true);
        SealCapsuleCommand = new Command(async () => await SealCapsuleAsync());
        UnsealCommand = new Command<TimeCapsule>(async (capsule) => await UnsealCapsuleAsync(capsule));
        CancelCommand = new Command(() => IsCreatingNew = false);
        RefreshCommand = new Command(async () => await LoadCapsulesAsync());

        Task.Run(async () => await LoadCapsulesAsync());
    }

    public ObservableCollection<TimeCapsule> Capsules
    {
        get => _capsules;
        set => SetProperty(ref _capsules, value);
    }

    public TimeCapsule? SelectedCapsule
    {
        get => _selectedCapsule;
        set => SetProperty(ref _selectedCapsule, value);
    }

    public bool IsCreatingNew
    {
        get => _isCreatingNew;
        set => SetProperty(ref _isCreatingNew, value);
    }

    public string NewCapsuleTitle
    {
        get => _newCapsuleTitle;
        set => SetProperty(ref _newCapsuleTitle, value);
    }

    public string NewCapsuleMessage
    {
        get => _newCapsuleMessage;
        set => SetProperty(ref _newCapsuleMessage, value);
    }

    public DateTime UnsealDate
    {
        get => _unsealDate;
        set => SetProperty(ref _unsealDate, value);
    }

    public ICommand CreateNewCommand { get; }
    public ICommand SealCapsuleCommand { get; }
    public ICommand UnsealCommand { get; }
    public ICommand CancelCommand { get; }
    public ICommand RefreshCommand { get; }

    private async Task LoadCapsulesAsync()
    {
        if (IsBusy) return;

        try
        {
            IsBusy = true;
            var capsules = await _timeCapsuleService.GetAllCapsulesAsync();
            Capsules = new ObservableCollection<TimeCapsule>(capsules);
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert("Error", ex.Message, "OK")!;
        }
        finally
        {
            IsBusy = false;
        }
    }

    private async Task SealCapsuleAsync()
    {
        if (string.IsNullOrWhiteSpace(NewCapsuleTitle) || 
            string.IsNullOrWhiteSpace(NewCapsuleMessage))
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Validation", 
                "Please fill in all fields before sealing the capsule.", 
                "OK")!;
            return;
        }

        if (UnsealDate <= DateTime.Now)
        {
            await Application.Current?.MainPage?.DisplayAlert(
                "Validation", 
                "Unseal date must be in the future.", 
                "OK")!;
            return;
        }

        try
        {
            IsBusy = true;

            var capsule = new TimeCapsule
            {
                Title = NewCapsuleTitle,
                Message = NewCapsuleMessage,
                UnsealDate = UnsealDate,
                PreviewText = NewCapsuleMessage.Length > 50 
                    ? NewCapsuleMessage.Substring(0, 47) + "..." 
                    : NewCapsuleMessage
            };

            await _timeCapsuleService.SealCapsuleAsync(capsule);
            
            await Application.Current?.MainPage?.DisplayAlert(
                "Sealed!", 
                $"Your time capsule has been sealed until {UnsealDate:MMMM dd, yyyy}!", 
                "OK")!;

            // Reset form
            NewCapsuleTitle = string.Empty;
            NewCapsuleMessage = string.Empty;
            UnsealDate = DateTime.Now.AddMonths(1);
            IsCreatingNew = false;

            await LoadCapsulesAsync();
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert("Error", ex.Message, "OK")!;
        }
        finally
        {
            IsBusy = false;
        }
    }

    private async Task UnsealCapsuleAsync(TimeCapsule? capsule)
    {
        if (capsule == null) return;

        if (capsule.UnsealDate > DateTime.Now)
        {
            var daysRemaining = (capsule.UnsealDate - DateTime.Now).Days;
            await Application.Current?.MainPage?.DisplayAlert(
                "Not Yet!", 
                $"This capsule cannot be opened for {daysRemaining} more days.", 
                "OK")!;
            return;
        }

        try
        {
            IsBusy = true;
            await _timeCapsuleService.UnsealCapsuleAsync(capsule.Id);
            
            await Application.Current?.MainPage?.DisplayAlert(
                "Unsealed!", 
                $"Message from the past:\n\n{capsule.Message}", 
                "OK")!;

            await LoadCapsulesAsync();
        }
        catch (Exception ex)
        {
            await Application.Current?.MainPage?.DisplayAlert("Error", ex.Message, "OK")!;
        }
        finally
        {
            IsBusy = false;
        }
    }
}
