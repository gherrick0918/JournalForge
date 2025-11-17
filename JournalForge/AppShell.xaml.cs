namespace JournalForge;

public partial class AppShell : Shell
{
	public AppShell()
	{
		InitializeComponent();

		// Register routes for navigation
		// Note: Routes defined in AppShell.xaml don't need to be registered again
		// But we register them here to support programmatic navigation with parameters
		Routing.RegisterRoute("MainPage", typeof(Pages.MainPage));
		Routing.RegisterRoute("JournalEntryPage", typeof(Pages.JournalEntryPage));
		Routing.RegisterRoute("HistoryPage", typeof(Pages.HistoryPage));
		Routing.RegisterRoute("TimeCapsulePage", typeof(Pages.TimeCapsulePage));
	}
}
