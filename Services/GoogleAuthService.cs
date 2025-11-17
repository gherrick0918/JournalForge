using System;
using System.Net.Http;
using System.Net.Http.Json;
using System.Text.Json;
using System.Threading.Tasks;
using Microsoft.Maui.Authentication;

namespace JournalForge.Services;

public class UserProfile
{
    public string Id { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string PhotoUrl { get; set; } = string.Empty;
}

public interface IGoogleAuthService
{
    Task<bool> SignInAsync();
    Task SignOutAsync();
    Task<bool> IsSignedInAsync();
    Task<UserProfile?> GetCurrentUserAsync();
    event EventHandler<bool>? AuthenticationStateChanged;
}

public class GoogleAuthService : IGoogleAuthService
{
    private readonly string _firebaseApiKey;
    private readonly string _googleClientId;
    private readonly string _redirectUri;
    private readonly HttpClient _httpClient = new();

    private UserProfile? _currentUser;
    private bool _isSignedIn = false;

    public event EventHandler<bool>? AuthenticationStateChanged;

    // Constructor requires the Firebase Web API key, the Google OAuth client id, and your redirect URI
    public GoogleAuthService(string firebaseApiKey, string googleClientId, string redirectUri)
    {
        _firebaseApiKey = firebaseApiKey ?? throw new ArgumentNullException(nameof(firebaseApiKey));
        _googleClientId = googleClientId ?? throw new ArgumentNullException(nameof(googleClientId));
        _redirectUri = redirectUri ?? throw new ArgumentNullException(nameof(redirectUri));
    }

    public async Task<bool> SignInAsync()
    {
        try
        {
            // Build Google OAuth2 authorize URL using implicit flow to obtain id_token (and access_token).
            // Ensure the redirect URI is registered for the OAuth client (native app scheme).
            var scopes = "openid email profile";
            var nonce = Guid.NewGuid().ToString("N");
            var authUrl =
                $"https://accounts.google.com/o/oauth2/v2/auth" +
                $"?client_id={Uri.EscapeDataString(_googleClientId)}" +
                $"&redirect_uri={Uri.EscapeDataString(_redirectUri)}" +
                $"&response_type=token%20id_token" +
                $"&scope={Uri.EscapeDataString(scopes)}" +
                $"&nonce={Uri.EscapeDataString(nonce)}" +
                $"&prompt=select_account";

            // Launch the system browser / web view to authenticate
            var authResult = await WebAuthenticator.AuthenticateAsync(
                new Uri(authUrl),
                new Uri(_redirectUri));

            if (authResult == null)
            {
                System.Diagnostics.Debug.WriteLine("WebAuthenticator returned null result.");
                return false;
            }

            // WebAuthenticator returns tokens in the Properties dictionary (depends on platform/response)
            authResult.Properties.TryGetValue("id_token", out var idToken);
            authResult.Properties.TryGetValue("access_token", out var accessToken);

            if (string.IsNullOrWhiteSpace(idToken) && string.IsNullOrWhiteSpace(accessToken))
            {
                System.Diagnostics.Debug.WriteLine("No id_token or access_token returned from Google.");
                return false;
            }

            // Exchange the Google token with Firebase Auth (REST API) to sign in / create a Firebase user
            var firebaseEndpoint = $"https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key={_firebaseApiKey}";

            // Prefer id_token, fall back to access_token
            var postBody = !string.IsNullOrWhiteSpace(idToken)
                ? $"id_token={Uri.EscapeDataString(idToken)}&providerId=google.com"
                : $"access_token={Uri.EscapeDataString(accessToken)}&providerId=google.com";

            var payload = new
            {
                postBody,
                requestUri = "http://localhost", // any valid URL; Firebase requires it
                returnSecureToken = true,
                returnIdpCredential = true
            };

            var response = await _httpClient.PostAsJsonAsync(firebaseEndpoint, payload);
            if (!response.IsSuccessStatusCode)
            {
                var err = await response.Content.ReadAsStringAsync();
                System.Diagnostics.Debug.WriteLine($"Firebase signInWithIdp failed: {response.StatusCode} - {err}");
                return false;
            }

            using var stream = await response.Content.ReadAsStreamAsync();
            using var doc = await JsonDocument.ParseAsync(stream);
            var root = doc.RootElement;

            // Extract commonly used fields from Firebase response
            var localId = root.TryGetProperty("localId", out var pLocalId) ? pLocalId.GetString() ?? string.Empty : string.Empty;
            var email = root.TryGetProperty("email", out var pEmail) ? pEmail.GetString() ?? string.Empty : string.Empty;
            var displayName = root.TryGetProperty("displayName", out var pName) ? pName.GetString() ?? string.Empty : string.Empty;
            var photoUrl = root.TryGetProperty("photoUrl", out var pPhoto) ? pPhoto.GetString() ?? string.Empty : string.Empty;

            // Populate current user
            _currentUser = new UserProfile
            {
                Id = localId,
                Email = email,
                Name = displayName,
                PhotoUrl = photoUrl
            };

            _isSignedIn = true;
            AuthenticationStateChanged?.Invoke(this, true);

            return true;
        }
        catch (OperationCanceledException)
        {
            // User cancelled auth
            System.Diagnostics.Debug.WriteLine("User cancelled Google sign-in.");
            return false;
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"Error signing in with Google/Firebase: {ex}");
            return false;
        }
    }

    public async Task SignOutAsync()
    {
        try
        {
            _currentUser = null;
            _isSignedIn = false;
            AuthenticationStateChanged?.Invoke(this, false);
            await Task.CompletedTask;
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine($"Error signing out: {ex.Message}");
        }
    }

    public Task<bool> IsSignedInAsync()
    {
        return Task.FromResult(_isSignedIn);
    }

    public Task<UserProfile?> GetCurrentUserAsync()
    {
        return Task.FromResult(_currentUser);
    }
}
