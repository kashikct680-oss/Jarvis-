package com.kishore.jarvis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val BackgroundColor = Color(0xFF050B18)
private val Cyan = Color(0xFF00E5FF)
private val Blue = Color(0xFF397BFF)
private val MutedText = Color(0xFF8CA9C8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.rgb(5, 11, 24)
        window.navigationBarColor = android.graphics.Color.rgb(5, 11, 24)

        setContent {
            JarvisApp()
        }
    }
}

@Composable
fun JarvisApp() {
    val context = androidx.compose.ui.platform.LocalContext.current

    var message by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("SYSTEM READY") }
    var isListening by remember { mutableStateOf(false) }
    var ttsReady by remember { mutableStateOf(false) }

    var messages by remember {
        mutableStateOf(
            listOf(
                "JARVIS: Systems initialized.",
                "JARVIS: Hello! I am ready. How may I assist you?"
            )
        )
    }

    val tts = remember {
        TextToSpeech(context) { result ->
            if (result == TextToSpeech.SUCCESS) {
                ttsReady = true
            }
        }
    }

    DisposableEffect(tts) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speak(text: String) {
        if (ttsReady) {
            tts.language = Locale("en", "IN")
            tts.setSpeechRate(1.0f)
            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "jarvis_reply"
            )
        }
    }

    fun getReply(input: String): String {
        val command = input.lowercase(Locale.ROOT)

        return when {
            command.contains("hello") ||
            command.contains("hi jarvis") ->
                "Hello! I am listening. What can I do for you?"

            command.contains("your name") ->
                "My name is JARVIS, your personal assistant."

            command.contains("how are you") ->
                "I am functioning properly. Thank you for asking."

            command.contains("time") ->
                "The current time is ${
                    java.text.SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(java.util.Date())
                }."

            command.contains("date") ->
                "Today's date is ${
                    java.text.SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.getDefault()
                    ).format(java.util.Date())
                }."

            command.contains("thank") ->
                "You are welcome. I am always ready to help."

            else ->
                "I heard you say: $input. My advanced AI connection will be added in a later stage."
        }
    }

    fun sendMessage(input: String) {
        if (input.isBlank()) return

        val userMessage = input.trim()
        val reply = getReply(userMessage)

        messages = messages +
            "YOU: $userMessage" +
            "JARVIS: $reply"

        message = ""
        status = "RESPONSE READY"
        speak(reply)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                status = "MICROPHONE READY"
            } else {
                status = "MICROPHONE PERMISSION DENIED"
            }
        }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            if (
                android.os.Build.VERSION.SDK_INT >= 31 &&
                SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
            ) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }
        } else {
            null
        }
    }

    DisposableEffect(speechRecognizer) {
        if (speechRecognizer != null) {
            speechRecognizer.setRecognitionListener(
                object : RecognitionListener {

                    override fun onReadyForSpeech(params: Bundle?) {
                        status = "LISTENING..."
                    }

                    override fun onBeginningOfSpeech() {
                        status = "HEARING YOUR VOICE"
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                        status = "PROCESSING..."
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        status = "VOICE INPUT ERROR. TRY AGAIN."
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false

                        val spokenText =
                            results?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )?.firstOrNull()

                        if (!spokenText.isNullOrBlank()) {
                            message = spokenText
                            sendMessage(spokenText)
                        } else {
                            status = "NO SPEECH DETECTED"
                        }
                    }

                    override fun onPartialResults(
                        partialResults: Bundle?
                    ) {}

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {}
                }
            )
        }

        onDispose {
            speechRecognizer?.destroy()
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            status = "SPEECH RECOGNITION NOT AVAILABLE"
            return
        }

        if (
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        try {
            val intent = Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            ).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "en-IN"
                )
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                    "en-IN"
                )
                putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
                )
            }

            tts.stop()
            isListening = true
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            isListening = false
            status = "COULD NOT START MICROPHONE"
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BackgroundColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "J.A.R.V.I.S",
                            color = Cyan,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "PERSONAL AI ASSISTANT",
                            color = MutedText,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    Text(
                        "● ACTIVE",
                        color = Color(0xFF00FF9D),
                        fontSize = 11.sp
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFF18334A)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(145.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF102D4B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "J",
                                color = Cyan,
                                fontSize = 76.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            status,
                            color = Cyan,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp
                        )

                        Text(
                            "VOICE + TEXT INTERFACE",
                            color = MutedText,
                            fontSize = 10.sp
                        )
                    }
                }

                Text(
                    "CONVERSATION",
                    color = Cyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0A1424))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { item ->
                        Text(
                            text = item,
                            color = if (item.startsWith("YOU:"))
                                Cyan
                            else
                                Color(0xFFD4E7F8),
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF102239))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { sendMessage(message) }
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (message.isEmpty()) {
                                    Text(
                                        "Type your message...",
                                        color = MutedText
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Button(
                        onClick = { sendMessage(message) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue
                        )
                    ) {
                        Text("SEND")
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { startListening() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF087D9A)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            if (isListening) "LISTENING..." else "🎙 SPEAK"
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            tts.stop()
                            status = "SPEECH STOPPED"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("STOP VOICE")
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "VOICE • AI • MEMORY • AUTOMATION",
                    modifier = Modifier.fillMaxWidth(),
                    color = MutedText,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
