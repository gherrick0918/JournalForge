package com.journalforge.app.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.journalforge.app.JournalForgeApplication
import com.journalforge.app.R
import com.journalforge.app.services.AuthState
import com.journalforge.app.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Simplified SettingsActivity using reactive auth state management
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var app: JournalForgeApplication
    private lateinit var tvAccountInfo: TextView
    private lateinit var btnSignIn: Button
    private lateinit var btnSignOut: Button
    private lateinit var tvSyncStatus: TextView
    
    private val authViewModel: AuthViewModel by viewModels()
    
    // Register for activity result to handle Google Sign-In
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleSignInResult(result.data)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        app = application as JournalForgeApplication
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "⚙️ Settings"
        
        // Initialize views
        tvAccountInfo = findViewById(R.id.tv_account_info)
        btnSignIn = findViewById(R.id.btn_sign_in_google)
        btnSignOut = findViewById(R.id.btn_sign_out)
        tvSyncStatus = findViewById(R.id.tv_sync_status)
        
        // Setup listeners
        btnSignIn.setOnClickListener {
            signInWithGoogle()
        }
        
        btnSignOut.setOnClickListener {
            signOut()
        }
        
        // Observe auth state changes
        authViewModel.authState.observe(this) { authState ->
            when (authState) {
                is AuthState.Loading -> updateUI(false) // Show as not signed in while loading
                is AuthState.Authenticated -> updateUI(true)
                is AuthState.Unauthenticated -> updateUI(false)
            }
        }
        
        // Observe user profile changes
        authViewModel.userProfile.observe(this) { userProfile ->
            if (userProfile != null) {
                tvAccountInfo.text = getString(R.string.signed_in_as, userProfile.email)
            }
        }
    }
    
    private fun signInWithGoogle() {
        val signInIntent = app.googleAuthService.getSignInClient().signInIntent
        signInLauncher.launch(signInIntent)
    }
    
    private fun handleSignInResult(data: android.content.Intent?) {
        lifecycleScope.launch {
            try {
                if (data == null) {
                    Toast.makeText(this@SettingsActivity, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                val result = app.googleAuthService.handleSignInResult(data)
                if (result.success) {
                    Toast.makeText(this@SettingsActivity, R.string.sign_in_success, Toast.LENGTH_SHORT).show()
                    // AuthStateManager will automatically update UI via observer
                } else {
                    val errorMsg = result.errorMessage ?: getString(R.string.sign_in_failed)
                    Toast.makeText(this@SettingsActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, R.string.sign_in_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun signOut() {
        lifecycleScope.launch {
            try {
                app.googleAuthService.signOut()
                Toast.makeText(this@SettingsActivity, "Signed out successfully", Toast.LENGTH_SHORT).show()
                // AuthStateManager will automatically update UI via observer
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error signing out", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateUI(isSignedIn: Boolean) {
        if (isSignedIn) {
            val user = authViewModel.getCurrentUser()
            tvAccountInfo.text = getString(R.string.signed_in_as, user?.email ?: "Unknown")
            tvAccountInfo.visibility = View.VISIBLE
            btnSignIn.visibility = View.GONE
            btnSignOut.visibility = View.VISIBLE
            tvSyncStatus.visibility = View.VISIBLE
            tvSyncStatus.text = getString(R.string.never_synced)
        } else {
            tvAccountInfo.visibility = View.GONE
            btnSignIn.visibility = View.VISIBLE
            btnSignOut.visibility = View.GONE
            tvSyncStatus.visibility = View.GONE
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}