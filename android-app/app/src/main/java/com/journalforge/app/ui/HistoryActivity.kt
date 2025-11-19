package com.journalforge.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.journalforge.app.JournalForgeApplication
import com.journalforge.app.R
import com.journalforge.app.models.JournalEntry
import kotlinx.coroutines.launch

/**
 * Activity for viewing journal entry history
 */
class HistoryActivity : AppCompatActivity() {
    
    private lateinit var app: JournalForgeApplication
    private lateinit var rvEntries: RecyclerView
    private lateinit var tvNoEntries: TextView
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnSortNewest: Button
    private lateinit var btnSortOldest: Button
    
    private var allEntries = listOf<JournalEntry>()
    private var displayedEntries = listOf<JournalEntry>()
    private var sortNewestFirst = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        app = application as JournalForgeApplication
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "📚 Chronicle"
        
        // Initialize views
        rvEntries = findViewById(R.id.rv_entries)
        tvNoEntries = findViewById(R.id.tv_no_entries)
        etSearch = findViewById(R.id.et_search)
        btnSortNewest = findViewById(R.id.btn_sort_newest)
        btnSortOldest = findViewById(R.id.btn_sort_oldest)
        
        // Setup RecyclerView
        rvEntries.layoutManager = LinearLayoutManager(this)
        
        // Setup search
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterEntries(s?.toString() ?: "")
            }
        })
        
        // Setup sort buttons
        btnSortNewest.setOnClickListener {
            sortNewestFirst = true
            updateSortButtons()
            sortAndDisplayEntries()
        }
        
        btnSortOldest.setOnClickListener {
            sortNewestFirst = false
            updateSortButtons()
            sortAndDisplayEntries()
        }
        
        updateSortButtons()
        loadEntries()
    }
    
    override fun onResume() {
        super.onResume()
        loadEntries()
    }
    
    private fun updateSortButtons() {
        if (sortNewestFirst) {
            btnSortNewest.setTextColor(getColor(R.color.gold))
            btnSortOldest.setTextColor(getColor(R.color.stone_gray))
        } else {
            btnSortNewest.setTextColor(getColor(R.color.stone_gray))
            btnSortOldest.setTextColor(getColor(R.color.gold))
        }
    }
    
    private fun loadEntries() {
        lifecycleScope.launch {
            try {
                allEntries = app.journalEntryService.getAllEntries()
                sortAndDisplayEntries()
            } catch (e: Exception) {
                tvNoEntries.visibility = View.VISIBLE
                tvNoEntries.text = "Error loading entries"
                rvEntries.visibility = View.GONE
            }
        }
    }
    
    private fun filterEntries(query: String) {
        displayedEntries = if (query.isBlank()) {
            allEntries
        } else {
            allEntries.filter { entry ->
                entry.title.contains(query, ignoreCase = true) ||
                entry.content.contains(query, ignoreCase = true)
            }
        }
        sortAndDisplayEntries()
    }
    
    private fun sortAndDisplayEntries() {
        val sortedEntries = if (sortNewestFirst) {
            displayedEntries.sortedByDescending { it.timestamp }
        } else {
            displayedEntries.sortedBy { it.timestamp }
        }
        
        if (sortedEntries.isEmpty()) {
            tvNoEntries.visibility = View.VISIBLE
            rvEntries.visibility = View.GONE
        } else {
            tvNoEntries.visibility = View.GONE
            rvEntries.visibility = View.VISIBLE
            rvEntries.adapter = HistoryAdapter(sortedEntries, ::openEntry, ::deleteEntry)
        }
    }
    
    private fun openEntry(entry: JournalEntry) {
        val intent = Intent(this, JournalEntryActivity::class.java)
        intent.putExtra("ENTRY_ID", entry.id)
        startActivity(intent)
    }
    
    private fun deleteEntry(entry: JournalEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete \"${entry.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    app.journalEntryService.deleteEntry(entry.id)
                    loadEntries()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

/**
 * Adapter for history entries
 */
class HistoryAdapter(
    private val entries: List<JournalEntry>,
    private val onItemClick: (JournalEntry) -> Unit,
    private val onDeleteClick: (JournalEntry) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_entry_title)
        val tvDate: TextView = view.findViewById(R.id.tv_entry_date)
        val tvPreview: TextView = view.findViewById(R.id.tv_entry_preview)
        val btnDelete: View = view.findViewById(R.id.btn_delete)
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_entry, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvTitle.text = entry.title
        holder.tvDate.text = entry.getFormattedDate()
        holder.tvPreview.text = entry.getPreview()
        holder.itemView.setOnClickListener { onItemClick(entry) }
        holder.btnDelete.setOnClickListener { onDeleteClick(entry) }
    }
    
    override fun getItemCount() = entries.size
}
