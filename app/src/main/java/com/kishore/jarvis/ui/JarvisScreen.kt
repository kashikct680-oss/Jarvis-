package com.kishore.jarvis.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
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
    var message by rememberSaveable {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "J.A.R.V.I.S",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = state.statusMessage,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        HologramCore()

        Spacer(modifier = Modifier.height(12.dp))

        if (state.isProcessing) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
        }

        ConversationPanel(
            messages = state.conversation,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Ask JARVIS anything...") },
            placeholder = { Text("Type your command") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (message.isNotBlank()) {
                        onSendMessage(message.trim())
                        message = ""
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onMicrophoneClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("MIC")
            }

            Button(
                onClick = {
                    if (message.isNotBlank()) {
                        onSendMessage(message.trim())
                        message = ""
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = message.isNotBlank() && !state.isProcessing
            ) {
                Text("SEND")
            }
        }

        state.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    state.pendingConfirmation?.let { confirmation ->
        AlertDialog(
            onDismissRequest = onCancelAction,
            title = {
                Text("Confirm action")
            },
            text = {
                Text(confirmation)
            },
            confirmButton = {
                TextButton(onClick = onConfirmAction) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelAction) {
                    Text("Cancel")
                }
            }
        )
    }
}
