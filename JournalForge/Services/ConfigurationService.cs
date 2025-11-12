using System.Text.Json;
using JournalForge.Models;

namespace JournalForge.Services;

/// <summary>
/// Service to load configuration from appsettings.json files.
/// This allows secure API key storage without committing sensitive data to git.
/// </summary>
public class ConfigurationService
{
    /// <summary>
    /// Loads app settings from appsettings.local.json (priority) or appsettings.json (fallback).
    /// The local file should contain your actual API key and is excluded from git.
    /// </summary>
    public static async Task<AppSettings> LoadSettingsAsync()
    {
        var settings = new AppSettings();
        
        try
        {
            // Try to load from appsettings.local.json first (user's private config)
            var localConfig = await TryLoadConfigFileAsync("appsettings.local.json");
            if (localConfig != null)
            {
                return ParseSettings(localConfig);
            }
            
            // Fallback to appsettings.json (checked into git, no API key)
            var defaultConfig = await TryLoadConfigFileAsync("appsettings.json");
            if (defaultConfig != null)
            {
                return ParseSettings(defaultConfig);
            }
        }
        catch (Exception ex)
        {
            // Log error but don't crash - app works without OpenAI
            System.Diagnostics.Debug.WriteLine($"Error loading settings: {ex.Message}");
        }
        
        // Return empty settings if no config file found (fallback to mock AI)
        return settings;
    }
    
    private static async Task<string?> TryLoadConfigFileAsync(string filename)
    {
        try
        {
            // Try to read from app's Resources/Raw folder (where MAUI assets are stored)
            using var stream = await FileSystem.OpenAppPackageFileAsync(filename);
            using var reader = new StreamReader(stream);
            return await reader.ReadToEndAsync();
        }
        catch
        {
            // File not found or couldn't be read - this is normal
            return null;
        }
    }
    
    private static AppSettings ParseSettings(string json)
    {
        try
        {
            var doc = JsonDocument.Parse(json);
            var root = doc.RootElement;
            
            if (root.TryGetProperty("OpenAI", out var openAI))
            {
                var settings = new AppSettings();
                
                if (openAI.TryGetProperty("ApiKey", out var apiKey))
                {
                    settings.OpenAIApiKey = apiKey.GetString() ?? string.Empty;
                }
                
                if (openAI.TryGetProperty("Model", out var model))
                {
                    settings.OpenAIModel = model.GetString() ?? "gpt-4o-mini";
                }
                
                return settings;
            }
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"Error parsing settings: {ex.Message}");
        }
        
        return new AppSettings();
    }
}
