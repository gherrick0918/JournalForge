package com.journalforge.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.journalforge.app.JournalForgeApplication
import com.journalforge.app.R
import com.journalforge.app.models.JournalEntry
import com.journalforge.app.services.AuthState
import com.journalforge.app.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Simplified MainActivity using reactive auth state management.
 * Relies purely on LiveData observer - no synchronous checks to avoid race conditions.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var app: JournalForgeApplication
    private lateinit var tvDailyPrompt: TextView
    private lateinit var tvDailyInsight: TextView
    private lateinit var rvRecentEntries: RecyclerView
    private lateinit var tvNoEntries: TextView
    private lateinit var btnNewEntry: Button
    private lateinit var btnViewHistory: Button
    private lateinit var btnTimeCapsules: Button
    
    private val authViewModel: AuthViewModel by viewModels()
    private var hasNavigatedToLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = application as JournalForgeApplication

        // Observe auth state - redirect to login if unauthenticated
        authViewModel.authState.observe(this) { authState ->
            android.util.Log.d(TAG, "Auth state changed: $authState")
            
            when (authState) {
                is AuthState.Loading -> {
                    android.util.Log.d(TAG, "Auth state is Loading, showing loading UI")
                    showLoadingUI()
                }
                is AuthState.Unauthenticated -> {
                    android.util.Log.d(TAG, "Auth state is Unauthenticated, redirecting to LoginActivity")
                    navigateToLoginActivity()
                }
                is AuthState.Authenticated -> {
                    android.util.Log.d(TAG, "Auth state is Authenticated, showing main UI")
                    // Only initialize UI on first authenticated state
                    if (!::tvDailyPrompt.isInitialized) {
                        initializeMainUI()
                    }
                }
            }
        }
    }
    
    private fun showLoadingUI() {
        setContentView(R.layout.activity_main)
        // UI will be initialized once authenticated state is received
    }
    
    private fun initializeMainUI() {
        // User is authenticated, proceed with normal initialization
        setContentView(R.layout.activity_main)

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "⚔️ Quest Log"

        // Initialize views
        tvDailyPrompt = findViewById(R.id.tv_daily_prompt)
        tvDailyInsight = findViewById(R.id.tv_daily_insight)
        rvRecentEntries = findViewById(R.id.rv_recent_entries)
        tvNoEntries = findViewById(R.id.tv_no_entries)
        btnNewEntry = findViewById(R.id.btn_new_entry)
        btnViewHistory = findViewById(R.id.btn_view_history)
        btnTimeCapsules = findViewById(R.id.btn_time_capsules)

        // Setup RecyclerView
        rvRecentEntries.layoutManager = LinearLayoutManager(this)

        // Setup button listeners
        btnNewEntry.setOnClickListener {
            startActivity(Intent(this, JournalEntryActivity::class.java))
        }

        btnViewHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnTimeCapsules.setOnClickListener {
            startActivity(Intent(this, TimeCapsuleActivity::class.java))
        }

        // Load data
        loadDailyContent()
        loadRecentEntries()
    }

    override fun onResume() {
        super.onResume()
        
        // Refresh entries when resuming, but only if UI is initialized
        if (::tvDailyPrompt.isInitialized) {
            loadRecentEntries()
        }
    }

    private fun loadDailyContent() {
        lifecycleScope.launch {
            try {
                val prompt = app.aiService.generateDailyPrompt()
                tvDailyPrompt.text = prompt

                val insight = app.aiService.generateDailyInsight()
                tvDailyInsight.text = insight
            } catch (e: Exception) {
                Log.e("MainActivity", "Critical error loading daily content", e)
                // Let the AIService handle the fallback - this should rarely happen
                tvDailyPrompt.text = app.aiService.generateDailyPrompt()
                tvDailyInsight.text = app.aiService.generateDailyInsight()
            }
        }
    }

    private fun loadRecentEntries() {
        lifecycleScope.launch {
            try {
                val entries = app.journalEntryService.getRecentEntries(5)
                if (entries.isEmpty()) {
                    tvNoEntries.visibility = android.view.View.VISIBLE
                    rvRecentEntries.visibility = android.view.View.GONE
                } else {
                    tvNoEntries.visibility = android.view.View.GONE
                    rvRecentEntries.visibility = android.view.View.VISIBLE
                    rvRecentEntries.adapter = EntryAdapter(entries) { entry ->
                        openEntry(entry)
                    }
                }
            } catch (e: Exception) {
                tvNoEntries.visibility = android.view.View.VISIBLE
                tvNoEntries.text = "Error loading entries"
            }
        }
    }

    private fun openEntry(entry: JournalEntry) {
        val intent = Intent(this, JournalEntryActivity::class.java)
        intent.putExtra("ENTRY_ID", entry.id)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_sign_out -> {
                lifecycleScope.launch {
                    app.googleAuthService.signOut()
                    // AuthStateManager will automatically update state
                    // Observer will handle navigation to LoginActivity
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun navigateToLoginActivity() {
        // Prevent multiple navigation attempts
        if (hasNavigatedToLogin) {
            android.util.Log.d(TAG, "Already navigated to login, skipping")
            return
        }
        hasNavigatedToLogin = true
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

class EntryAdapter(
    private val entries: List<JournalEntry>,
    private val onItemClick: (JournalEntry) -> Unit
) : RecyclerView.Adapter<EntryAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_entry_title)
        val tvDate: TextView = view.findViewById(R.id.tv_entry_date)
        val tvPreview: TextView = view.findViewById(R.id.tv_entry_preview)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvTitle.text = entry.title
        holder.tvDate.text = entry.getFormattedDate()
        holder.tvPreview.text = entry.getPreview()
        holder.itemView.setOnClickListener { onItemClick(entry) }
    }

    override fun getItemCount() = entries.size
}