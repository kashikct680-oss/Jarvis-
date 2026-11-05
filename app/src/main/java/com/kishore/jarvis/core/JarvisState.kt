package com.kishore.jarvis.core

data class JarvisState(
    val userInput: String = "",
    val response: String = "",
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val isSpeaking: Boolean = false,
    val statusMessage: String = "JARVIS is ready",
    val conversation: List<ConversationMessage> = emptyList(),
    val pendingConfirmation: String? = null,
    val errorMessage: String? = null
)

data class ConversationMessage(
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
