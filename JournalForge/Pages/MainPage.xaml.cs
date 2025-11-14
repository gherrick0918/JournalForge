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
		// Execute async without blocking to avoid UI thread issues
		_ = LoadDataAsync();
	}
	
	private async Task LoadDataAsync()
	{
		try
		{
			// Allow the page to render first
			await Task.Delay(50);
			_viewModel.RefreshCommand.Execute(null);
		}
		catch (Exception ex)
		{
			System.Diagnostics.Debug.WriteLine($"MainPage.LoadDataAsync error: {ex.Message}");
		}
	}

	private async void OnEntrySelected(object sender, SelectionChangedEventArgs e)
	{
		if (e.CurrentSelection.FirstOrDefault() is JournalEntry selectedEntry)
		{
			try
			{
				// Clear the selection first to allow re-selecting the same item
				((CollectionView)sender).SelectedItem = null;
				
				// Navigate to the entry page with the entry ID
				await Shell.Current.GoToAsync($"JournalEntryPage?entryId={selectedEntry.Id}");
			}
			catch (Exception ex)
			{
				System.Diagnostics.Debug.WriteLine($"MainPage.OnEntrySelected navigation error: {ex.Message}");
				await DisplayAlert("Error", "Unable to open entry. Please try again.", "OK");
			}
		}
	}
}
