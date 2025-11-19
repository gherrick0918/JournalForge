package com.journalforge.app.services

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.journalforge.app.models.UserProfile

/**
 * Centralized authentication state management.
 * Single source of truth for all authentication state in the app.
 */
class AuthStateManager private constructor() {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    // LiveData for observing authentication state changes
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    // LiveData for observing user profile
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile
    
    init {
        // Initialize with current auth state
        updateAuthState()
        
        // Listen to Firebase auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            Log.d(TAG, "Firebase auth state changed: ${firebaseAuth.currentUser != null}")
            updateAuthState()
        }
    }
    
    /**
     * Update internal auth state based on Firebase
     */
    private fun updateAuthState() {
        val user = auth.currentUser
        
        if (user != null) {
            _authState.postValue(AuthState.Authenticated)
            _userProfile.postValue(
                UserProfile(
                    id = user.uid,
                    email = user.email ?: "",
                    name = user.displayName ?: "",
                    photoUrl = user.photoUrl?.toString()
                )
            )
            Log.d(TAG, "Auth state updated: Authenticated (${user.email})")
        } else {
            _authState.postValue(AuthState.Unauthenticated)
            _userProfile.postValue(null)
            Log.d(TAG, "Auth state updated: Unauthenticated")
        }
    }
    
    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }
    
    /**
     * Get current user profile synchronously (for non-UI code)
     */
    fun getCurrentUserProfile(): UserProfile? {
        val user = auth.currentUser ?: return null
        return UserProfile(
            id = user.uid,
            email = user.email ?: "",
            name = user.displayName ?: "",
            photoUrl = user.photoUrl?.toString()
        )
    }
    
    companion object {
        private const val TAG = "AuthStateManager"
        
        @Volatile
        private var instance: AuthStateManager? = null
        
        /**
         * Get singleton instance
         */
        fun getInstance(): AuthStateManager {
            return instance ?: synchronized(this) {
                instance ?: AuthStateManager().also { instance = it }
            }
        }
    }
}

/**
 * Represents the authentication state of the user
 */
sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}
