using JournalForge.ViewModels;

namespace JournalForge.Pages;

public partial class JournalEntryPage : ContentPage
{
	public JournalEntryPage(JournalEntryViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
	}
}
