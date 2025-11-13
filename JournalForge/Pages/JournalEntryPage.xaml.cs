using JournalForge.ViewModels;

namespace JournalForge.Pages;

[QueryProperty(nameof(EntryId), "entryId")]
public partial class JournalEntryPage : ContentPage
{
	private readonly JournalEntryViewModel _viewModel;
	private ToolbarItem? _saveToolbarItem;

	public JournalEntryPage(JournalEntryViewModel viewModel)
	{
		InitializeComponent();
		BindingContext = viewModel;
		_viewModel = viewModel;
		
		// Store reference to save toolbar item
		_saveToolbarItem = ToolbarItems.FirstOrDefault();
		
		// Subscribe to property changes
		_viewModel.PropertyChanged += OnViewModelPropertyChanged;
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
		else
		{
			// Reset for a new entry with personalized greeting
			await _viewModel.ResetForNewEntryAsync();
		}
		
		// Update toolbar based on view mode
		UpdateToolbarItems();
	}
	
	private void OnViewModelPropertyChanged(object? sender, System.ComponentModel.PropertyChangedEventArgs e)
	{
		if (e.PropertyName == nameof(JournalEntryViewModel.IsViewMode))
		{
			UpdateToolbarItems();
		}
	}
	
	private void UpdateToolbarItems()
	{
		if (_saveToolbarItem == null) return;
		
		if (_viewModel.IsViewMode)
		{
			// Remove save button in view mode
			if (ToolbarItems.Contains(_saveToolbarItem))
			{
				ToolbarItems.Remove(_saveToolbarItem);
			}
		}
		else
		{
			// Add save button in edit mode
			if (!ToolbarItems.Contains(_saveToolbarItem))
			{
				ToolbarItems.Add(_saveToolbarItem);
			}
		}
	}
}
