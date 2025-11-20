package com.journalforge.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
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
    private lateinit var btnSortNewest: MaterialButton
    private lateinit var btnSortOldest: MaterialButton
    private lateinit var btnGenerateSummary: MaterialButton
    private lateinit var btnSemanticSearch: MaterialButton

    private var allEntries = listOf<JournalEntry>()
    private var displayedEntries = listOf<JournalEntry>()
    private var sortNewestFirst = true
    private var useSemanticSearch = false

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
        btnGenerateSummary = findViewById(R.id.btn_generate_summary)
        btnSemanticSearch = findViewById(R.id.btn_semantic_search)

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

        btnGenerateSummary.setOnClickListener {
            generateJournalSummary()
        }

        btnSemanticSearch.setOnClickListener {
            useSemanticSearch = !useSemanticSearch
            updateSemanticSearchButton()
            // Re-trigger search if there's a query
            val query = etSearch.text.toString()
            if (query.isNotBlank()) {
                filterEntries(query)
            }
        }

        updateSortButtons()
        updateSemanticSearchButton()
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

    private fun updateSemanticSearchButton() {
        if (useSemanticSearch) {
            btnSemanticSearch.strokeColor = getColorStateList(R.color.gold)
            btnSemanticSearch.setTextColor(getColor(R.color.gold))
        } else {
            btnSemanticSearch.strokeColor = getColorStateList(R.color.stone_gray)
            btnSemanticSearch.setTextColor(getColor(R.color.stone_gray))
        }
    }

    private fun loadEntries() {
        lifecycleScope.launch {
            try {
                allEntries = app.journalEntryService.getAllEntries()
                displayedEntries = allEntries  // Initialize displayedEntries with all entries
                sortAndDisplayEntries()
            } catch (e: Exception) {
                tvNoEntries.visibility = View.VISIBLE
                tvNoEntries.text = "Error loading entries"
                rvEntries.visibility = View.GONE
            }
        }
    }

    private fun filterEntries(query: String) {
        if (query.isBlank()) {
            displayedEntries = allEntries
            sortAndDisplayEntries()
            return
        }

        if (useSemanticSearch) {
            // Use AI semantic search
            lifecycleScope.launch {
                try {
                    val entriesWithIds = allEntries.map { it.id to it.content }
                    val results = app.aiService.semanticSearch(query, entriesWithIds)
                    val relevantIds = results.map { it.first }.toSet()

                    displayedEntries = allEntries.filter { it.id in relevantIds }
                        .sortedBy { entry ->
                            // Sort by semantic relevance
                            results.indexOfFirst { it.first == entry.id }
                        }

                    sortAndDisplayEntries()
                } catch (e: Exception) {
                    // Fallback to keyword search on error
                    displayedEntries = allEntries.filter { entry ->
                        entry.title.contains(query, ignoreCase = true) ||
                        entry.content.contains(query, ignoreCase = true)
                    }
                    sortAndDisplayEntries()
                }
            }
        } else {
            // Use standard keyword search
            displayedEntries = allEntries.filter { entry ->
                entry.title.contains(query, ignoreCase = true) ||
                entry.content.contains(query, ignoreCase = true)
            }
            sortAndDisplayEntries()
        }
    }

    private fun sortAndDisplayEntries() {
        val sortedEntries = if (sortNewestFirst) {
            displayedEntries.sortedByDescending { it.createdDate }
        } else {
            displayedEntries.sortedBy { it.createdDate }
        }

        if (sortedEntries.isEmpty()) {
            tvNoEntries.visibility = View.VISIBLE
            rvEntries.visibility = View.GONE
        } else {
            tvNoEntries.visibility = View.GONE
            rvEntries.visibility = View.VISIBLE
            rvEntries.adapter = HistoryAdapter(sortedEntries, ::openEntry, ::analyzeEntry, ::deleteEntry)
        }
    }

    private fun generateJournalSummary() {
        if (allEntries.isEmpty()) {
            Toast.makeText(this, "No entries to summarize", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                btnGenerateSummary.isEnabled = false
                btnGenerateSummary.text = "⏳ Generating..."

                val entryContents = allEntries.take(10).map { entry ->
                    "${entry.title}: ${entry.content}"
                }

                val summary = app.aiService.generateJournalSummary(entryContents, "recent")

                androidx.appcompat.app.AlertDialog.Builder(this@HistoryActivity)
                    .setTitle("📊 Journal Summary")
                    .setMessage(summary)
                    .setPositiveButton("Close", null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@HistoryActivity, "Could not generate summary", Toast.LENGTH_SHORT).show()
            } finally {
                btnGenerateSummary.isEnabled = true
                btnGenerateSummary.text = "🤖 Generate AI Summary"
            }
        }
    }

    private fun analyzeEntry(entry: JournalEntry) {
        lifecycleScope.launch {
            try {
                val progressDialog = androidx.appcompat.app.AlertDialog.Builder(this@HistoryActivity)
                    .setTitle("🤖 Analyzing Entry...")
                    .setMessage("Please wait while AI analyzes this entry...")
                    .setCancelable(false)
                    .create()
                progressDialog.show()

                val analysis = app.aiService.analyzeEntry(entry.content)

                progressDialog.dismiss()

                androidx.appcompat.app.AlertDialog.Builder(this@HistoryActivity)
                    .setTitle("💡 Entry Analysis")
                    .setMessage("\"${entry.title}\"\n\n$analysis")
                    .setPositiveButton("Close", null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@HistoryActivity, "Could not analyze entry", Toast.LENGTH_SHORT).show()
            }
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
    private val onAnalyzeClick: (JournalEntry) -> Unit,
    private val onDeleteClick: (JournalEntry) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_entry_title)
        val tvDate: TextView = view.findViewById(R.id.tv_entry_date)
        val tvPreview: TextView = view.findViewById(R.id.tv_entry_preview)
        val btnAnalyze: View = view.findViewById(R.id.btn_analyze)
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
        holder.btnAnalyze.setOnClickListener { onAnalyzeClick(entry) }
        holder.btnDelete.setOnClickListener { onDeleteClick(entry) }
    }

    override fun getItemCount() = entries.size
}
