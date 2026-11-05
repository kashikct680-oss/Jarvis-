package com.kishore.jarvis.automation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

data class MessageResult(
    val success: Boolean,
    val message: String
)

class MessagingManager(
    private val context: Context
) {

    // Opens an SMS draft with the recipient and message.
    fun prepareSms(
        phoneNumber: String,
        message: String
    ): MessageResult {

        if (phoneNumber.isBlank() || message.isBlank()) {
            return MessageResult(
                false,
                "Please provide a phone number and message."
            )
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${Uri.encode(phoneNumber)}")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return openIntent(
            intent,
            "SMS draft is ready. Review it before sending."
        )
    }

    // Opens WhatsApp with a prepared message.
    fun prepareWhatsApp(
        phoneNumber: String,
        message: String
    ): MessageResult {

        if (phoneNumber.isBlank() || message.isBlank()) {
            return MessageResult(
                false,
                "Please provide a phone number and message."
            )
        }

        val cleanNumber = phoneNumber.filter {
            it.isDigit() || it == '+'
        }

        val uri = Uri.parse(
            "https://wa.me/${cleanNumber.removePrefix("+")}" +
                "?text=${Uri.encode(message)}"
        )

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return openIntent(
            intent,
            "WhatsApp message is prepared. Review and press Send."
        )
    }

    // Opens a supported app's share interface.
    // The user selects the recipient and confirms sending.
    fun prepareShareMessage(
        message: String
    ): MessageResult {

        if (message.isBlank()) {
            return MessageResult(
                false,
                "Please provide a message."
            )
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            val chooser = Intent.createChooser(
                intent,
                "Choose messaging app"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)

            MessageResult(
                true,
                "Choose your messaging app and review the message."
            )

        } catch (exception: Exception) {
            MessageResult(
                false,
                "Unable to open the sharing interface."
            )
        }
    }

    private fun openIntent(
        intent: Intent,
        successMessage: String
    ): MessageResult {

        return try {
            context.startActivity(intent)

            MessageResult(
                true,
                successMessage
            )

        } catch (exception: ActivityNotFoundException) {
            MessageResult(
                false,
                "The required messaging app is not available."
            )

        } catch (exception: Exception) {
            MessageResult(
                false,
                "Unable to open the messaging app."
            )
        }
    }
}
