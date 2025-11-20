package com.journalforge.app.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.journalforge.app.JournalForgeApplication
import com.journalforge.app.R
import com.journalforge.app.models.TimeCapsule
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * Activity for managing time capsules
 */
class TimeCapsuleActivity : AppCompatActivity() {
    
    private lateinit var app: JournalForgeApplication
    private lateinit var rvReadyCapsules: RecyclerView
    private lateinit var rvSealedCapsules: RecyclerView
    private lateinit var tvNoReadyCapsules: TextView
    private lateinit var tvNoSealedCapsules: TextView
    private lateinit var btnNewCapsule: Button
    private lateinit var fabNewCapsule: FloatingActionButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_capsule)
        
        app = application as JournalForgeApplication
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "⏰ Time Capsules"
        
        // Initialize views
        rvReadyCapsules = findViewById(R.id.rv_ready_capsules)
        rvSealedCapsules = findViewById(R.id.rv_sealed_capsules)
        tvNoReadyCapsules = findViewById(R.id.tv_no_ready_capsules)
        tvNoSealedCapsules = findViewById(R.id.tv_no_sealed_capsules)
        btnNewCapsule = findViewById(R.id.btn_new_capsule)
        fabNewCapsule = findViewById(R.id.fab_new_capsule)
        
        // Setup RecyclerViews
        rvReadyCapsules.layoutManager = LinearLayoutManager(this)
        rvSealedCapsules.layoutManager = LinearLayoutManager(this)
        
        // Setup button listeners
        btnNewCapsule.setOnClickListener { showCreateCapsuleDialog() }
        fabNewCapsule.setOnClickListener { showCreateCapsuleDialog() }
        
        loadCapsules()
    }
    
    override fun onResume() {
        super.onResume()
        loadCapsules()
    }
    
    private fun loadCapsules() {
        lifecycleScope.launch {
            try {
                // Load ready to unseal capsules
                val readyCapsules = app.timeCapsuleService.getReadyToUnsealCapsules()
                if (readyCapsules.isEmpty()) {
                    tvNoReadyCapsules.visibility = View.VISIBLE
                    rvReadyCapsules.visibility = View.GONE
                } else {
                    tvNoReadyCapsules.visibility = View.GONE
                    rvReadyCapsules.visibility = View.VISIBLE
                    rvReadyCapsules.adapter = TimeCapsuleAdapter(
                        readyCapsules,
                        canUnseal = true,
                        onUnsealClick = ::unsealCapsule,
                        onDeleteClick = ::deleteCapsule
                    )
                }
                
                // Load sealed capsules
                val sealedCapsules = app.timeCapsuleService.getSealedCapsules()
                if (sealedCapsules.isEmpty()) {
                    tvNoSealedCapsules.visibility = View.VISIBLE
                    rvSealedCapsules.visibility = View.GONE
                } else {
                    tvNoSealedCapsules.visibility = View.GONE
                    rvSealedCapsules.visibility = View.VISIBLE
                    rvSealedCapsules.adapter = TimeCapsuleAdapter(
                        sealedCapsules,
                        canUnseal = false,
                        onUnsealClick = ::unsealCapsule,
                        onDeleteClick = ::deleteCapsule
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(this@TimeCapsuleActivity, "Error loading capsules", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showCreateCapsuleDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_capsule, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.et_capsule_title)
        val etMessage = dialogView.findViewById<EditText>(R.id.et_capsule_message)
        val btnSelectDate = dialogView.findViewById<Button>(R.id.btn_select_unseal_date)
        val tvSelectedDate = dialogView.findViewById<TextView>(R.id.tv_selected_date)
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 1) // Default: 1 month from now
        var selectedDate = calendar.time
        tvSelectedDate.text = "Unseal on: ${TimeCapsule("", "", "", Date(), selectedDate).getFormattedUnsealDate()}"
        
        btnSelectDate.setOnClickListener {
            val datePickerCalendar = Calendar.getInstance()
            datePickerCalendar.time = selectedDate
            
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.time
                    tvSelectedDate.text = "Unseal on: ${TimeCapsule("", "", "", Date(), selectedDate).getFormattedUnsealDate()}"
                },
                datePickerCalendar.get(Calendar.YEAR),
                datePickerCalendar.get(Calendar.MONTH),
                datePickerCalendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }
        
        AlertDialog.Builder(this)
            .setTitle("⏰ Create Time Capsule")
            .setView(dialogView)
            .setPositiveButton("Seal") { _, _ ->
                val title = etTitle.text.toString().trim()
                val message = etMessage.text.toString().trim()
                
                if (title.isBlank() || message.isBlank()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val capsule = TimeCapsule(
                    title = title,
                    message = message,
                    unsealDate = selectedDate
                )
                
                lifecycleScope.launch {
                    val success = app.timeCapsuleService.saveCapsule(capsule)
                    if (success) {
                        Toast.makeText(
                            this@TimeCapsuleActivity,
                            "Time capsule sealed! 🔒",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadCapsules()
                    } else {
                        Toast.makeText(
                            this@TimeCapsuleActivity,
                            "Failed to create capsule",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun unsealCapsule(capsule: TimeCapsule) {
        if (!capsule.canUnseal()) {
            Toast.makeText(this, "This capsule isn't ready to unseal yet!", Toast.LENGTH_SHORT).show()
            return
        }
        
        AlertDialog.Builder(this)
            .setTitle("📖 Open Time Capsule")
            .setMessage("${capsule.title}\n\nCreated: ${capsule.getFormattedCreatedDate()}\n\n${capsule.message}")
            .setPositiveButton("Keep") { _, _ ->
                lifecycleScope.launch {
                    app.timeCapsuleService.unsealCapsule(capsule.id)
                    loadCapsules()
                }
            }
            .setNegativeButton("Delete") { _, _ ->
                deleteCapsule(capsule)
            }
            .show()
    }
    
    private fun deleteCapsule(capsule: TimeCapsule) {
        AlertDialog.Builder(this)
            .setTitle("Delete Capsule")
            .setMessage("Are you sure you want to delete \"${capsule.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    app.timeCapsuleService.deleteCapsule(capsule.id)
                    loadCapsules()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

/**
 * Adapter for time capsules
 */
class TimeCapsuleAdapter(
    private val capsules: List<TimeCapsule>,
    private val canUnseal: Boolean,
    private val onUnsealClick: (TimeCapsule) -> Unit,
    private val onDeleteClick: (TimeCapsule) -> Unit
) : RecyclerView.Adapter<TimeCapsuleAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_capsule_title)
        val tvDate: TextView = view.findViewById(R.id.tv_capsule_date)
        val tvPreview: TextView = view.findViewById(R.id.tv_capsule_preview)
        val btnUnseal: Button = view.findViewById(R.id.btn_unseal)
        val btnDelete: Button = view.findViewById(R.id.btn_delete)
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_capsule, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val capsule = capsules[position]
        holder.tvTitle.text = capsule.title
        holder.tvDate.text = "Unseal on: ${capsule.getFormattedUnsealDate()}"
        holder.tvPreview.text = if (capsule.isSealed) {
            "🔒 Sealed until ${capsule.getFormattedUnsealDate()}"
        } else {
            capsule.message
        }
        
        if (canUnseal) {
            holder.btnUnseal.visibility = View.VISIBLE
            holder.btnUnseal.setOnClickListener { onUnsealClick(capsule) }
        } else {
            holder.btnUnseal.visibility = View.GONE
        }
        
        holder.btnDelete.setOnClickListener { onDeleteClick(capsule) }
    }
    
    override fun getItemCount() = capsules.size
}
