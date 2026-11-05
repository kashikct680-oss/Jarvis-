package com.kishore.jarvis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kishore.jarvis.core.ConversationMessage

@Composable
fun ConversationPanel(
    messages: List<ConversationMessage>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(messages) { message ->

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (message.isUser) {
                            Color(0xFF123B58)
                        } else {
                            Color(0xFF17212B)
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = if (message.isUser) "YOU" else "JARVIS",
                    color = if (message.isUser) {
                        Color.Cyan
                    } else {
                        Color(0xFF00FF88)
                    },
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = message.message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
