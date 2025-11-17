package com.journalforge.app.models

import com.google.gson.annotations.SerializedName
import java.util.Date
import java.util.UUID

/**
 * Represents a journal entry
 */
data class JournalEntry(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    
    @SerializedName("title")
    var title: String = "",
    
    @SerializedName("content")
    var content: String = "",
    
    @SerializedName("createdDate")
    val createdDate: Date = Date(),
    
    @SerializedName("modifiedDate")
    var modifiedDate: Date = Date(),
    
    @SerializedName("mood")
    var mood: String? = null,
    
    @SerializedName("tags")
    var tags: List<String> = emptyList(),
    
    @SerializedName("aiConversation")
    var aiConversation: List<AIMessage> = emptyList()
) {
    fun getFormattedDate(): String {
        val formatter = java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault())
        return formatter.format(createdDate)
    }
    
    fun getPreview(maxLength: Int = 150): String {
        return if (content.length > maxLength) {
            content.substring(0, maxLength) + "..."
        } else {
            content
        }
    }
}

/**
 * Represents an AI message in the conversation
 */
data class AIMessage(
    @SerializedName("role")
    val role: String, // "user" or "assistant"
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("timestamp")
    val timestamp: Date = Date()
)
