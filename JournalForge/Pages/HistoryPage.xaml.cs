using JournalForge.ViewModels;

namespace JournalForge.Pages;

public partial class HistoryPage : ContentPage
{
	public HistoryPage(HistoryViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
