package com.journalforge.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.journalforge.app.JournalForgeApplication
import com.journalforge.app.R
import com.journalforge.app.models.JournalEntry
import kotlinx.coroutines.launch

/**
 * Activity for creating or editing journal entries
 */
class JournalEntryActivity : AppCompatActivity() {
    
    private lateinit var app: JournalForgeApplication
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnSave: Button
    private lateinit var btnAskAI: Button
    private lateinit var btnSuggestEnding: Button
    
    private var currentEntry: JournalEntry? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal_entry)
        
        app = application as JournalForgeApplication
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "📜 New Entry"
        
        // Initialize views
        etTitle = findViewById(R.id.et_entry_title)
        etContent = findViewById(R.id.et_entry_content)
        btnSave = findViewById(R.id.btn_save)
        btnAskAI = findViewById(R.id.btn_ask_ai)
        btnSuggestEnding = findViewById(R.id.btn_suggest_ending)
        
        // Load existing entry if editing
        val entryId = intent.getStringExtra("ENTRY_ID")
        if (entryId != null) {
            loadEntry(entryId)
        }
        
        // Setup listeners
        btnSave.setOnClickListener {
            saveEntry()
        }
        
        btnAskAI.setOnClickListener {
            askAI()
        }
        
        btnSuggestEnding.setOnClickListener {
            suggestEnding()
        }
    }
    
    private fun loadEntry(entryId: String) {
        lifecycleScope.launch {
            val entry = app.journalEntryService.getEntry(entryId)
            if (entry != null) {
                currentEntry = entry
                etTitle.setText(entry.title)
                etContent.setText(entry.content)
                supportActionBar?.title = "📝 Edit Entry"
            }
        }
    }
    
    private fun saveEntry() {
        val title = etTitle.text.toString()
        val content = etContent.text.toString()
        
        if (title.isBlank() || content.isBlank()) {
            Toast.makeText(this, "Please fill in title and content", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            val entry = currentEntry?.copy(
                title = title,
                content = content
            ) ?: JournalEntry(
                title = title,
                content = content
            )
            
            val success = app.journalEntryService.saveEntry(entry)
            if (success) {
                Toast.makeText(this@JournalEntryActivity, R.string.entry_saved, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@JournalEntryActivity, "Failed to save entry", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun askAI() {
        val content = etContent.text.toString()
        if (content.isBlank()) {
            Toast.makeText(this, "Write something first!", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val question = app.aiService.generateProbingQuestion(content)
                Toast.makeText(this@JournalEntryActivity, "AI: $question", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@JournalEntryActivity, "Error generating question", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun suggestEnding() {
        val content = etContent.text.toString()
        if (content.isBlank()) {
            Toast.makeText(this, "Write something first!", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val ending = app.aiService.suggestEnding(content)
                // Append the suggestion to the content
                etContent.setText("$content\n\n$ending")
                etContent.setSelection(etContent.text.length)
                Toast.makeText(this@JournalEntryActivity, "Ending suggestion added!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@JournalEntryActivity, "Error generating ending", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
