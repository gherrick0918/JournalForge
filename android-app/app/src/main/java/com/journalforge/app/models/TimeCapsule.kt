package com.journalforge.app.models

import com.google.gson.annotations.SerializedName
import java.util.Date
import java.util.UUID

/**
 * Represents a time capsule - a journal entry sealed until a future date
 */
data class TimeCapsule(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    
    @SerializedName("title")
    var title: String = "",
    
    @SerializedName("message")
    var message: String = "",
    
    @SerializedName("createdDate")
    val createdDate: Date = Date(),
    
    @SerializedName("unsealDate")
    var unsealDate: Date = Date(),
    
    @SerializedName("isSealed")
    var isSealed: Boolean = true
) {
    fun canUnseal(): Boolean {
        return isSealed && Date().after(unsealDate)
    }
    
    fun getFormattedUnsealDate(): String {
        val formatter = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
        return formatter.format(unsealDate)
    }
    
    fun getFormattedCreatedDate(): String {
        val formatter = java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault())
        return formatter.format(createdDate)
    }
}
