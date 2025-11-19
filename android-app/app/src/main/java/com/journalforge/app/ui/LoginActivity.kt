package com.journalforge.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.SignInButton
import com.journalforge.app.JournalForgeApplication
import com.journalforge.app.R
import com.journalforge.app.services.AuthState
import com.journalforge.app.services.GoogleAuthService
import com.journalforge.app.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Simplified LoginActivity using reactive auth state management.
 * Relies purely on LiveData observer - no synchronous checks to avoid race conditions.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var googleAuthService: GoogleAuthService
    private val authViewModel: AuthViewModel by viewModels()
    private var hasNavigated = false
    
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleSignInResult(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Observe auth state - navigate to MainActivity when authenticated
        authViewModel.authState.observe(this) { authState ->
            Log.d(TAG, "Auth state changed: $authState")
            
            // Prevent multiple navigation attempts
            if (hasNavigated) {
                Log.d(TAG, "Already navigated, ignoring auth state change")
                return@observe
            }
            
            when (authState) {
                is AuthState.Loading -> {
                    Log.d(TAG, "Auth state is Loading, showing loading UI")
                    showLoadingUI()
                }
                is AuthState.Authenticated -> {
                    Log.d(TAG, "Auth state is Authenticated, navigating to MainActivity")
                    navigateToMainActivity()
                }
                is AuthState.Unauthenticated -> {
                    Log.d(TAG, "Auth state is Unauthenticated, showing login UI")
                    showLoginUI()
                }
            }
        }
    }
    
    private fun showLoadingUI() {
        setContentView(R.layout.activity_login)
        // Hide sign-in button while loading
        findViewById<SignInButton>(R.id.sign_in_button)?.visibility = android.view.View.GONE
    }
    
    private fun showLoginUI() {
        // User is not authenticated, show login UI
        googleAuthService = (application as JournalForgeApplication).googleAuthService
        setContentView(R.layout.activity_login)
        setupSignInButton()
    }
    
    private fun setupSignInButton() {
        val signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.visibility = android.view.View.VISIBLE
        signInButton.setOnClickListener {
            Log.d(TAG, "Sign-in button clicked, launching Google Sign-In")
            val signInIntent = googleAuthService.getSignInClient().signInIntent
            signInLauncher.launch(signInIntent)
        }
    }

    private fun handleSignInResult(data: Intent?) {
        lifecycleScope.launch {
            try {
                if (data == null) {
                    Log.d(TAG, "Sign-in cancelled by user")
                    Toast.makeText(this@LoginActivity, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val result = googleAuthService.handleSignInResult(data)
                if (result.success) {
                    Log.d(TAG, "Sign-in successful")
                    Toast.makeText(this@LoginActivity, R.string.sign_in_success, Toast.LENGTH_SHORT).show()
                    // AuthStateManager will automatically update state
                    // Observer will handle navigation to MainActivity
                } else {
                    Log.e(TAG, "Sign-in failed: ${result.errorMessage}")
                    val errorMsg = result.errorMessage ?: getString(R.string.sign_in_failed)
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling sign-in result", e)
                Toast.makeText(this@LoginActivity, R.string.sign_in_failed, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun navigateToMainActivity() {
        // Prevent multiple navigation attempts
        if (hasNavigated) {
            Log.d(TAG, "Already navigated, skipping")
            return
        }
        hasNavigated = true
        
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}