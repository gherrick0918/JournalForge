package com.journalforge.app.models

import com.google.gson.annotations.SerializedName

/**
 * Application settings
 */
data class AppSettings(
    @SerializedName("openAIApiKey")
    val openAIApiKey: String? = null,
    
    @SerializedName("openAIModel")
    val openAIModel: String = "gpt-4o-mini",
    
    @SerializedName("firebaseApiKey")
    val firebaseApiKey: String? = null,
    
    @SerializedName("googleClientId")
    val googleClientId: String? = null
)
