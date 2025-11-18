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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.journalforge.app.R
import com.journalforge.app.models.UserProfile
import kotlinx.coroutines.tasks.await

/**
 * Service for handling Google Sign-In with Firebase Authentication
 */
class GoogleAuthService(private val context: Context) {

    private val auth: FirebaseAuth = Firebase.auth
    private val googleSignInClient: GoogleSignInClient

    // Listener for authentication state changes
    var onAuthStateChanged: ((Boolean) -> Unit)? = null

    init {
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)

        // Add auth state listener
        auth.addAuthStateListener { firebaseAuth ->
            onAuthStateChanged?.invoke(firebaseAuth.currentUser != null)
        }
    }

    /**
     * Get the GoogleSignInClient for starting the sign-in intent
     */
    fun getSignInClient(): GoogleSignInClient = googleSignInClient

    /**
     * Handle the sign-in result from the Google Sign-In intent
     * @param data The intent data returned from the sign-in activity
     * @return True if sign-in was successful
     */
    suspend fun handleSignInResult(data: android.content.Intent?): Boolean {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if (account != null) {
                Log.d(TAG, "Google sign-in successful for account: ${account.email}")
                firebaseAuthWithGoogle(account)
            } else {
                Log.e(TAG, "Google sign-in account is null")
                false
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign-in failed with status code: ${e.statusCode}", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google sign-in", e)
            false
        }
    }

    /**
     * Authenticate with Firebase using Google credentials
     */
    private suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Boolean {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()

            Log.d(TAG, "Firebase authentication successful: ${result.user?.email}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firebase authentication failed", e)
            false
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

    /**
     * Check if user is currently signed in
     */
    fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Get the current user profile
     */
    fun getCurrentUser(): UserProfile? {
        val user = auth.currentUser ?: return null

        return UserProfile(
            id = user.uid,
            email = user.email ?: "",
            name = user.displayName ?: "",
            photoUrl = user.photoUrl?.toString()
        )
    }

    companion object {
        private const val TAG = "GoogleAuthService"
    }
}