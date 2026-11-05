package com.kishore.jarvis

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.kishore.jarvis.core.JarvisViewModel
import com.kishore.jarvis.core.JarvisViewModelFactory
import com.kishore.jarvis.ui.JarvisScreen
import com.kishore.jarvis.voice.SpeechInputManager
import com.kishore.jarvis.voice.SpeechOutputManager

class MainActivity : ComponentActivity() {

    private lateinit var jarvisViewModel: JarvisViewModel
    private lateinit var speechOutput: SpeechOutputManager
    private var speechInput: SpeechInputManager? = null

    private val backendUrl =
        "https://YOUR-PRIVATE-BACKEND-URL"

    private var pendingVoiceLanguage = "en-IN"

    private val microphonePermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startVoiceRecognition(pendingVoiceLanguage)
            } else {
                jarvisViewModel.setListening(false)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        jarvisViewModel = ViewModelProvider(
            this,
            JarvisViewModelFactory(
                applicationContext,
                backendUrl
            )
        )[JarvisViewModel::class.java]

        speechOutput = SpeechOutputManager(this)

        speechInput = SpeechInputManager(
            context = this,
            onResult = { spokenText ->
                runOnUiThread {
                    jarvisViewModel.setListening(false)
                    jarvisViewModel.processText(spokenText)
                }
            },
            onError = { error ->
                runOnUiThread {
                    jarvisViewModel.setListening(false)
                }
            }
        )

        setContent {
            MaterialTheme {

                val currentState = jarvisViewModel.state

                LaunchedEffect(currentState.response) {
                    if (currentState.response.isNotBlank()) {
                        speechOutput.speak(
                            currentState.response,
                            "en"
                        )
                    }
                }

                JarvisScreen(
                    state = currentState,

                    onSendMessage = { message ->
                        jarvisViewModel.processText(message)
                    },

                    onMicrophoneClick = {
                        requestMicrophonePermission("en-IN")
                    },

                    onConfirmAction = {
                        // Actual action execution will be connected
                        // after the task execution module is integrated.
                    },

                    onCancelAction = {
                        jarvisViewModel.clearConversation()
                    }
                )
            }
        }
    }

    private fun requestMicrophonePermission(language: String) {
        pendingVoiceLanguage = language

        val permissionGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            startVoiceRecognition(language)
        } else {
            microphonePermission.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    private fun startVoiceRecognition(language: String) {
        if (speechInput?.isRecognitionAvailable() == true) {
            jarvisViewModel.setListening(true)
            speechInput?.startListening(language)
        } else {
            jarvisViewModel.setListening(false)
        }
    }

    override fun onDestroy() {
        speechInput?.destroy()
        speechOutput.shutdown()
        super.onDestroy()
    }
}
