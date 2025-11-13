using JournalForge.ViewModels;

namespace JournalForge.Pages;

[QueryProperty(nameof(EntryId), "entryId")]
public partial class JournalEntryPage : ContentPage
{
	private readonly JournalEntryViewModel _viewModel;

	public JournalEntryPage(JournalEntryViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
		_viewModel = viewModel;
	}

	public string? EntryId { get; set; }

	protected override async void OnAppearing()
	{
		base.OnAppearing();
		
		// Load existing entry if EntryId is provided
		if (!string.IsNullOrEmpty(EntryId))
		{
			await _viewModel.LoadEntryAsync(EntryId);
		}
	}
}
