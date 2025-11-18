package com.journalforge.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.SignInButton
import com.journalforge.app.JournalForgeApplication
import com.journalforge.app.R
import com.journalforge.app.services.GoogleAuthService
import com.journalforge.app.services.SignInResult
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var googleAuthService: GoogleAuthService

    private var isHandlingSignIn = false
    
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Always attempt to handle the sign-in result, regardless of result code
        // Google Sign-In may return data even when resultCode is not RESULT_OK
        // Prevent multiple simultaneous handling of sign-in results
        if (!isHandlingSignIn) {
            // Defensive check: ensure googleAuthService is initialized
            // This prevents crashes if the activity was recreated during sign-in
            if (::googleAuthService.isInitialized) {
                handleSignInResult(result.data)
            } else {
                Log.e(TAG, "googleAuthService not initialized when sign-in result received")
                Toast.makeText(this@LoginActivity, "Sign-in failed due to initialization error. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        googleAuthService = (application as JournalForgeApplication).googleAuthService
        
        // Reset the handling flag on each onCreate
        isHandlingSignIn = false
        
        // Check for force_login_ui flag from MainActivity
        val prefs = getSharedPreferences("auth_state", MODE_PRIVATE)
        val forceLoginUI = prefs.getBoolean("force_login_ui", false)
        
        if (forceLoginUI) {
            // Clear the flag and show login UI regardless of auth state
            prefs.edit().putBoolean("force_login_ui", false).apply()
            Log.d(TAG, "force_login_ui flag set, showing login UI")
            setContentView(R.layout.activity_login)
            setupSignInButton()
            return
        }
        
        // Check if user is already signed in
        if (googleAuthService.isSignedIn()) {
            Log.d(TAG, "User already signed in, navigating to MainActivity")
            
            // Set flag to indicate we're coming from an already-authenticated state
            prefs.edit().putBoolean("just_authenticated", true).apply()
            
            // Clear the activity stack to prevent back navigation to LoginActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        setContentView(R.layout.activity_login)
        setupSignInButton()
    }
    
    private fun setupSignInButton() {

        findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener {
            // Check if user is already signed in before launching sign-in flow
            // This prevents issues when sign-in state is stale or inconsistent
            if (googleAuthService.isSignedIn()) {
                Log.d(TAG, "User already signed in when clicking sign-in button, navigating directly")
                Toast.makeText(this, "Already signed in", Toast.LENGTH_SHORT).show()
                
                // Set flag and navigate to MainActivity
                val prefs = getSharedPreferences("auth_state", MODE_PRIVATE)
                prefs.edit().putBoolean("just_authenticated", true).apply()
                
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return@setOnClickListener
            }
            
            val signInIntent = googleAuthService.getSignInClient().signInIntent
            signInLauncher.launch(signInIntent)
        }
    }

    private fun handleSignInResult(data: Intent?) {
        // Set flag to prevent duplicate handling
        isHandlingSignIn = true
        
        lifecycleScope.launch {
            try {
                // Check if data is null (user cancelled sign-in)
                if (data == null) {
                    Log.d(TAG, "Sign-in cancelled by user")
                    Toast.makeText(this@LoginActivity, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
                    isHandlingSignIn = false
                    return@launch
                }

                val result = googleAuthService.handleSignInResult(data)
                if (result.success) {
                    Log.d(TAG, "Sign-in successful, verifying auth state before navigation")
                    
                    // Verify auth state is stable before navigating
                    // This prevents race conditions where MainActivity checks auth state
                    // before it's fully propagated
                    var retries = 0
                    while (retries < 15 && !googleAuthService.isSignedIn()) {
                        Log.d(TAG, "Waiting for auth state to stabilize (attempt ${retries + 1}/15)")
                        kotlinx.coroutines.delay(100)
                        retries++
                    }
                    
                    if (googleAuthService.isSignedIn()) {
                        Log.d(TAG, "Auth state verified, giving extra time for state propagation")
                        // Give an extra moment for auth state to fully propagate to all listeners
                        kotlinx.coroutines.delay(200)
                        
                        Log.d(TAG, "Navigating to MainActivity")
                        Toast.makeText(this@LoginActivity, R.string.sign_in_success, Toast.LENGTH_SHORT).show()
                        
                        // Set flag to indicate we just completed authentication
                        // This prevents MainActivity from checking auth state too early
                        val prefs = getSharedPreferences("auth_state", MODE_PRIVATE)
                        prefs.edit().putBoolean("just_authenticated", true).apply()
                        
                        // Reset the handling flag before navigation
                        isHandlingSignIn = false
                        
                        // Clear the activity stack and start MainActivity as a new task
                        // This prevents going back to LoginActivity and ensures clean navigation
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e(TAG, "Auth state verification failed after sign-in success")
                        Toast.makeText(this@LoginActivity, "Sign-in succeeded but auth state not ready. Please try again.", Toast.LENGTH_LONG).show()
                        isHandlingSignIn = false
                    }
                } else {
                    Log.e(TAG, "Sign-in failed: ${result.errorMessage}")
                    // Show specific error message to user
                    val errorMsg = result.errorMessage ?: getString(R.string.sign_in_failed)
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                    isHandlingSignIn = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling sign-in result", e)
                Toast.makeText(this@LoginActivity, R.string.sign_in_failed, Toast.LENGTH_LONG).show()
                isHandlingSignIn = false
            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}