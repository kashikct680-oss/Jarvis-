package com.kishore.jarvis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kishore.jarvis.core.JarvisState

@Composable
fun JarvisScreen(
    state: JarvisState,
    onSendMessage: (String) -> Unit,
    onMicrophoneClick: () -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF080D18)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "J.A.R.V.I.S",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF00D9FF)
            )

            Text(
                text = state.statusMessage,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
            )

            HologramCore(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp, max = 190.dp)
            )

            Text(
                text = "CONVERSATION",
                color = Color(0xFF00D9FF),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Color(0xFF111A2B),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.conversation) { message ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (message.isUser) {
                            Alignment.End
                        } else {
                            Alignment.Start
                        }
                    ) {
                        Text(
                            text = if (message.isUser) "YOU" else "JARVIS",
                            color = if (message.isUser) {
                                Color(0xFF70C7FF)
                            } else {
                                Color(0xFF00E5B0)
                            }
                        )

                        Text(
                            text = message.message,
                            color = Color.White,
                            modifier = Modifier
                                .background(
                                    Color(0xFF202D43),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp)
                        )
                    }
                }

                if (state.isProcessing) {
                    item {
                        Text(
                            text = "JARVIS is thinking...",
                            color = Color(0xFF00D9FF)
                        )
                    }
                }
            }

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color(0xFFFF7777),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Ask JARVIS...", color = Color.Gray)
                    },
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank()
                ) {
                    Text("SEND")
                }
            }

            Button(
                onClick = onMicrophoneClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    if (state.isListening) "STOP LISTENING"
                    else "🎙 SPEAK TO JARVIS"
                )
            }
        }
    }

    state.pendingConfirmation?.let { description ->
        AlertDialog(
            onDismissRequest = onCancelAction,
            title = {
                Text("Confirm action")
            },
            text = {
                Text(description)
            },
            confirmButton = {
                TextButton(onClick = onConfirmAction) {
                    Text("CONFIRM")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelAction) {
                    Text("CANCEL")
                }
            }
        )
    }
}
