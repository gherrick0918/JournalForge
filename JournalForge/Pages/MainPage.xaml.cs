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

	private async void OnEntryTapped(object sender, TappedEventArgs e)
	{
		if (e.Parameter is JournalEntry selectedEntry)
		{
			try
			{
				System.Diagnostics.Debug.WriteLine($"MainPage.OnEntryTapped - Tapped entry ID: {selectedEntry.Id}, Title: {selectedEntry.Title}");
				
				// Navigate to the entry page with the entry ID
				var route = $"JournalEntryPage?entryId={Uri.EscapeDataString(selectedEntry.Id)}";
				System.Diagnostics.Debug.WriteLine($"MainPage.OnEntryTapped - Navigating to: {route}");
				await Shell.Current.GoToAsync(route);
				
				System.Diagnostics.Debug.WriteLine($"MainPage.OnEntryTapped - Navigation completed successfully");
			}
			catch (Exception ex)
			{
				System.Diagnostics.Debug.WriteLine($"MainPage.OnEntryTapped navigation error: {ex.Message}");
				System.Diagnostics.Debug.WriteLine($"MainPage.OnEntryTapped stack trace: {ex.StackTrace}");
				await DisplayAlert("Error", $"Unable to open entry: {ex.Message}", "OK");
			}
		}
		else
		{
			System.Diagnostics.Debug.WriteLine($"MainPage.OnEntryTapped - Parameter is not a JournalEntry");
		}
	}
}
