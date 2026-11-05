package com.kishore.jarvis.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CommandType {
    GREETING,
    GET_TIME,
    GET_DATE,
    OPEN_APP,
    APP_TASK,
    SET_REMINDER,
    SAVE_NOTE,
    SEND_MESSAGE,
    UNKNOWN
}

enum class MessagingApp {
    WHATSAPP,
    TELEGRAM,
    INSTAGRAM,
    FACEBOOK_MESSENGER,
    GOOGLE_MESSAGES,
    SIGNAL,
    DISCORD,
    OTHER,
    NOT_SPECIFIED
}

data class JarvisCommand(
    val type: CommandType,
    val originalText: String,
    val argument: String = "",
    val messagingApp: MessagingApp = MessagingApp.NOT_SPECIFIED,
    val appName: String = "",
    val taskDescription: String = ""
)

class CommandRouter {

    fun process(input: String): JarvisCommand {
        val text = input.trim()
        val command = text.lowercase(Locale.ROOT)

        if (text.isBlank()) {
            return JarvisCommand(CommandType.UNKNOWN, text)
        }

        return when {
            containsAny(
                command,
                "hello", "hi jarvis", "hey jarvis",
                "नमस्ते", "नमस्कार", "हेलो"
            ) -> JarvisCommand(
                CommandType.GREETING, text
            )

            containsAny(
                command,
                "what time", "current time", "time",
                "समय", "कितने बजे"
            ) -> JarvisCommand(
                CommandType.GET_TIME, text
            )

            containsAny(
                command,
                "today's date", "what date", "date",
                "तारीख", "आज की डेट"
            ) -> JarvisCommand(
                CommandType.GET_DATE, text
            )

            isMessagingCommand(command) -> JarvisCommand(
                type = CommandType.SEND_MESSAGE,
                originalText = text,
                argument = text,
                messagingApp = detectMessagingApp(command),
                taskDescription = text
            )

            containsAny(
                command,
                "reminder", "remind me",
                "याद दिलाना", "रिमाइंडर"
            ) -> JarvisCommand(
                CommandType.SET_REMINDER,
                text,
                text,
                taskDescription = text
            )

            containsAny(
                command,
                "remember", "save note", "take a note",
                "याद रखना", "नोट बनाओ", "नोट सेव"
            ) -> JarvisCommand(
                CommandType.SAVE_NOTE,
                text,
                text,
                taskDescription = text
            )

            isAppTask(command) -> JarvisCommand(
                type = CommandType.APP_TASK,
                originalText = text,
                argument = text,
                appName = extractAppHint(text),
                taskDescription = text
            )

            containsAny(
                command,
                "open", "launch", "खोलो",
                "खोल दो", "चालू करो"
            ) -> JarvisCommand(
                type = CommandType.OPEN_APP,
                originalText = text,
                argument = text,
                appName = extractAppHint(text)
            )

            else -> JarvisCommand(
                CommandType.UNKNOWN, text
            )
        }
    }

    private fun isMessagingCommand(command: String): Boolean {
        return containsAny(
            command,
            "send message", "send a message",
            "message to", "text message",
            "message bhejo", "message bhej",
            "msg bhejo", "message karo",
            "मैसेज भेजो", "मैसेज भेज दो",
            "संदेश भेजो"
        )
    }

    private fun isAppTask(command: String): Boolean {
        val mentionsAppAction = containsAny(
            command,
            "open", "launch", "खोलो", "खोल दो"
        )

        val mentionsTask = containsAny(
            command,
            "find", "search", "book", "booking",
            "order", "buy", "compare", "help me",
            "restaurant", "hotel", "flight", "train",
            "ticket", "food", "navigate",
            "ढूंढो", "खोजो", "बुक करो",
            "ऑर्डर करो", "खाना मंगाओ"
        )

        return mentionsAppAction && mentionsTask
    }

    private fun detectMessagingApp(command: String): MessagingApp {
        return when {
            containsAny(command, "whatsapp", "व्हाट्सएप") ->
                MessagingApp.WHATSAPP

            containsAny(command, "telegram", "टेलीग्राम") ->
                MessagingApp.TELEGRAM

            containsAny(command, "instagram", "इंस्टाग्राम") ->
                MessagingApp.INSTAGRAM

            containsAny(command, "messenger", "मैसेंजर") ->
                MessagingApp.FACEBOOK_MESSENGER

            containsAny(command, "google messages", "sms", "एसएमएस") ->
                MessagingApp.GOOGLE_MESSAGES

            containsAny(command, "signal", "सिग्नल") ->
                MessagingApp.SIGNAL

            containsAny(command, "discord", "डिस्कॉर्ड") ->
                MessagingApp.DISCORD

            else -> MessagingApp.NOT_SPECIFIED
        }
    }

    private fun extractAppHint(text: String): String {
        val patterns = listOf(
            Regex("""(?i)\b(?:open|launch)\s+(.+?)(?:\s+and\s+|\s+to\s+|$)"""),
            Regex("""(?i)(?:खोलो|खोल दो)\s+(.+)""")
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }

        return ""
    }

    private fun containsAny(
        text: String,
        vararg keywords: String
    ): Boolean {
        return keywords.any { text.contains(it) }
    }

    fun getTime(): String {
        return SimpleDateFormat(
            "hh:mm a", Locale.getDefault()
        ).format(Date())
    }

    fun getDate(): String {
        return SimpleDateFormat(
            "dd MMMM yyyy", Locale.getDefault()
        ).format(Date())
    }
}
