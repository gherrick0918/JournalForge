using JournalForge.ViewModels;

namespace JournalForge.Pages;

[QueryProperty(nameof(EntryId), "entryId")]
public partial class JournalEntryPage : ContentPage
{
	private readonly JournalEntryViewModel _viewModel;
	private ToolbarItem? _saveToolbarItem;
	private string? _entryId;
	private bool _isNavigatingToEntry;

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

	public string? EntryId 
	{ 
		get => _entryId;
		set
		{
			System.Diagnostics.Debug.WriteLine($"JournalEntryPage.EntryId setter called with value: {value}");
			_entryId = value;
			_isNavigatingToEntry = !string.IsNullOrEmpty(value);
			System.Diagnostics.Debug.WriteLine($"JournalEntryPage.EntryId - _isNavigatingToEntry set to: {_isNavigatingToEntry}");
		}
	}

	protected override async void OnAppearing()
	{
		base.OnAppearing();
		
		System.Diagnostics.Debug.WriteLine($"JournalEntryPage.OnAppearing - EntryId: {_entryId}, _isNavigatingToEntry: {_isNavigatingToEntry}");
		
		// Load existing entry if EntryId is provided
		if (_isNavigatingToEntry && !string.IsNullOrEmpty(_entryId))
		{
			System.Diagnostics.Debug.WriteLine($"JournalEntryPage.OnAppearing - Loading entry with ID: {_entryId}");
			await _viewModel.LoadEntryAsync(_entryId);
			_isNavigatingToEntry = false; // Reset flag after loading
			System.Diagnostics.Debug.WriteLine($"JournalEntryPage.OnAppearing - Entry loaded successfully");
		}
		else if (!_isNavigatingToEntry)
		{
			System.Diagnostics.Debug.WriteLine($"JournalEntryPage.OnAppearing - Resetting for new entry");
			// Reset for a new entry with personalized greeting
			await _viewModel.ResetForNewEntryAsync();
		}
		
		// Clear the EntryId after loading to allow new entries next time
		_entryId = null;
		
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
