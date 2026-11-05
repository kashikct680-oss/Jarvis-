package com.kishore.jarvis.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kishore.jarvis.ai.AiRepository
import com.kishore.jarvis.ai.ChatMessage
import com.kishore.jarvis.automation.ActionConfirmationManager
import com.kishore.jarvis.automation.PendingAction
import com.kishore.jarvis.automation.SensitiveActionType
import com.kishore.jarvis.memory.LocalStorage
import kotlinx.coroutines.launch

class JarvisViewModel(
    private val aiRepository: AiRepository,
    private val localStorage: LocalStorage
) : ViewModel() {

    var state by mutableStateOf(JarvisState())
        private set

    private val confirmationManager = ActionConfirmationManager()

    private val conversationHistory = mutableListOf<ChatMessage>()

    fun processText(input: String) {
        if (input.isBlank() || state.isProcessing) return

        val userMessage = ConversationMessage(
            message = input,
            isUser = true
        )

        state = state.copy(
            userInput = input,
            isProcessing = true,
            errorMessage = null,
            conversation = state.conversation + userMessage,
            statusMessage = "JARVIS is thinking..."
        )

        viewModelScope.launch {
            try {
                val response = aiRepository.ask(
                    userMessage = input,
                    conversationHistory = conversationHistory.toList()
                )

                if (response.success) {
                    conversationHistory.add(
                        ChatMessage("user", input)
                    )

                    conversationHistory.add(
                        ChatMessage("assistant", response.text)
                    )

                    state = state.copy(
                        response = response.text,
                        isProcessing = false,
                        statusMessage = "JARVIS is ready",
                        conversation = state.conversation + ConversationMessage(
                            message = response.text,
                            isUser = false
                        )
                    )
                } else {
                    state = state.copy(
                        isProcessing = false,
                        statusMessage = "AI unavailable",
                        errorMessage = response.errorMessage
                            ?: "Unable to get a response."
                    )
                }

            } catch (exception: Exception) {
                state = state.copy(
                    isProcessing = false,
                    statusMessage = "Something went wrong",
                    errorMessage = exception.message
                        ?: "Unexpected error."
                )
            }
        }
    }

    fun setListening(value: Boolean) {
        state = state.copy(
            isListening = value,
            statusMessage = if (value) {
                "Listening..."
            } else {
                "JARVIS is ready"
            }
        )
    }

    fun setSpeaking(value: Boolean) {
        state = state.copy(
            isSpeaking = value,
            statusMessage = if (value) {
                "JARVIS is speaking..."
            } else {
                "JARVIS is ready"
            }
        )
    }

    fun saveNote(key: String, value: String) {
        localStorage.saveNote(key, value)

        state = state.copy(
            response = "Your note has been saved.",
            statusMessage = "Note saved"
        )
    }

    fun getSavedNote(key: String): String? {
        return localStorage.getNote(key)
    }

    fun requestActionConfirmation(
        type: SensitiveActionType,
        title: String,
        description: String
    ): PendingAction {

        val action = confirmationManager.requestConfirmation(
            type,
            title,
            description
        )

        state = state.copy(
            pendingConfirmation = description,
            statusMessage = "Waiting for your confirmation"
        )

        return action
    }

    fun confirmPendingAction(actionId: String): PendingAction? {
        val action = confirmationManager.confirm(actionId)

        state = state.copy(
            pendingConfirmation = null,
            statusMessage = if (action != null) {
                "Action confirmed"
            } else {
                "Confirmation expired or invalid"
            }
        )

        return action
    }

    fun cancelPendingAction(actionId: String) {
        confirmationManager.cancel(actionId)

        state = state.copy(
            pendingConfirmation = null,
            statusMessage = "Action cancelled"
        )
    }

    fun clearConversation() {
        conversationHistory.clear()
        confirmationManager.clear()

        state = JarvisState(
            statusMessage = "Conversation cleared"
        )
    }
}
