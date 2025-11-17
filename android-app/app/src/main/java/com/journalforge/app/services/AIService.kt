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
    suspend fun generateProbingQuestion(entryContent: String): String = withContext(Dispatchers.IO) {
        if (settings?.openAIApiKey.isNullOrBlank()) {
            return@withContext getMockQuestion()
        }
        
        try {
            val response = callOpenAI(
                "Based on this journal entry, ask a thoughtful probing question to help the writer explore their thoughts deeper: \"$entryContent\"",
                systemPrompt = "You are a wise companion helping adventurers reflect on their journey. Ask insightful questions."
            )
            response ?: getMockQuestion()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating question", e)
            getMockQuestion()
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
    private suspend fun callOpenAI(prompt: String, systemPrompt: String): String? = withContext(Dispatchers.IO) {
        try {
            val apiKey = settings?.openAIApiKey ?: return@withContext null
            val model = settings.openAIModel
            
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }
            
            val requestBody = JSONObject().apply {
                put("model", model)
                put("messages", messages)
                put("temperature", 0.7)
                put("max_tokens", 150)
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
    
    companion object {
        private const val TAG = "AIService"
    }
}
