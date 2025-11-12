using JournalForge.ViewModels;
using JournalForge.Models;

namespace JournalForge.Pages;

public partial class MainPage : ContentPage
{
	private readonly MainViewModel _viewModel;

	public MainPage(MainViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
		_viewModel = viewModel;
	}

	private async void OnEntrySelected(object sender, SelectionChangedEventArgs e)
	{
		if (e.CurrentSelection.FirstOrDefault() is JournalEntry selectedEntry)
		{
			// Show the entry details
			await DisplayAlert(
				selectedEntry.Title,
				$"Created: {selectedEntry.CreatedDate:MMMM dd, yyyy}\n\n{selectedEntry.Content}",
				"OK");
			
			// Clear the selection
			((CollectionView)sender).SelectedItem = null;
		}
	}
}
