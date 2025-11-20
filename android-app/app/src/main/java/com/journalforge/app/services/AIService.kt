package com.journalforge.app.services

import android.util.Log
import com.google.gson.Gson
import com.journalforge.app.models.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Service for AI-powered journaling features using OpenAI API
 */
class AIService(private val settings: AppSettings?) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    /**
     * Generate a daily prompt
     */
    suspend fun generateDailyPrompt(): String = withContext(Dispatchers.IO) {
        if (settings?.openAIApiKey.isNullOrBlank()) {
            return@withContext getMockPrompt()
        }
        
        try {
            val response = callOpenAI(
                "Generate a creative and thoughtful journaling prompt in RPG/fantasy style. Keep it brief (1-2 sentences).",
                systemPrompt = "You are a wise sage guiding adventurers on their journaling quest."
            )
            response ?: getMockPrompt()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating prompt", e)
            getMockPrompt()
        }
    }
    
    /**
     * Generate a probing question based on journal content
     */
    suspend fun generateProbingQuestion(entryContent: String, conversationHistory: List<String> = emptyList()): String = withContext(Dispatchers.IO) {
        if (settings?.openAIApiKey.isNullOrBlank()) {
            return@withContext getMockQuestion()
        }
        
        try {
            val response = callOpenAI(
                "Based on this journal entry, ask a thoughtful probing question to help the writer explore their thoughts deeper: \"$entryContent\"",
                systemPrompt = "You are a wise companion helping adventurers reflect on their journey. Ask insightful questions to deepen their self-awareness. Be creative and vary your questions.",
                conversationHistory = conversationHistory
            )
            response ?: getMockQuestion()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating question", e)
            getMockQuestion()
        }
    }
    
    /**
     * Generate a conversational response that directly addresses what the user said
     */
    suspend fun generateConversationalResponse(userMessage: String, conversationHistory: List<String> = emptyList()): String = withContext(Dispatchers.IO) {
        if (settings?.openAIApiKey.isNullOrBlank()) {
            return@withContext getMockConversationalResponse(userMessage)
        }
        
        try {
            // Build context from recent messages
            val contextSummary = if (conversationHistory.size > 2) {
                "Previous conversation context: ${conversationHistory.takeLast(4).joinToString(" | ")}"
            } else ""
            
            val prompt = buildString {
                if (contextSummary.isNotEmpty()) {
                    append("$contextSummary\n\n")
                }
                append("User just said: \"$userMessage\"\n\n")
                append("Respond warmly and specifically to what they shared. ")
                append("Reference the specific feelings, situations, or topics they mentioned. ")
                append("Then ask ONE thoughtful follow-up question that shows you're truly listening and want to understand deeper. ")
                append("Be empathetic, validating, and conversational.")
            }
            
            val response = callOpenAI(
                prompt,
                systemPrompt = """You are a warm, empathetic companion helping someone explore their thoughts and feelings through journaling. 
                    |Your role is to have a genuine, supportive conversation.
                    |
                    |CRITICAL RULES:
                    |- ALWAYS acknowledge and respond to what the user JUST said
                    |- Reference specific words, feelings, or situations they mentioned
                    |- Show deep empathy and validation, especially for difficult emotions
                    |- When they share struggles (anxiety, depression, stress, etc.), acknowledge those feelings before asking questions
                    |- Ask ONE relevant follow-up question that directly relates to what they shared
                    |- Vary your responses: sometimes validate feelings, sometimes explore causes, sometimes ask about coping
                    |- Be conversational and natural (2-3 sentences max)
                    |- Use light RPG/fantasy elements occasionally, but prioritize genuine support
                    |- Never give generic responses like "what would you like to explore next" - be specific!""".trimMargin(),
                conversationHistory = conversationHistory
            )
            response ?: getMockConversationalResponse(userMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating conversational response", e)
            getMockConversationalResponse(userMessage)
        }
    }
    
    /**
     * Suggest an ending for the journal entry
     */
    suspend fun suggestEnding(entryContent: String): String = withContext(Dispatchers.IO) {
        if (settings?.openAIApiKey.isNullOrBlank()) {
            return@withContext getMockEnding()
        }
        
        try {
            val response = callOpenAI(
                "Suggest a thoughtful 1-2 sentence ending for this journal entry: \"$entryContent\"",
                systemPrompt = "You are a wise scribe helping to conclude reflective journal entries."
            )
            response ?: getMockEnding()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating ending", e)
            getMockEnding()
        }
    }
    
    /**
     * Generate daily insight based on the journal entry content
     */
    suspend fun generateDailyInsight(entryContent: String = ""): String = withContext(Dispatchers.IO) {
        if (settings?.openAIApiKey.isNullOrBlank()) {
            return@withContext getMockInsight()
        }
        
        try {
            val prompt = if (entryContent.isNotBlank()) {
                "Based on this journal entry: \"${entryContent.take(500)}\"\n\nGenerate a brief, personalized insight that reflects on what they wrote. Keep it supportive and relevant to their experience (1-2 sentences)."
            } else {
                "Generate a brief, inspirational insight about journaling or self-reflection in RPG/fantasy style (1-2 sentences)."
            }
            
            val response = callOpenAI(
                prompt,
                systemPrompt = "You are a wise, empathetic companion offering thoughtful reflections to journalers. When you see their writing, provide insights that directly relate to their experience."
            )
            response ?: getMockInsight()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating insight", e)
            getMockInsight()
        }
    }
    
    /**
     * Generate a summary of multiple journal entries with pattern analysis
     */
    suspend fun generateJournalSummary(entries: List<String>, timeframe: String = "recent"): String = withContext(Dispatchers.IO) {
        if (settings?.openAIApiKey.isNullOrBlank()) {
            return@withContext getMockSummary()
        }
        
        try {
            val entriesText = entries.take(10).joinToString("\n\n---\n\n") { entry ->
                entry.take(300) // Limit each entry to 300 chars
            }
            
            val prompt = """Analyze these $timeframe journal entries and provide:
                |1. Key emotional patterns or themes (2-3 sentences)
                |2. Notable growth or changes (1-2 sentences)
                |3. One thoughtful reflection or encouragement (1 sentence)
                |
                |Entries:
                |$entriesText
                |
                |Keep the summary supportive, insightful, and around 5-6 sentences total.""".trimMargin()
            
            val response = callOpenAI(
                prompt,
                systemPrompt = "You are a wise, empathetic journaling companion who helps people understand patterns and growth in their reflections. Provide thoughtful, supportive analysis."
            )
            response ?: getMockSummary()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating summary", e)
            getMockSummary()
        }
    }
    
    /**
     * Analyze a specific journal entry for insights
     */
    suspend fun analyzeEntry(entryContent: String): String = withContext(Dispatchers.IO) {
        if (settings?.openAIApiKey.isNullOrBlank()) {
            return@withContext getMockAnalysis()
        }
        
        try {
            val prompt = """Analyze this journal entry and provide a brief, thoughtful reflection:
                |
                |"${entryContent.take(800)}"
                |
                |Focus on:
                |- Key emotions or themes present
                |- Significant moments or realizations
                |- Patterns worth noting
                |
                |Keep your analysis supportive and insightful (3-4 sentences).""".trimMargin()
            
            val response = callOpenAI(
                prompt,
                systemPrompt = "You are a wise, empathetic companion analyzing journal entries. Provide thoughtful, supportive insights that help the person understand their experience better."
            )
            response ?: getMockAnalysis()
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing entry", e)
            getMockAnalysis()
        }
    }
    
    /**
     * Semantic search across journal entries
     */
    suspend fun semanticSearch(query: String, entries: List<Pair<String, String>>): List<Pair<String, Double>> = withContext(Dispatchers.IO) {
        if (settings?.openAIApiKey.isNullOrBlank()) {
            // Fallback to basic keyword search
            return@withContext entries.mapIndexed { index, (id, content) ->
                val relevance = if (content.contains(query, ignoreCase = true)) 1.0 else 0.0
                id to relevance
            }.filter { it.second > 0 }
        }
        
        try {
            // Use OpenAI to find semantically similar entries
            val entriesSummary = entries.take(20).joinToString("\n") { (id, content) ->
                "ID:$id | ${content.take(150)}"
            }
            
            val prompt = """Given this search query: "$query"
                |
                |Find the most relevant journal entries from this list. Return ONLY the IDs of relevant entries, separated by commas, in order of relevance.
                |
                |Entries:
                |$entriesSummary
                |
                |Return format: ID1,ID2,ID3 (no explanations, just IDs)""".trimMargin()
            
            val response = callOpenAI(
                prompt,
                systemPrompt = "You are a semantic search engine for journal entries. Understand the meaning and emotion behind queries, not just keywords."
            )
            
            if (response != null) {
                val ids = response.split(",").map { it.trim() }
                ids.mapIndexed { index, id -> 
                    id to (1.0 - (index * 0.1)) // Decreasing relevance score
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in semantic search", e)
            emptyList()
        }
    }
    
    /**
     * Call OpenAI API
     */
    private suspend fun callOpenAI(prompt: String, systemPrompt: String, conversationHistory: List<String> = emptyList()): String? = withContext(Dispatchers.IO) {
        try {
            val apiKey = settings?.openAIApiKey ?: return@withContext null
            val model = settings.openAIModel
            
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                // Add conversation history for context
                conversationHistory.forEachIndexed { index, message ->
                    put(JSONObject().apply {
                        put("role", if (index % 2 == 0) "user" else "assistant")
                        put("content", message)
                    })
                }
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }
            
            val requestBody = JSONObject().apply {
                put("model", model)
                put("messages", messages)
                put("temperature", 0.9)  // Increased from 0.7 for more variety
                put("max_tokens", 150)
                put("presence_penalty", 0.6)  // Encourage new topics
                put("frequency_penalty", 0.5)  // Reduce repetition
            }
            
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "OpenAI API error: ${response.code}")
                    return@withContext null
                }
                
                val responseBody = response.body?.string() ?: return@withContext null
                val json = JSONObject(responseBody)
                
                json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling OpenAI", e)
            null
        }
    }
    
    // Mock responses for when API key is not configured
    private fun getMockPrompt(): String {
        val prompts = listOf(
            "⚔️ What challenge did you face today, brave adventurer?",
            "📜 Chronicle a moment that made you feel alive today.",
            "🔮 What wisdom did you gain on your journey?",
            "🗡️ Describe a decision you made and its consequences.",
            "🏰 What fortress of habit are you building?"
        )
        return prompts.random()
    }
    
    private fun getMockQuestion(): String {
        val questions = listOf(
            "How did that make you feel, adventurer?",
            "What deeper meaning might lie beneath this event?",
            "If you could advise your past self, what would you say?",
            "What lesson might this experience teach you?",
            "How might this shape your future journey?"
        )
        return questions.random()
    }
    
    private fun getMockEnding(): String {
        val endings = listOf(
            "May your path forward be illuminated by this reflection.",
            "Thus concludes another chapter in your epic tale.",
            "Your journey of self-discovery continues, brave soul.",
            "May this wisdom serve you well on the road ahead.",
            "And so, another page turns in your legend."
        )
        return endings.random()
    }
    
    private fun getMockInsight(): String {
        val insights = listOf(
            "🔮 The greatest adventures begin with a single written word.",
            "📜 Each entry is a step on your path to self-mastery.",
            "⚔️ Reflection is the weapon of the wise adventurer.",
            "🏰 Build your mental fortress, one journal entry at a time.",
            "🗡️ Your thoughts, once recorded, become powerful artifacts."
        )
        return insights.random()
    }
    
    private fun getMockConversationalResponse(userMessage: String): String {
        // Extract keywords to make response feel more personalized
        val lowerMessage = userMessage.lowercase()
        
        return when {
            // Mental health and emotional struggles - HIGH PRIORITY
            lowerMessage.contains("anxiety") || lowerMessage.contains("anxious") -> {
                listOf(
                    "I hear that you've been dealing with anxiety. That can be really overwhelming. What does the anxiety feel like for you?",
                    "Anxiety can be so challenging to navigate. Thank you for sharing that. When do you notice it affecting you most?",
                    "I'm sorry you're experiencing anxiety. That takes courage to acknowledge. What helps you cope when it feels intense?"
                ).random()
            }
            lowerMessage.contains("depression") || lowerMessage.contains("depressed") -> {
                listOf(
                    "I'm hearing that depression has been weighing on you. That's really difficult. What does a hard day with depression look like for you?",
                    "Thank you for trusting me with this. Depression can feel so heavy. What, if anything, has brought you moments of relief?",
                    "I'm sorry you're going through this with depression. You're not alone in this. How long have you been feeling this way?"
                ).random()
            }
            lowerMessage.contains("rough") || lowerMessage.contains("hard") || lowerMessage.contains("difficult") || lowerMessage.contains("tough") -> {
                listOf(
                    "It sounds like things have been really rough for you. I'm here to listen. What's been the hardest part?",
                    "I can hear that it's been difficult. Thank you for opening up about this. What's weighing on you most right now?",
                    "That sounds really tough. I appreciate you sharing this with me. How have you been managing through it?"
                ).random()
            }
            lowerMessage.contains("stress") || lowerMessage.contains("stressed") || lowerMessage.contains("overwhelm") -> {
                listOf(
                    "Stress can be so consuming. I hear you. What's contributing most to feeling overwhelmed right now?",
                    "That level of stress sounds really challenging. What part of it feels most difficult to handle?",
                    "I understand that stressed feeling. Thank you for expressing it. What would help lighten that burden for you?"
                ).random()
            }
            lowerMessage.contains("sad") || lowerMessage.contains("cry") || lowerMessage.contains("crying") || lowerMessage.contains("tears") -> {
                listOf(
                    "I hear the sadness in your words. It's okay to feel this way. What's bringing up these feelings?",
                    "Thank you for sharing these difficult emotions with me. What do you think is at the heart of this sadness?",
                    "Those tears represent real pain. I'm here with you. What's been weighing most heavily on your heart?"
                ).random()
            }
            lowerMessage.contains("lonely") || lowerMessage.contains("alone") || lowerMessage.contains("isolated") -> {
                listOf(
                    "Loneliness can feel so heavy. I'm glad you're here sharing this. What makes you feel most isolated?",
                    "That feeling of being alone is so hard. Thank you for trusting me with this. When do you feel it most?",
                    "I hear that sense of isolation. You're not alone in feeling this way. What kind of connection are you craving?"
                ).random()
            }
            // Positive emotions and growth
            lowerMessage.contains("happy") || lowerMessage.contains("joy") || lowerMessage.contains("excited") || lowerMessage.contains("great") -> {
                listOf(
                    "I can sense that positive energy! What's bringing you this happiness?",
                    "That's wonderful to hear! Tell me more about what's making you feel this way.",
                    "I love hearing about these brighter moments. What made this experience special for you?"
                ).random()
            }
            // General feelings
            lowerMessage.contains("feel") || lowerMessage.contains("felt") || lowerMessage.contains("feeling") -> {
                listOf(
                    "I hear you expressing those feelings. What do you think is at the root of them?",
                    "Those emotions sound significant. Tell me more about what led to feeling this way.",
                    "Thank you for sharing what you're feeling. How are these emotions affecting you right now?"
                ).random()
            }
            // Work and career
            lowerMessage.contains("work") || lowerMessage.contains("job") || lowerMessage.contains("career") || lowerMessage.contains("boss") -> {
                listOf(
                    "Work situations can be so challenging. What's the most frustrating part for you?",
                    "I hear what you're saying about work. How is this affecting you beyond just the job itself?",
                    "That sounds like a complex work situation. What would you most like to change about it?"
                ).random()
            }
            // Relationships
            lowerMessage.contains("friend") || lowerMessage.contains("family") || lowerMessage.contains("relationship") || lowerMessage.contains("partner") -> {
                listOf(
                    "Relationships can be complicated. What happened in this interaction that's affecting you?",
                    "I can hear that this person matters to you. What are you feeling about this situation?",
                    "Thank you for sharing about this relationship. What do you need from this person that you're not getting?"
                ).random()
            }
            // Uncertainty
            lowerMessage.contains("don't know") || lowerMessage.contains("not sure") || lowerMessage.contains("confused") || lowerMessage.contains("uncertain") -> {
                listOf(
                    "Uncertainty can be uncomfortable. What's making this feel unclear for you?",
                    "It's okay not to have all the answers. What's the main question you're wrestling with?",
                    "I hear that confusion. What would clarity look like to you in this situation?"
                ).random()
            }
            // Time references
            lowerMessage.contains("today") || lowerMessage.contains("this morning") || lowerMessage.contains("tonight") || lowerMessage.contains("week") -> {
                listOf(
                    "Tell me more about what happened. What stood out most to you about this experience?",
                    "I'm listening closely. What made this particularly significant for you?",
                    "I hear you reflecting on this time. What emotions are coming up as you think about it?"
                ).random()
            }
            // Default - but still more specific than before
            else -> {
                listOf(
                    "I hear what you're saying. What's the most important part of this for you?",
                    "Thank you for sharing that. What thoughts or feelings are strongest as you reflect on this?",
                    "That sounds meaningful. What aspect of this experience do you want to explore more?",
                    "I'm here listening. What's beneath what you just shared?"
                ).random()
            }
        }
    }
    
    private fun getMockSummary(): String {
        val summaries = listOf(
            "📊 Your recent entries show a journey of self-discovery and growth. You've been thoughtfully exploring your emotions and experiences, which demonstrates real courage and commitment to understanding yourself better.",
            "🌟 Looking at your journal entries, there's a pattern of resilience and reflection. You're actively working through challenges and taking time to process your feelings, which is a powerful practice.",
            "💫 Your entries reveal someone who is deeply engaged with their inner world. The themes of growth, struggle, and hope interweave through your reflections, showing a commitment to personal development."
        )
        return summaries.random()
    }
    
    private fun getMockAnalysis(): String {
        val analyses = listOf(
            "This entry shows thoughtful self-reflection and honesty about your experiences. The emotions you've shared are valid and meaningful, and writing about them is an important step in processing them.",
            "There's a clear thread of introspection in this entry. You're not just describing events but exploring how they affected you, which demonstrates emotional awareness and growth.",
            "Your words reveal both vulnerability and strength. This kind of honest journaling helps create clarity and understanding about your experiences."
        )
        return analyses.random()
    }
    
    companion object {
        private const val TAG = "AIService"
    }
}
