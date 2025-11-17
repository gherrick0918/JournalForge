package com.journalforge.app.services

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.journalforge.app.models.JournalEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

/**
 * Service for managing journal entries with local JSON file storage
 */
class JournalEntryService(private val context: Context) {
    
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()
    
    private val entriesFile: File
        get() = File(context.filesDir, "journal_entries.json")
    
    /**
     * Get all journal entries
     */
    suspend fun getAllEntries(): List<JournalEntry> = withContext(Dispatchers.IO) {
        try {
            if (!entriesFile.exists()) {
                return@withContext emptyList()
            }
            
            val json = entriesFile.readText()
            val type = object : TypeToken<List<JournalEntry>>() {}.type
            gson.fromJson<List<JournalEntry>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading entries", e)
            emptyList()
        }
    }
    
    /**
     * Get a single entry by ID
     */
    suspend fun getEntry(id: String): JournalEntry? = withContext(Dispatchers.IO) {
        getAllEntries().find { it.id == id }
    }
    
    /**
     * Save a new entry or update an existing one
     */
    suspend fun saveEntry(entry: JournalEntry): Boolean = withContext(Dispatchers.IO) {
        try {
            val entries = getAllEntries().toMutableList()
            
            // Update modified date
            entry.modifiedDate = Date()
            
            // Remove old version if exists and add new
            entries.removeIf { it.id == entry.id }
            entries.add(entry)
            
            // Sort by created date (newest first)
            entries.sortByDescending { it.createdDate }
            
            // Save to file
            val json = gson.toJson(entries)
            entriesFile.writeText(json)
            
            Log.d(TAG, "Entry saved successfully: ${entry.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving entry", e)
            false
        }
    }
    
    /**
     * Delete an entry
     */
    suspend fun deleteEntry(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val entries = getAllEntries().toMutableList()
            val removed = entries.removeIf { it.id == id }
            
            if (removed) {
                val json = gson.toJson(entries)
                entriesFile.writeText(json)
                Log.d(TAG, "Entry deleted: $id")
            }
            
            removed
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting entry", e)
            false
        }
    }
    
    /**
     * Search entries by text
     */
    suspend fun searchEntries(query: String): List<JournalEntry> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext getAllEntries()
        }
        
        val lowerQuery = query.lowercase()
        getAllEntries().filter { entry ->
            entry.title.lowercase().contains(lowerQuery) ||
            entry.content.lowercase().contains(lowerQuery) ||
            entry.tags.any { it.lowercase().contains(lowerQuery) }
        }
    }
    
    /**
     * Get recent entries (last N entries)
     */
    suspend fun getRecentEntries(count: Int = 5): List<JournalEntry> = withContext(Dispatchers.IO) {
        getAllEntries().take(count)
    }
    
    /**
     * Export entries to JSON string
     */
    suspend fun exportToJson(entries: List<JournalEntry>): String = withContext(Dispatchers.IO) {
        gson.toJson(entries)
    }
    
    /**
     * Export single entry to text format
     */
    suspend fun exportToText(entry: JournalEntry): String = withContext(Dispatchers.IO) {
        buildString {
            appendLine("=" .repeat(50))
            appendLine(entry.title)
            appendLine("=" .repeat(50))
            appendLine()
            appendLine("Created: ${entry.getFormattedDate()}")
            if (entry.mood != null) {
                appendLine("Mood: ${entry.mood}")
            }
            if (entry.tags.isNotEmpty()) {
                appendLine("Tags: ${entry.tags.joinToString(", ")}")
            }
            appendLine()
            appendLine(entry.content)
            appendLine()
            
            if (entry.aiConversation.isNotEmpty()) {
                appendLine("-" .repeat(50))
                appendLine("AI Conversation:")
                appendLine("-" .repeat(50))
                entry.aiConversation.forEach { msg ->
                    appendLine("${msg.role.uppercase()}: ${msg.content}")
                    appendLine()
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "JournalEntryService"
    }
}
