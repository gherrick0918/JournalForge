using Android.App;
using Android.Content.PM;
using Android.OS;

namespace JournalForge;

[Activity(Theme = "@style/Maui.SplashTheme", MainLauncher = true, LaunchMode = LaunchMode.SingleTop, ConfigurationChanges = ConfigChanges.ScreenSize | ConfigChanges.Orientation | ConfigChanges.UiMode | ConfigChanges.ScreenLayout | ConfigChanges.SmallestScreenSize | ConfigChanges.Density)]
public class MainActivity : MauiAppCompatActivity
{
    protected override void OnCreate(Bundle? savedInstanceState)
    {
        try
        {
            base.OnCreate(savedInstanceState);
        }
        catch (System.Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"MainActivity.OnCreate error: {ex.Message}");
            throw;
        }
    }
}
