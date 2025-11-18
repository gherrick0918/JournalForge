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

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Always attempt to handle the sign-in result, regardless of result code
        // Google Sign-In may return data even when resultCode is not RESULT_OK
        handleSignInResult(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        googleAuthService = (application as JournalForgeApplication).googleAuthService
        
        // Check if user is already signed in
        if (googleAuthService.isSignedIn()) {
            Log.d(TAG, "User already signed in, navigating to MainActivity")
            // Clear the activity stack to prevent back navigation to LoginActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        setContentView(R.layout.activity_login)

        findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener {
            val signInIntent = googleAuthService.getSignInClient().signInIntent
            signInLauncher.launch(signInIntent)
        }
    }

    private fun handleSignInResult(data: Intent?) {
        lifecycleScope.launch {
            try {
                // Check if data is null (user cancelled sign-in)
                if (data == null) {
                    Log.d(TAG, "Sign-in cancelled by user")
                    Toast.makeText(this@LoginActivity, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val result = googleAuthService.handleSignInResult(data)
                if (result.success) {
                    Log.d(TAG, "Sign-in successful, navigating to MainActivity")
                    Toast.makeText(this@LoginActivity, R.string.sign_in_success, Toast.LENGTH_SHORT).show()
                    // Clear the activity stack and start MainActivity as a new task
                    // This prevents going back to LoginActivity and ensures clean navigation
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.e(TAG, "Sign-in failed: ${result.errorMessage}")
                    // Show specific error message to user
                    val errorMsg = result.errorMessage ?: getString(R.string.sign_in_failed)
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling sign-in result", e)
                Toast.makeText(this@LoginActivity, R.string.sign_in_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}