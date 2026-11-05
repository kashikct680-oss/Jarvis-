
package com.kishore.jarvis

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val BackgroundColor = Color(0xFF050B18)
private val Cyan = Color(0xFF00E5FF)
private val Blue = Color(0xFF397BFF)
private val MutedText = Color(0xFF8CA9C8)

class MainActivity : ComponentActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.rgb(5, 11, 24)
        window.navigationBarColor = android.graphics.Color.rgb(5, 11, 24)

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.US)

                ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

                if (ttsReady) {
                    textToSpeech.setSpeechRate(0.95f)
                    textToSpeech.setPitch(0.9f)
                    speak("Hello. JARVIS is online. How may I assist you?")
                }
            }
        }

        setContent {
            JarvisApp(
                speakOut = { text -> speak(text) }
            )
        }
    }

    private fun speak(text: String) {
        if (ttsReady) {
            textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JARVIS_SPEECH"
            )
        }
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}

@Composable
fun JarvisApp(speakOut: (String) -> Unit) {

    var message by remember { mutableStateOf("") }

    var messages by remember {
        mutableStateOf(
            listOf("JARVIS ONLINE. Voice system initialized.")
        )
    }

    fun sendMessage() {
        if (message.isNotBlank()) {
            val userMessage = message.trim()

            messages = messages + "YOU: $userMessage"

            val response =
                "I heard you say $userMessage. " +
                "I am currently running in demonstration mode. " +
                "My artificial intelligence connection will be added in the next stage."

            speakOut(response)

            message = ""
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
                    .padding(horizontal = 20.dp, vertical = 12.dp)
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
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "ADVANCED PERSONAL ASSISTANT",
                            color = MutedText,
                            fontSize = 9.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    Surface(
                        color = Color(0xFF0C2630),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 7.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00FF9D))
                            )

                            Spacer(Modifier.width(7.dp))

                            Text(
                                "VOICE ACTIVE",
                                color = Color(0xFF00FF9D),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                HorizontalDivider(color = Color(0xFF18334A))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.9f),
                    contentAlignment = Alignment.Center
                ) {
                    HologramCore()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(y = 125.dp)
                    ) {
                        Text(
                            "SYSTEM READY",
                            color = Cyan,
                            fontSize = 12.sp,
                            letterSpacing = 3.sp
                        )

                        Text(
                            "VOICE RESPONSE ENABLED",
                            color = MutedText,
                            fontSize = 9.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Text(
                    "CONVERSATION",
                    color = Cyan,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f)
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
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF102239))
                        .padding(horizontal = 14.dp, vertical = 5.dp),
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
                            onSend = { sendMessage() }
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (message.isEmpty()) {
                                    Text(
                                        "Talk to JARVIS...",
                                        color = MutedText,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Button(
                        onClick = { sendMessage() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(
                            horizontal = 17.dp,
                            vertical = 9.dp
                        )
                    ) {
                        Text("SEND", fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "VOICE  •  MEMORY  •  AUTOMATION  •  AI",
                    modifier = Modifier.fillMaxWidth(),
                    color = MutedText,
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun HologramCore() {

    val transition = rememberInfiniteTransition(label = "hologram")

    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                9000,
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )

    Canvas(
        modifier = Modifier.size(250.dp)
    ) {
        val center = this.center
        val base = size.minDimension * 0.30f
        val radius = base * pulse

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x5540CFFF),
                    Color(0x22397BFF),
                    Color.Transparent
                ),
                center = center,
                radius = base * 2.2f
            ),
            radius = base * 2.2f,
            center = center
        )

        drawCircle(
            color = Cyan.copy(alpha = 0.35f),
            radius = base * 1.65f,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        drawArc(
            color = Cyan,
            startAngle = rotation,
            sweepAngle = 95f,
            useCenter = false,
            topLeft = Offset(
                center.x - base * 1.85f,
                center.y - base * 1.85f
            ),
            size = Size(
                base * 3.7f,
                base * 3.7f
            ),
            style = Stroke(width = 3.dp.toPx())
        )

        drawArc(
            color = Blue,
            startAngle = -rotation,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(
                center.x - base * 1.4f,
                center.y - base * 1.4f
            ),
            size = Size(
                base * 2.8f,
                base * 2.8f
            ),
            style = Stroke(width = 2.dp.toPx())
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    Cyan,
                    Color(0xFF1768D8),
                    Color(0xFF07152E)
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.65f),
            radius = radius * 0.13f,
            center = Offset(
                center.x - radius * 0.25f,
                center.y - radius * 0.25f
            )
        )

        for (i in 0 until 8) {
            val angle = Math.toRadians(
                (rotation + i * 45f).toDouble()
            )

            val start = Offset(
                center.x +
                    kotlin.math.cos(angle).toFloat() * base * 1.15f,
                center.y +
                    kotlin.math.sin(angle).toFloat() * base * 1.15f
            )

            val end = Offset(
                center.x +
                    kotlin.math.cos(angle).toFloat() * base * 1.48f,
                center.y +
                    kotlin.math.sin(angle).toFloat() * base * 1.48f
            )

            drawLine(
                color = Cyan.copy(alpha = 0.8f),
                start = start,
                end = end,
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
