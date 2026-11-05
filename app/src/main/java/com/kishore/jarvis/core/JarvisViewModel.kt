package com.kishore.jarvis.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kishore.jarvis.ai.AiRepository
import com.kishore.jarvis.ai.ChatMessage
import com.kishore.jarvis.automation.ActionConfirmationManager
import com.kishore.jarvis.automation.SensitiveActionType
import com.kishore.jarvis.memory.LocalStorage
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JarvisViewModel(
    private val aiRepository: AiRepository,
    private val localStorage: LocalStorage
) : ViewModel() {

    var state = JarvisState()
        private set

    private val confirmationManager = ActionConfirmationManager()

    private val conversationHistory = mutableListOf<ChatMessage>()

    private fun updateState(newState: JarvisState) {
        state = newState
        onStateChanged?.invoke(state)
    }

    var onStateChanged: ((JarvisState) -> Unit)? = null

    fun processText(input: String) {
        if (input.isBlank()) return

        val userMessage = ConversationMessage(
            message = input,
            isUser = true
        )

        updateState(
            state.copy(
                userInput = input,
                isProcessing = true,
                errorMessage = null,
                conversation = state.conversation + userMessage,
                statusMessage = "JARVIS is thinking..."
            )
        )

        viewModelScope.launch {
            try {
                val response = aiRepository.ask(
                    userMessage = input,
                    conversationHistory = conversationHistory.toList()
                )

                if (response.success) {
                    conversationHistory.add(ChatMessage("user", input))
                    conversationHistory.add(ChatMessage("assistant", response.text))

                    val assistantMessage = ConversationMessage(
                        message = response.text,
                        isUser = false
                    )

                    updateState(
                        state.copy(
                            response = response.text,
                            isProcessing = false,
                            statusMessage = "JARVIS is ready",
                            conversation = state.conversation + assistantMessage
                        )
                    )
                } else {
                    updateState(
                        state.copy(
                            isProcessing = false,
                            statusMessage = "AI unavailable",
                            errorMessage = response.errorMessage
                                ?: "Unable to get a response."
                        )
                    )
                }
            } catch (exception: Exception) {
                updateState(
                    state.copy(
                        isProcessing = false,
                        statusMessage = "Something went wrong",
                        errorMessage = exception.message
                            ?: "An unexpected error occurred."
                    )
                )
            }
        }
    }

    fun setListening(listening: Boolean) {
        updateState(
            state.copy(
                isListening = listening,
                statusMessage = if (listening) {
                    "Listening..."
                } else {
                    "JARVIS is ready"
                }
            )
        )
    }

    fun setSpeaking(speaking: Boolean) {
        updateState(
            state.copy(
                isSpeaking = speaking,
                statusMessage = if (speaking) {
                    "JARVIS is speaking..."
                } else {
                    "JARVIS is ready"
                }
            )
        )
    }

    fun saveNote(key: String, value: String) {
        localStorage.saveNote(key, value)
        updateState(
            state.copy(
                response = "Your note has been saved.",
                statusMessage = "Note saved"
            )
        )
    }

    fun getSavedNote(key: String): String? {
        return localStorage.getNote(key)
    }

    fun requestActionConfirmation(
        type: SensitiveActionType,
        title: String,
        description: String
    ): String {
        val action = confirmationManager.requestConfirmation(
            type,
            title,
            description
        )

        updateState(
            state.copy(
                pendingConfirmation = action.description,
                statusMessage = "Waiting for your confirmation"
            )
        )

        return action.id
    }

    fun cancelPendingAction(actionId: String) {
        confirmationManager.cancel(actionId)

        updateState(
            state.copy(
                pendingConfirmation = null,
                statusMessage = "Action cancelled"
            )
        )
    }

    fun clearConversation() {
        conversationHistory.clear()

        updateState(
            state.copy(
                conversation = emptyList(),
                response = "",
                userInput = "",
                pendingConfirmation = null,
                statusMessage = "Conversation cleared"
            )
        )

        confirmationManager.clear()
    }
}
