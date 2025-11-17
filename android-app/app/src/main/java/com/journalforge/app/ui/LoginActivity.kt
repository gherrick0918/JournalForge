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
        if (result.resultCode == RESULT_OK) {
            lifecycleScope.launch {
                val success = googleAuthService.handleSignInResult(result.data)
                if (success) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    // Handle sign-in failure
                    Log.e("LoginActivity", "Sign-in failed")
                }
            }
        }
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
}