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

	protected override void OnAppearing()
	{
		base.OnAppearing();
		// Refresh data when returning to the page
		_viewModel.RefreshCommand.Execute(null);
	}

	private async void OnEntrySelected(object sender, SelectionChangedEventArgs e)
	{
		if (e.CurrentSelection.FirstOrDefault() is JournalEntry selectedEntry)
		{
			// Navigate to the entry page with the entry ID
			await Shell.Current.GoToAsync($"{nameof(JournalEntryPage)}?entryId={selectedEntry.Id}");
			
			// Clear the selection
			((CollectionView)sender).SelectedItem = null;
		}
	}
}
