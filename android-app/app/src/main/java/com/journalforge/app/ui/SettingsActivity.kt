package com.journalforge.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.journalforge.app.JournalForgeApplication
import com.journalforge.app.R
import kotlinx.coroutines.launch

/**
 * Settings activity with Google Sign-In integration
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var app: JournalForgeApplication
    private lateinit var tvAccountInfo: TextView
    private lateinit var btnSignIn: Button
    private lateinit var btnSignOut: Button
    private lateinit var tvSyncStatus: TextView
    
    // Register for activity result to handle Google Sign-In
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Always attempt to handle the sign-in result, regardless of result code
        // Google Sign-In may return data even when resultCode is not RESULT_OK
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
        
        // Setup auth state change listener
        app.googleAuthService.onAuthStateChanged = { isSignedIn ->
            runOnUiThread {
                updateUI(isSignedIn)
            }
        }
        
        // Update initial UI
        updateUI(app.googleAuthService.isSignedIn())
    }
    
    private fun signInWithGoogle() {
        val signInIntent = app.googleAuthService.getSignInClient().signInIntent
        signInLauncher.launch(signInIntent)
    }
    
    private fun handleSignInResult(data: Intent?) {
        lifecycleScope.launch {
            try {
                // Check if data is null (user cancelled sign-in)
                if (data == null) {
                    Toast.makeText(this@SettingsActivity, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                val success = app.googleAuthService.handleSignInResult(data)
                if (success) {
                    Toast.makeText(this@SettingsActivity, R.string.sign_in_success, Toast.LENGTH_SHORT).show()
                    updateUI(true)
                } else {
                    Toast.makeText(this@SettingsActivity, R.string.sign_in_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun signOut() {
        lifecycleScope.launch {
            try {
                app.googleAuthService.signOut()
                Toast.makeText(this@SettingsActivity, "Signed out successfully", Toast.LENGTH_SHORT).show()
                updateUI(false)
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error signing out", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateUI(isSignedIn: Boolean) {
        if (isSignedIn) {
            val user = app.googleAuthService.getCurrentUser()
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
