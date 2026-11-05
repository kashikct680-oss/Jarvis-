package com.kishore.jarvis.core

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kishore.jarvis.ai.AiRepository
import com.kishore.jarvis.ai.GeminiService
import com.kishore.jarvis.automation.CommandExecutionManager
import com.kishore.jarvis.memory.LocalStorage

class JarvisViewModelFactory(
    private val context: Context,
    private val backendUrl: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(JarvisViewModel::class.java)) {

            val aiRepository: AiRepository =
                GeminiService(backendUrl)

            val localStorage = LocalStorage(
                context.applicationContext
            )

            val commandExecutor = CommandExecutionManager(
                context.applicationContext
            )

            return JarvisViewModel(
                aiRepository,
                localStorage,
                commandExecutor
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
