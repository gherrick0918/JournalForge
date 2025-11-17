package com.journalforge.app.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.journalforge.app.R

/**
 * Activity for viewing journal entry history
 */
class HistoryActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "📚 Chronicle"
        
        findViewById<TextView>(R.id.tv_placeholder).text = 
            "History view coming soon!\n\nThis will show all your journal entries with search and filter options."
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
