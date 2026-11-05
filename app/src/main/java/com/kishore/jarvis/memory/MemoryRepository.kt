package com.kishore.jarvis.memory

import android.content.Context

class MemoryRepository(context: Context) {

    private val preferences = context.getSharedPreferences(
        "jarvis_local_memory",
        Context.MODE_PRIVATE
    )

    private val prefix = "memory_"

    // Save information locally.
    fun saveMemory(key: String, value: String): Boolean {

        if (key.isBlank() || value.isBlank()) {
            return false
        }

        return try {
            preferences.edit()
                .putString(prefix + key.trim(), value.trim())
                .commit()
        } catch (exception: Exception) {
            false
        }
    }

    // Retrieve saved information.
    fun getMemory(key: String): String? {
        if (key.isBlank()) return null

        return preferences.getString(
            prefix + key.trim(),
            null
        )
    }

    // Retrieve all saved information.
    fun getAllMemories(): Map<String, String> {

        return preferences.all
            .filterKeys { it.startsWith(prefix) }
            .mapNotNull { (key, value) ->
                val memoryValue = value as? String
                    ?: return@mapNotNull null

                key.removePrefix(prefix) to memoryValue
            }
            .toMap()
    }

    // Delete one saved memory.
    fun forgetMemory(key: String): Boolean {

        if (key.isBlank()) return false

        val storageKey = prefix + key.trim()

        if (!preferences.contains(storageKey)) {
            return false
        }

        return preferences.edit()
            .remove(storageKey)
            .commit()
    }

    // Delete all JARVIS memories.
    fun clearAllMemories(): Boolean {
        return preferences.edit()
            .clear()
            .commit()
    }
}
