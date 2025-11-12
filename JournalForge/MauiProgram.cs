using Microsoft.Extensions.Logging;
using CommunityToolkit.Maui;
using JournalForge.Models;

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

		// Configure app settings for OpenAI
		// TO USE OPENAI: Set your API key in the AppSettings below
		var appSettings = new AppSettings
		{
			// Replace with your OpenAI API key from https://platform.openai.com/api-keys
			OpenAIApiKey = Environment.GetEnvironmentVariable("OPENAI_API_KEY") ?? string.Empty,
			OpenAIModel = "gpt-4o-mini" // Cost-effective model; change to "gpt-4o" for better quality
		};
		builder.Services.AddSingleton(appSettings);

		// Register AI service - uses OpenAI if API key is configured, otherwise falls back to mock
		builder.Services.AddSingleton<Services.IAIService>(sp =>
		{
			var settings = sp.GetRequiredService<AppSettings>();
			if (!string.IsNullOrWhiteSpace(settings.OpenAIApiKey))
			{
				return new Services.OpenAIService(settings);
			}
			return new Services.AIService();
		});

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
