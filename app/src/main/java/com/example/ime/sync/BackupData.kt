package com.example.ime.sync

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

/**
 * Cấu trúc dữ liệu sao lưu đồng bộ lên Google Drive (appDataFolder).
 */
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "Anykey26-1.0",
    val settingsMap: Map<String, Any?> = emptyMap(),
    val learnedWords: List<String> = emptyList()
) {
    fun toJson(): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(this)
    }

    companion object {
        fun fromJson(json: String): BackupData? {
            return try {
                val gson = Gson()
                val type = object : TypeToken<BackupData>() {}.type
                gson.fromJson<BackupData>(json, type)
            } catch (e: Exception) {
                null
            }
        }
    }
}
