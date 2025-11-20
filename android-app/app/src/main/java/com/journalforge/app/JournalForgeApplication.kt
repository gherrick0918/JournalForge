package com.journalforge.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.journalforge.app.models.AppSettings
import com.journalforge.app.services.AIService
import com.journalforge.app.services.GoogleAuthService
import com.journalforge.app.services.JournalEntryService
import com.journalforge.app.services.TimeCapsuleService

/**
 * Application class for JournalForge
 */
class JournalForgeApplication : Application() {
    
    // Services
    lateinit var journalEntryService: JournalEntryService
        private set
    
    lateinit var googleAuthService: GoogleAuthService
        private set
    
    lateinit var aiService: AIService
        private set
    
    lateinit var timeCapsuleService: TimeCapsuleService
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Load settings from BuildConfig
        val settings = AppSettings(
            openAIApiKey = if (BuildConfig.OPENAI_API_KEY.isNotBlank()) BuildConfig.OPENAI_API_KEY else null,
            openAIModel = BuildConfig.OPENAI_MODEL
        )
        
        // Initialize services
        journalEntryService = JournalEntryService(this)
        googleAuthService = GoogleAuthService(this)
        aiService = AIService(settings)
        timeCapsuleService = TimeCapsuleService(this)
    }
}
