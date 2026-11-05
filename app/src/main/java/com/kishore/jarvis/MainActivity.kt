
package com.kishore.jarvis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JarvisScreen()
        }
    }
}

@Composable
fun JarvisScreen() {
    var message by remember { mutableStateOf("") }
    var reply by remember {
        mutableStateOf("Hello! I am JARVIS. How can I help you?")
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "J.A.R.V.I.S",
                    fontSize = 34.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "YOUR PERSONAL AI ASSISTANT",
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = reply,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(30.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Talk to JARVIS") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            reply = "You said: $message"
                            message = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SEND MESSAGE")
                }
            }
        }
    }
}
