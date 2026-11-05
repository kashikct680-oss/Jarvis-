package com.kishore.jarvis.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
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
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF00D9FF),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = state.statusMessage,
                color = Color.LightGray,
                modifier = Modifier.padding(
                    top = 6.dp,
                    bottom = 16.dp
                )
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF101D30)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = Color(0xFF073B55)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "J",
                                color = Color(0xFF00D9FF),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = when {
                            state.isListening -> "Listening..."
                            state.isProcessing -> "Processing..."
                            state.isSpeaking -> "Speaking..."
                            else -> "System Ready"
                        },
                        color = Color(0xFF00D9FF),
                        modifier = Modifier.padding(top = 12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = "CONVERSATION",
                color = Color(0xFF00D9FF),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF101725)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (state.conversation.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Hello! I am JARVIS.",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Type or speak a command to begin.",
                            color = Color.LightGray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
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
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (message.isUser) {
                                            Color(0xFF075B78)
                                        } else {
                                            Color(0xFF26364B)
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = message.message,
                                        color = Color.White,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Type your command...",
                        color = Color.Gray
                    )
                },
                enabled = !state.isProcessing,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onMicrophoneClick,
                    modifier = Modifier.size(
                        width = 120.dp,
                        height = 48.dp
                    )
                ) {
                    Text("MIC")
                }

                Button(
                    onClick = {
                        val message = inputText.trim()
                        if (message.isNotEmpty()) {
                            onSendMessage(message)
                            inputText = ""
                        }
                    },
                    modifier = Modifier.size(
                        width = 120.dp,
                        height = 48.dp
                    ),
                    enabled = inputText.isNotBlank() &&
                        !state.isProcessing
                ) {
                    Text("SEND")
                }
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.errorMessage,
                    color = Color.Red
                )
            }
        }
    }

    if (state.pendingConfirmation != null) {
        AlertDialog(
            onDismissRequest = onCancelAction,
            title = {
                Text("Confirm Action")
            },
            text = {
                Text(state.pendingConfirmation)
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
