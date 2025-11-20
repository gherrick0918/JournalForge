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
                append("User's message: \"$userMessage\"\n\n")
                append("Respond naturally to what the user just said. Reference specific details from their message. ")
                append("Ask a follow-up question that directly relates to what they mentioned. ")
                append("Be conversational, empathetic, and show that you're actively listening.")
            }
            
            val response = callOpenAI(
                prompt,
                systemPrompt = """You are a wise, empathetic companion on an adventurer's journaling quest. 
                    |Your role is to have a genuine conversation, not just ask generic questions.
                    |- Always respond directly to what the user says
                    |- Reference specific details they mention
                    |- Show empathy and understanding
                    |- Ask follow-up questions that flow naturally from the conversation
                    |- Vary your responses - sometimes reflect, sometimes probe deeper, sometimes validate their feelings
                    |- Keep responses conversational and natural (2-3 sentences max)
                    |- Use the RPG/fantasy theme lightly - don't overdo it""".trimMargin(),
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
     * Generate daily insight
     */
    suspend fun generateDailyInsight(): String = withContext(Dispatchers.IO) {
        if (settings?.openAIApiKey.isNullOrBlank()) {
            return@withContext getMockInsight()
        }
        
        try {
            val response = callOpenAI(
                "Generate a brief, inspirational insight about journaling or self-reflection in RPG/fantasy style (1-2 sentences).",
                systemPrompt = "You are a wise sage offering wisdom to adventurers."
            )
            response ?: getMockInsight()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating insight", e)
            getMockInsight()
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
            lowerMessage.contains("feel") || lowerMessage.contains("felt") -> {
                listOf(
                    "I hear you expressing those feelings. What do you think triggered them?",
                    "Those emotions sound significant. Can you tell me more about what led to that?",
                    "Thank you for sharing that with me. How are you processing these feelings now?"
                ).random()
            }
            lowerMessage.contains("work") || lowerMessage.contains("job") -> {
                listOf(
                    "Work can certainly be challenging. What aspect of this situation matters most to you?",
                    "That sounds like quite a day at work. How did you handle it?",
                    "I'm curious - what would your ideal outcome have been in that situation?"
                ).random()
            }
            lowerMessage.contains("friend") || lowerMessage.contains("family") || lowerMessage.contains("relationship") -> {
                listOf(
                    "Relationships shape so much of our journey. What was going through your mind during that interaction?",
                    "It sounds like this person is important to you. How did that make you feel?",
                    "Thank you for opening up about that. What do you think you learned from this experience?"
                ).random()
            }
            lowerMessage.contains("today") || lowerMessage.contains("this morning") || lowerMessage.contains("tonight") -> {
                listOf(
                    "Let's explore that moment more deeply. What stood out to you about it?",
                    "That's an interesting start to your reflection. What else happened that affected you?",
                    "I'm listening - tell me more about what made that significant."
                ).random()
            }
            lowerMessage.contains("don't know") || lowerMessage.contains("not sure") || lowerMessage.contains("confused") -> {
                listOf(
                    "Uncertainty is part of growth. What's the first thing that comes to mind when you think about it?",
                    "It's okay not to have all the answers. What does your gut tell you?",
                    "Sometimes we know more than we think. What possibilities are you considering?"
                ).random()
            }
            else -> {
                listOf(
                    "That's a meaningful reflection. What else comes to mind as you think about it?",
                    "I appreciate you sharing that. How does exploring this make you feel?",
                    "Tell me more - what aspect of this feels most important to you right now?",
                    "Interesting perspective. What led you to that realization?",
                    "I'm curious to hear more. What would you like to explore next?"
                ).random()
            }
        }
    }
    
    companion object {
        private const val TAG = "AIService"
    }
}
