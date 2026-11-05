package com.kishore.jarvis.ai

interface AiRepository {

    suspend fun ask(
        userMessage: String,
        conversationHistory: List<ChatMessage> = emptyList()
    ): AiResponse

    suspend fun isAvailable(): Boolean
}

data class ChatMessage(
    val role: String,
    val content: String
)

data class AiResponse(
    val success: Boolean,
    val text: String,
    val errorMessage: String? = null
)
