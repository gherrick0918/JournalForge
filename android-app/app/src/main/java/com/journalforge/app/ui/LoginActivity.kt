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
            startActivity(Intent(this, MainActivity::class.java))
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

                val success = googleAuthService.handleSignInResult(data)
                if (success) {
                    Log.d(TAG, "Sign-in successful, navigating to MainActivity")
                    Toast.makeText(this@LoginActivity, R.string.sign_in_success, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Log.e(TAG, "Sign-in failed")
                    Toast.makeText(this@LoginActivity, R.string.sign_in_failed, Toast.LENGTH_LONG).show()
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