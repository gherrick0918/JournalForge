using Android.App;
using Android.Runtime;

namespace JournalForge;

[Application]
public class MainApplication : MauiApplication
{
	public MainApplication(IntPtr handle, JniHandleOwnership ownership)
		: base(handle, ownership)
	{
	}

	protected override MauiApp CreateMauiApp()
	{
		try
		{
			return MauiProgram.CreateMauiApp();
		}
		catch (System.Exception ex)
		{
			System.Diagnostics.Debug.WriteLine($"MainApplication.CreateMauiApp error: {ex.Message}");
			throw;
		}
	}

	public override void OnCreate()
	{
		try
		{
			base.OnCreate();
		}
		catch (System.Exception ex)
		{
			System.Diagnostics.Debug.WriteLine($"MainApplication.OnCreate error: {ex.Message}");
			throw;
		}
	}
}
