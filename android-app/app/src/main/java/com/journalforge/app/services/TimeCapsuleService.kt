package com.journalforge.app.services

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.journalforge.app.models.TimeCapsule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

/**
 * Service for managing time capsules with local JSON file storage
 */
class TimeCapsuleService(private val context: Context) {
    
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()
    
    private val capsulesFile: File
        get() = File(context.filesDir, "time_capsules.json")
    
    /**
     * Get all time capsules
     */
    suspend fun getAllCapsules(): List<TimeCapsule> = withContext(Dispatchers.IO) {
        try {
            if (!capsulesFile.exists()) {
                return@withContext emptyList()
            }
            
            val json = capsulesFile.readText()
            val type = object : TypeToken<List<TimeCapsule>>() {}.type
            gson.fromJson<List<TimeCapsule>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading capsules", e)
            emptyList()
        }
    }
    
    /**
     * Get sealed capsules (not yet ready to unseal)
     */
    suspend fun getSealedCapsules(): List<TimeCapsule> = withContext(Dispatchers.IO) {
        getAllCapsules().filter { it.isSealed && !it.canUnseal() }
    }
    
    /**
     * Get capsules ready to unseal
     */
    suspend fun getReadyToUnsealCapsules(): List<TimeCapsule> = withContext(Dispatchers.IO) {
        getAllCapsules().filter { it.canUnseal() }
    }
    
    /**
     * Get unsealed capsules
     */
    suspend fun getUnsealedCapsules(): List<TimeCapsule> = withContext(Dispatchers.IO) {
        getAllCapsules().filter { !it.isSealed }
    }
    
    /**
     * Get a single capsule by ID
     */
    suspend fun getCapsule(id: String): TimeCapsule? = withContext(Dispatchers.IO) {
        getAllCapsules().find { it.id == id }
    }
    
    /**
     * Save a new capsule or update an existing one
     */
    suspend fun saveCapsule(capsule: TimeCapsule): Boolean = withContext(Dispatchers.IO) {
        try {
            val capsules = getAllCapsules().toMutableList()
            
            // Remove old version if exists and add new
            capsules.removeIf { it.id == capsule.id }
            capsules.add(capsule)
            
            // Sort by unseal date
            capsules.sortBy { it.unsealDate }
            
            // Save to file
            val json = gson.toJson(capsules)
            capsulesFile.writeText(json)
            
            Log.d(TAG, "Capsule saved successfully: ${capsule.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving capsule", e)
            false
        }
    }
    
    /**
     * Unseal a capsule
     */
    suspend fun unsealCapsule(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val capsule = getCapsule(id) ?: return@withContext false
            
            if (!capsule.canUnseal()) {
                Log.w(TAG, "Capsule $id cannot be unsealed yet")
                return@withContext false
            }
            
            capsule.isSealed = false
            saveCapsule(capsule)
        } catch (e: Exception) {
            Log.e(TAG, "Error unsealing capsule", e)
            false
        }
    }
    
    /**
     * Delete a capsule
     */
    suspend fun deleteCapsule(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val capsules = getAllCapsules().toMutableList()
            val removed = capsules.removeIf { it.id == id }
            
            if (removed) {
                val json = gson.toJson(capsules)
                capsulesFile.writeText(json)
                Log.d(TAG, "Capsule deleted: $id")
            }
            
            removed
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting capsule", e)
            false
        }
    }
    
    companion object {
        private const val TAG = "TimeCapsuleService"
    }
}
