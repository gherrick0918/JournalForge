package com.journalforge.app.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a user profile from Google Sign-In
 */
data class UserProfile(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("email")
    val email: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("photoUrl")
    val photoUrl: String? = null
)
