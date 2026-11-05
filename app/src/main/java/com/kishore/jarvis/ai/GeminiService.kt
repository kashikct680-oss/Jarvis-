package com.kishore.jarvis.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiService(
    private val backendUrl: String
) : AiRepository {

    override suspend fun ask(
        userMessage: String,
        conversationHistory: List<ChatMessage>
    ): AiResponse = withContext(Dispatchers.IO) {

        if (userMessage.isBlank()) {
            return@withContext AiResponse(
                success = false,
                text = "",
                errorMessage = "Please enter a message."
            )
        }

        var connection: HttpURLConnection? = null

        try {
            connection = URL(backendUrl)
                .openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.doOutput = true
            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            val messages = JSONArray()

            conversationHistory.forEach { message ->
                messages.put(
                    JSONObject().apply {
                        put("role", message.role)
                        put("content", message.content)
                    }
                )
            }

            messages.put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                }
            )

            val requestBody = JSONObject().apply {
                put("messages", messages)
            }

            connection.outputStream.use { output ->
                output.write(
                    requestBody.toString().toByteArray(Charsets.UTF_8)
                )
            }

            val statusCode = connection.responseCode

            val responseStream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseText = responseStream?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            if (statusCode !in 200..299) {
                return@withContext AiResponse(
                    success = false,
                    text = "",
                    errorMessage = "AI server error: $statusCode"
                )
            }

            val responseJson = JSONObject(responseText)
            val answer = responseJson.optString("text", "")

            if (answer.isBlank()) {
                AiResponse(
                    success = false,
                    text = "",
                    errorMessage = "The AI returned an empty response."
                )
            } else {
                AiResponse(
                    success = true,
                    text = answer
                )
            }

        } catch (exception: Exception) {
            AiResponse(
                success = false,
                text = "",
                errorMessage = exception.message
                    ?: "Unable to connect to the AI service."
            )
        } finally {
            connection?.disconnect()
        }
    }

    override suspend fun isAvailable(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(backendUrl)
                    .openConnection() as HttpURLConnection

                connection.requestMethod = "HEAD"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val available =
                    connection.responseCode in 200..499

                connection.disconnect()
                available

            } catch (exception: Exception) {
                false
            }
        }
}
