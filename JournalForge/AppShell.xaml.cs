namespace JournalForge;

public partial class AppShell : Shell
{
	public AppShell()
	{
		InitializeComponent();

		// Register routes for navigation
		Routing.RegisterRoute(nameof(Pages.MainPage), typeof(Pages.MainPage));
		Routing.RegisterRoute(nameof(Pages.JournalEntryPage), typeof(Pages.JournalEntryPage));
		Routing.RegisterRoute(nameof(Pages.TimeCapsulePage), typeof(Pages.TimeCapsulePage));
	}
}
