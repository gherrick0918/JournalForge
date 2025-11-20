package com.journalforge.app.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.journalforge.app.JournalForgeApplication
import com.journalforge.app.R
import com.journalforge.app.models.ChatMessage
import com.journalforge.app.models.JournalEntry
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Activity for creating or editing journal entries with AI conversation
 */
class JournalEntryActivity : AppCompatActivity() {
    
    private lateinit var app: JournalForgeApplication
    private lateinit var etTitle: EditText
    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etMessageInput: TextInputEditText
    private lateinit var btnSend: Button
    private lateinit var btnVoiceInput: Button
    private lateinit var btnSave: Button
    
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter
    
    private var currentEntry: JournalEntry? = null
    
    // Speech recognition launcher
    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.firstOrNull()?.let { text ->
                etMessageInput.setText(text)
                // Automatically send the message after speech recognition
                sendMessage()
            }
        }
    }
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognition()
        } else {
            Toast.makeText(this, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal_entry)
        
        app = application as JournalForgeApplication
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "📜 New Entry"
        
        // Initialize views
        etTitle = findViewById(R.id.et_entry_title)
        rvChatMessages = findViewById(R.id.rv_chat_messages)
        etMessageInput = findViewById(R.id.et_message_input)
        btnSend = findViewById(R.id.btn_send)
        btnVoiceInput = findViewById(R.id.btn_voice_input)
        btnSave = findViewById(R.id.btn_save)
        
        // Setup RecyclerView
        chatAdapter = ChatAdapter(chatMessages)
        rvChatMessages.apply {
            layoutManager = LinearLayoutManager(this@JournalEntryActivity)
            adapter = chatAdapter
        }
        
        // Load existing entry if editing
        val entryId = intent.getStringExtra("ENTRY_ID")
        if (entryId != null) {
            loadEntry(entryId)
        } else {
            // Add initial AI greeting for new entries - more conversational
            addAIMessage("⚔️ Welcome back, adventurer! I'm here to help you explore your thoughts. What's on your mind today?")
        }
        
        // Setup listeners
        btnSend.setOnClickListener {
            sendMessage()
        }
        
        btnVoiceInput.setOnClickListener {
            requestVoiceInput()
        }
        
        btnSave.setOnClickListener {
            saveEntry()
        }
    }
    
    private fun loadEntry(entryId: String) {
        lifecycleScope.launch {
            val entry = app.journalEntryService.getEntry(entryId)
            if (entry != null) {
                currentEntry = entry
                etTitle.setText(entry.title)
                
                // Restore conversation from aiConversation if available
                if (entry.aiConversation.isNotEmpty()) {
                    entry.aiConversation.forEach { aiMsg ->
                        val chatMsg = ChatMessage(
                            content = aiMsg.content,
                            isFromUser = aiMsg.role == "user",
                            timestamp = aiMsg.timestamp.time
                        )
                        chatMessages.add(chatMsg)
                    }
                    chatAdapter.notifyDataSetChanged()
                } else {
                    // Fallback: if no conversation stored, show content as single user message
                    val userMessage = ChatMessage(entry.content, isFromUser = true)
                    chatMessages.add(userMessage)
                    chatAdapter.notifyItemInserted(chatMessages.size - 1)
                }
                
                supportActionBar?.title = "📝 Edit Entry"
            }
        }
    }
    
    private fun sendMessage() {
        val messageText = etMessageInput.text.toString().trim()
        if (messageText.isBlank()) {
            Toast.makeText(this, "Please write something first!", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Add user message
        addUserMessage(messageText)
        etMessageInput.text?.clear()
        
        // Get AI response
        getAIResponse(messageText)
    }
    
    private fun addUserMessage(message: String) {
        val chatMessage = ChatMessage(message, isFromUser = true)
        chatMessages.add(chatMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        rvChatMessages.smoothScrollToPosition(chatMessages.size - 1)
    }
    
    private fun addAIMessage(message: String) {
        val chatMessage = ChatMessage(message, isFromUser = false)
        chatMessages.add(chatMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        rvChatMessages.smoothScrollToPosition(chatMessages.size - 1)
    }
    
    private fun getAIResponse(userMessage: String) {
        lifecycleScope.launch {
            try {
                // Build conversation history from recent messages (last 10 messages)
                val conversationHistory = chatMessages
                    .takeLast(10)
                    .map { it.content }
                
                val response = app.aiService.generateConversationalResponse(userMessage, conversationHistory)
                addAIMessage(response)
            } catch (e: Exception) {
                addAIMessage("🔮 I sense your thoughts are powerful. Continue writing, adventurer!")
            }
        }
    }
    
    private fun requestVoiceInput() {
        // Check for microphone permission
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startSpeechRecognition()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                Toast.makeText(
                    this,
                    "Microphone permission is needed for voice input",
                    Toast.LENGTH_LONG
                ).show()
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your thoughts...")
        }
        
        try {
            speechRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Speech recognition not available on this device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun saveEntry() {
        // Generate default title if empty
        val title = etTitle.text.toString().trim().ifBlank {
            generateDefaultTitle()
        }
        
        if (chatMessages.isEmpty()) {
            Toast.makeText(this, "Please write something first!", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Format the full conversation for display
        val content = chatMessages.joinToString("\n\n") { msg ->
            if (msg.isFromUser) {
                "You: ${msg.content}"
            } else {
                "Guide: ${msg.content}"
            }
        }
        
        // Check if user wrote anything
        val hasUserContent = chatMessages.any { it.isFromUser && it.content.isNotBlank() }
        if (!hasUserContent) {
            Toast.makeText(this, "Please write something first!", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Convert chat messages to AIMessage format for storage
        val aiConversation = chatMessages.map { chatMsg ->
            com.journalforge.app.models.AIMessage(
                role = if (chatMsg.isFromUser) "user" else "assistant",
                content = chatMsg.content,
                timestamp = java.util.Date(chatMsg.timestamp)
            )
        }
        
        lifecycleScope.launch {
            val entry = currentEntry?.copy(
                title = title,
                content = content,
                aiConversation = aiConversation
            ) ?: JournalEntry(
                title = title,
                content = content,
                aiConversation = aiConversation
            )
            
            val success = app.journalEntryService.saveEntry(entry)
            if (success) {
                Toast.makeText(this@JournalEntryActivity, R.string.entry_saved, Toast.LENGTH_SHORT).show()
                // Show daily insight after saving
                showDailyInsight()
            } else {
                Toast.makeText(this@JournalEntryActivity, "Failed to save entry", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun generateDefaultTitle(): String {
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault())
        return "Entry - ${dateFormat.format(calendar.time)}"
    }
    
    private fun showDailyInsight() {
        lifecycleScope.launch {
            try {
                // Extract user messages to create content summary
                val userMessages = chatMessages
                    .filter { it.isFromUser }
                    .joinToString(" ") { it.content }
                
                val insight = app.aiService.generateDailyInsight(userMessages)
                
                // Show insight in a dialog
                androidx.appcompat.app.AlertDialog.Builder(this@JournalEntryActivity)
                    .setTitle("✨ Daily Insight")
                    .setMessage(insight)
                    .setPositiveButton("Continue Quest") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            } catch (e: Exception) {
                // If insight fails, just finish normally
                finish()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

/**
 * Adapter for chat messages
 */
class ChatAdapter(
    private val messages: List<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isFromUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = android.view.LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            val view = inflater.inflate(R.layout.item_chat_message_user, parent, false)
            UserMessageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_message_ai, parent, false)
            AIMessageViewHolder(view)
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AIMessageViewHolder -> holder.bind(message)
        }
    }
    
    override fun getItemCount() = messages.size
    
    class UserMessageViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val tvContent: android.widget.TextView = view.findViewById(R.id.tv_message_content)
        
        fun bind(message: ChatMessage) {
            tvContent.text = message.content
        }
    }
    
    class AIMessageViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val tvContent: android.widget.TextView = view.findViewById(R.id.tv_message_content)
        
        fun bind(message: ChatMessage) {
            tvContent.text = message.content
        }
    }
}
