using JournalForge.ViewModels;

namespace JournalForge.Pages;

public partial class TimeCapsulePage : ContentPage
{
	public TimeCapsulePage(TimeCapsuleViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
