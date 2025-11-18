package com.journalforge.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.journalforge.app.JournalForgeApplication
import com.journalforge.app.R
import com.journalforge.app.models.JournalEntry
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var app: JournalForgeApplication
    private lateinit var tvDailyPrompt: TextView
    private lateinit var tvDailyInsight: TextView
    private lateinit var rvRecentEntries: RecyclerView
    private lateinit var tvNoEntries: TextView
    private lateinit var btnNewEntry: Button
    private lateinit var btnViewHistory: Button
    private lateinit var btnTimeCapsules: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = application as JournalForgeApplication

        // Check if we just completed authentication
        val prefs = getSharedPreferences("auth_state", MODE_PRIVATE)
        val justAuthenticated = prefs.getBoolean("just_authenticated", false)
        
        if (justAuthenticated) {
            // Clear the flag immediately
            prefs.edit().putBoolean("just_authenticated", false).apply()
            android.util.Log.d("MainActivity", "Just authenticated, trusting LoginActivity's verification completely")
            
            // Trust LoginActivity's verification completely - it already waited for auth state
            // to stabilize with retries and extra propagation time. Checking again here creates
            // a race condition that can cause both activities to finish and the app to exit.
            
            // Auth state is trusted, proceed with initialization
        } else {
            // Normal startup - check auth state
            if (!app.googleAuthService.isSignedIn()) {
                android.util.Log.d("MainActivity", "User not signed in, redirecting to LoginActivity")
                // Clear the activity stack to prevent back navigation to MainActivity
                // Also set a flag to prevent LoginActivity from auto-redirecting if auth state is stale
                prefs.edit().putBoolean("force_login_ui", true).apply()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return
            }
        }

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
        
        // Check auth state when resuming
        // If user is not signed in and we didn't just authenticate, redirect to login
        val prefs = getSharedPreferences("auth_state", MODE_PRIVATE)
        val justAuthenticated = prefs.getBoolean("just_authenticated", false)
        
        if (!app.googleAuthService.isSignedIn() && !justAuthenticated) {
            android.util.Log.d("MainActivity", "User no longer signed in, redirecting to LoginActivity")
            prefs.edit().putBoolean("force_login_ui", true).apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        // Refresh entries when returning to this activity
        if (app.googleAuthService.isSignedIn()) {
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
                tvDailyPrompt.text = "⚔️ What challenge did you face today?"
                tvDailyInsight.text = "🔮 Begin your journaling quest!"
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
                    // Clear the activity stack to prevent back navigation to MainActivity
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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