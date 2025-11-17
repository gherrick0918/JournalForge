package com.journalforge.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.journalforge.app.services.AIService
import com.journalforge.app.services.GoogleAuthService
import com.journalforge.app.services.JournalEntryService

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
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize services
        journalEntryService = JournalEntryService(this)
        googleAuthService = GoogleAuthService(this)
        aiService = AIService(null) // TODO: Load settings from file
    }
}
