package com.kishore.jarvis.memory

import android.content.Context

class LocalStorage(context: Context) {

    private val preferences = context.getSharedPreferences(
        "jarvis_persistent_storage",
        Context.MODE_PRIVATE
    )

    fun saveNote(key: String, value: String) {
        if (key.isBlank()) return

        preferences.edit()
            .putString("note_$key", value)
            .apply()
    }

    fun getNote(key: String): String? {
        return preferences.getString("note_$key", null)
    }

    fun getAllNotes(): Map<String, String> {
        return preferences.all
            .filterKeys { it.startsWith("note_") }
            .mapKeys { it.key.removePrefix("note_") }
            .mapValues { it.value as? String ?: "" }
    }

    fun savePreference(key: String, value: String) {
        if (key.isBlank()) return

        preferences.edit()
            .putString("preference_$key", value)
            .apply()
    }

    fun getPreference(key: String): String? {
        return preferences.getString("preference_$key", null)
    }

    fun deleteNote(key: String) {
        preferences.edit()
            .remove("note_$key")
            .apply()
    }

    fun clearAll() {
        preferences.edit().clear().apply()
    }
}
