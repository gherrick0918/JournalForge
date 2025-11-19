package com.journalforge.app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.journalforge.app.models.UserProfile
import com.journalforge.app.services.AuthState
import com.journalforge.app.services.AuthStateManager

/**
 * ViewModel for authentication-related UI
 * Provides auth state to UI components in a lifecycle-aware manner
 */
class AuthViewModel : ViewModel() {
    
    private val authStateManager = AuthStateManager.getInstance()
    
    // Expose auth state to UI
    val authState: LiveData<AuthState> = authStateManager.authState
    val userProfile: LiveData<UserProfile?> = authStateManager.userProfile
    
    /**
     * Check if user is authenticated (synchronous)
     */
    fun isAuthenticated(): Boolean {
        return authStateManager.isAuthenticated()
    }
    
    /**
     * Get current user profile (synchronous)
     */
    fun getCurrentUser(): UserProfile? {
        return authStateManager.getCurrentUserProfile()
    }
}
