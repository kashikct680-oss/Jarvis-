package com.kishore.jarvis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
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
    var input by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050B14))
            .padding(16.dp)
    ) {

        Text(
            text = "J.A.R.V.I.S",
            color = Color.Cyan,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = state.statusMessage,
            color = Color.LightGray,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        HologramCore(
            isListening = state.isListening,
            isProcessing = state.isProcessing,
            isSpeaking = state.isSpeaking
        )

        ConversationPanel(
            messages = state.conversation,
            modifier = Modifier.weight(1f)
        )

        state.errorMessage?.let { error ->
            Text(
                text = error,
                color = Color(0xFFFF7777),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ask JARVIS anything...") },
            shape = RoundedCornerShape(14.dp),
            singleLine = false
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onMicrophoneClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF087EAA)
                )
            ) {
                Text("MIC")
            }

            Button(
                onClick = {
                    val message = input.trim()
                    if (message.isNotEmpty()) {
                        onSendMessage(message)
                        input = ""
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = input.isNotBlank() && !state.isProcessing
            ) {
                Text("SEND")
            }
        }
    }

    if (state.pendingConfirmation != null) {
        AlertDialog(
            onDismissRequest = onCancelAction,
            title = { Text("Confirm action") },
            text = { Text(state.pendingConfirmation) },
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
