package com.journalforge.app.services

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import com.journalforge.app.R

/**
 * Result of a Google Sign-In attempt
 */
data class SignInResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val errorCode: Int? = null
)

/**
 * Simplified Google Sign-In service.
 * Handles only authentication operations - state management is in AuthStateManager.
 */
class GoogleAuthService(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient

    init {
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    /**
     * Get the GoogleSignInClient for starting the sign-in intent
     */
    fun getSignInClient(): GoogleSignInClient = googleSignInClient

    /**
     * Handle the sign-in result from the Google Sign-In intent
     * @param data The intent data returned from the sign-in activity
     * @return SignInResult with success status and error details if applicable
     */
    suspend fun handleSignInResult(data: android.content.Intent?): SignInResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if (account != null) {
                Log.d(TAG, "Google sign-in successful for account: ${account.email}")
                firebaseAuthWithGoogle(account)
            } else {
                Log.e(TAG, "Google sign-in account is null")
                SignInResult(
                    success = false,
                    errorMessage = "Unable to retrieve account information. Please try again."
                )
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign-in failed with status code: ${e.statusCode}", e)
            val errorMessage = when (e.statusCode) {
                10 -> "Developer error: Please ensure SHA-1 fingerprint is configured in Firebase Console."
                12500 -> "Configuration error: Please check your Firebase setup and google-services.json file."
                7 -> "Network error: Please check your internet connection and try again."
                else -> "Sign in failed (Error ${e.statusCode}). Please try again or contact support."
            }
            SignInResult(
                success = false,
                errorMessage = errorMessage,
                errorCode = e.statusCode
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google sign-in", e)
            SignInResult(
                success = false,
                errorMessage = "An unexpected error occurred: ${e.message ?: "Unknown error"}"
            )
        }
    }

    /**
     * Authenticate with Firebase using Google credentials
     */
    private suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): SignInResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()

            Log.d(TAG, "Firebase authentication successful: ${result.user?.email}")
            SignInResult(success = true)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase authentication failed", e)
            SignInResult(
                success = false,
                errorMessage = "Firebase authentication failed: ${e.message ?: "Unknown error"}"
            )
        }
    }

    /**
     * Sign out from both Google and Firebase
     */
    suspend fun signOut() {
        try {
            auth.signOut()
            googleSignInClient.signOut().await()
            Log.d(TAG, "Sign out successful")
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
        }
    }

    companion object {
        private const val TAG = "GoogleAuthService"
    }
}