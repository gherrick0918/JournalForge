package com.journalforge.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        setContentView(R.layout.activity_login)

        googleAuthService = (application as JournalForgeApplication).googleAuthService

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
                    return@launch
                }

                val success = googleAuthService.handleSignInResult(data)
                if (success) {
                    Log.d(TAG, "Sign-in successful, navigating to MainActivity")
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Log.e(TAG, "Sign-in failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling sign-in result", e)
            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}