using Microsoft.Extensions.Logging;
using CommunityToolkit.Maui;

namespace JournalForge;

public static class MauiProgram
{
	public static MauiApp CreateMauiApp()
	{
		var builder = MauiApp.CreateBuilder();
		builder
			.UseMauiApp<App>()
			.UseMauiCommunityToolkit()
			.ConfigureFonts(fonts =>
			{
				fonts.AddFont("OpenSans-Regular.ttf", "OpenSansRegular");
				fonts.AddFont("OpenSans-Semibold.ttf", "OpenSansSemibold");
			});

		// Register services
		builder.Services.AddSingleton<Services.IAIService, Services.AIService>();
		builder.Services.AddSingleton<Services.ITimeCapsuleService, Services.TimeCapsuleService>();
		builder.Services.AddSingleton<Services.IJournalEntryService, Services.JournalEntryService>();

		// Register view models
		builder.Services.AddTransient<ViewModels.MainViewModel>();
		builder.Services.AddTransient<ViewModels.JournalEntryViewModel>();
		builder.Services.AddTransient<ViewModels.TimeCapsuleViewModel>();

		// Register pages
		builder.Services.AddTransient<Pages.MainPage>();
		builder.Services.AddTransient<Pages.JournalEntryPage>();
		builder.Services.AddTransient<Pages.TimeCapsulePage>();

#if DEBUG
		builder.Logging.AddDebug();
#endif

		return builder.Build();
	}
}
