package com.journalforge.app.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.journalforge.app.R

/**
 * Activity for managing time capsules
 */
class TimeCapsuleActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_capsule)
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "⏰ Time Capsules"
        
        findViewById<TextView>(R.id.tv_placeholder).text = 
            "Time Capsules coming soon!\n\nSeal messages to your future self and unseal them on a specific date."
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
