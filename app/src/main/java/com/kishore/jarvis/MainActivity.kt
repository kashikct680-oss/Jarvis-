package com.kishore.jarvis

import android.Manifest
import android.content.pm.PackageManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.app.role.RoleManager
import android.telecom.TelecomManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.kishore.jarvis.core.JarvisViewModel
import com.kishore.jarvis.core.JarvisViewModelFactory
import com.kishore.jarvis.ui.JarvisScreen
import com.kishore.jarvis.voice.SpeechInputManager
import com.kishore.jarvis.voice.SpeechOutputManager

class MainActivity : ComponentActivity() {

    private lateinit var jarvisViewModel: JarvisViewModel
    private lateinit var speechOutput: SpeechOutputManager
    private var speechInput: SpeechInputManager? = null

    // Cloudflare Worker backend connected to Gemini
    private val backendUrl =
        "https://jarvis.kashikct680.workers.dev"

    private var pendingVoiceLanguage = "en-IN"
    private var pendingWakeWordStart = false
    private var wakeReceiverRegistered = false
    private var pendingVoiceApproval = false

    private val wakeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                com.kishore.jarvis.voice.WakeWordService.ACTION_COMMAND ->
                    intent.getStringExtra(
                        com.kishore.jarvis.voice.WakeWordService.EXTRA_COMMAND
                    )?.takeIf { it.isNotBlank() }?.let {
                        jarvisViewModel.processText(it)
                    }

                com.kishore.jarvis.voice.WakeWordService.ACTION_WAKE ->
                    speechOutput.speak("Yes, how can I help?", "en")

