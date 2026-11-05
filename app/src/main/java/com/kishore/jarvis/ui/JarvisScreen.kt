package com.kishore.jarvis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kishore.jarvis.core.JarvisState

@Composable
fun JarvisScreen(
    state: JarvisState,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onMicrophoneClick: () -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF050B16)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "J.A.R.V.I.S",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = state.statusMessage,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                contentAlignment = Alignment.Center
            ) {
                HologramCore(
                    modifier = Modifier.size(180.dp)
                )
            }

            Text(
                text = "CONVERSATION",
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Color(0xFF101B2A),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = message.message,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = state.userInput,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Ask JARVIS anything...")
                },
                enabled = !state.isProcessing,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMicrophoneClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("MIC")
                }

                Button(
                    onClick = onSendMessage,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isProcessing
                ) {
                    Text(
                        if (state.isProcessing) "WAIT..." else "SEND"
                    )
                }
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
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
                Text("Confirm action")
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
