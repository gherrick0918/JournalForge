package com.journalforge.app.models

/**
 * Represents a chat message in the AI conversation
 */
data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