                com.kishore.jarvis.automation.JarvisAccessibilityService.ACTION_READY_TO_CONFIRM ->
                    beginVoiceMessageApproval()
            }
        }
    }

    private val microphonePermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                if (pendingWakeWordStart) {
                    pendingWakeWordStart = false
                    startWakeWordService()
                } else {
                    startVoiceRecognition(pendingVoiceLanguage)
                }
            } else {
                pendingWakeWordStart = false
                jarvisViewModel.setListening(false)
            }
        }

    private val contactsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Toast.makeText(
            this,
            if (granted) "Contacts permission granted"
            else "Contacts permission denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    private val notificationsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Toast.makeText(
            this,
            if (granted) "Notifications enabled"
            else "Notifications denied; reminders may be limited",
            Toast.LENGTH_LONG
        ).show()
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

        val wakeFilter = IntentFilter().apply {
            addAction(
                com.kishore.jarvis.voice.WakeWordService.ACTION_COMMAND
            )
            addAction(
                com.kishore.jarvis.voice.WakeWordService.ACTION_WAKE
            )
            addAction(
                com.kishore.jarvis.automation.JarvisAccessibilityService.ACTION_READY_TO_CONFIRM
            )
        }

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(
                wakeReceiver,
                wakeFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(wakeReceiver, wakeFilter)
        }

        wakeReceiverRegistered = true

        speechInput = SpeechInputManager(
            context = this,
            onResult = { spokenText ->
                runOnUiThread {
                    handleRecognizedSpeech(spokenText)
                }
            },
            onError = {
                runOnUiThread {
                    jarvisViewModel.setListening(false)

                    if (pendingVoiceApproval) {
                        pendingVoiceApproval = false

                        com.kishore.jarvis.automation.JarvisAccessibilityService.cancelPendingMessage()

                        speechOutput.speak(
                            "I could not verify your voice approval. The message has been cancelled.",
                            "en"
                        )
                    }
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

                    onWakeWordToggle = {
                        requestWakeWordPermission()
                    },

                    onWakeWordStop = {
                        val stopIntent = Intent(
                            this@MainActivity,
                            com.kishore.jarvis.voice.WakeWordService::class.java
                        ).apply {
                            action =
                                com.kishore.jarvis.voice.WakeWordService.ACTION_STOP
                        }

                        startService(stopIntent)
                    },

                    onAccessibilitySettings = {
                        startActivity(
                            Intent(
                                android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
                            )
                        )
                    },

                    onRequestDefaultDialer = {
                        requestDefaultDialerRole()
                    },

                    onRequestContactsPermission = {
                        contactsPermission.launch(
                            Manifest.permission.READ_CONTACTS
                        )
                    },

                    onRequestNotificationsPermission = {
                        if (Build.VERSION.SDK_INT >= 33) {
                            notificationsPermission.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        } else {
                            startActivity(
                                Intent(
                                    android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                ).putExtra(
                                    android.provider.Settings.EXTRA_APP_PACKAGE,
                                    packageName
                                )
                            )
                        }
                    },

                    onConfirmAction = {
                        // Existing confirmation integration remains unchanged.
                    },

                    onCancelAction = {
                        jarvisViewModel.clearConversation()
                    }
                )
            }
        }
    }

    private fun beginVoiceMessageApproval() {
        val prompt =
            com.kishore.jarvis.automation.JarvisAccessibilityService.pendingVoicePrompt()

        if (prompt == null) {
            com.kishore.jarvis.automation.JarvisAccessibilityService.cancelPendingMessage()
            return
        }

        speechOutput.speak(prompt, "en") {
            runOnUiThread {
                pendingVoiceApproval = true
                startVoiceRecognition("en-IN")
            }
        }
    }

    private fun handleRecognizedSpeech(spokenText: String) {
        jarvisViewModel.setListening(false)

        if (!pendingVoiceApproval) {
            jarvisViewModel.processText(spokenText)
            return
        }

        pendingVoiceApproval = false

        val phrase = spokenText.lowercase()
            .replace(Regex("[^a-z ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        when (phrase) {
            "jarvis confirm send",
            "confirm send",
            "jarvis confirm",
            "confirm message" -> {
                jarvisViewModel.processText("confirm send")
            }

            "jarvis cancel",
            "cancel send",
            "cancel message",
            "no cancel" -> {
                jarvisViewModel.processText("cancel send")
            }

            else -> {
                com.kishore.jarvis.automation.JarvisAccessibilityService.cancelPendingMessage()

                speechOutput.speak(
                    "I did not hear the exact approval phrase. The message has been cancelled.",
                    "en"
                )
            }
        }
    }

    private fun requestDefaultDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)

            if (
                roleManager != null &&
                roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
            ) {
                startActivity(
                    roleManager.createRequestRoleIntent(
                        RoleManager.ROLE_DIALER
                    )
                )
            }
        } else {
            val intent = Intent(
                TelecomManager.ACTION_CHANGE_DEFAULT_DIALER
            ).putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                packageName
            )

            startActivity(intent)
        }
    }

    private fun requestWakeWordPermission() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            startWakeWordService()
        } else {
            pendingWakeWordStart = true
            microphonePermission.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    private fun startWakeWordService() {
        val serviceIntent = Intent(
            this,
            com.kishore.jarvis.voice.WakeWordService::class.java
        ).apply {
            action =
                com.kishore.jarvis.voice.WakeWordService.ACTION_START
        }

        ContextCompat.startForegroundService(
            this,
            serviceIntent
        )
    }

    private fun requestMicrophonePermission(language: String) {
        pendingVoiceLanguage = language

        val permissionGranted = ContextCompat.checkSelfPermission(
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
        val manager = speechInput

        if (manager != null && manager.isRecognitionAvailable()) {
            jarvisViewModel.setListening(true)
            manager.startListening(language)
        } else {
            jarvisViewModel.setListening(false)
        }
    }

    override fun onDestroy() {
        speechInput?.destroy()
        speechInput = null

        if (wakeReceiverRegistered) {
            unregisterReceiver(wakeReceiver)
            wakeReceiverRegistered = false
        }

        if (::speechOutput.isInitialized) {
            speechOutput.shutdown()
        }

        super.onDestroy()
    }
}
