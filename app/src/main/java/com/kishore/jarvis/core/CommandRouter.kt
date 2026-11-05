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
    CALL_CONTACT,
    CALL_LAST_NUMBER,
    ANSWER_CALL,
    REJECT_CALL,
    END_CALL,
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

            // Incoming call controls
            containsAny(
                command,
                "answer call", "pick up call", "receive call",
                "answer the phone", "pick up the phone",
                "call uthao", "phone uthao", "कॉल उठाओ",
                "फोन उठाओ", "कॉल रिसीव करो"
            ) -> JarvisCommand(
                CommandType.ANSWER_CALL, text
            )

            containsAny(
                command,
                "reject call", "decline call", "ignore call",
                "reject the call", "cut the call",
                "call kaat do", "कॉल काट दो",
                "कॉल रिजेक्ट करो", "फोन मत उठाना"
            ) -> JarvisCommand(
                CommandType.REJECT_CALL, text
            )

            containsAny(
                command,
                "end call", "hang up", "disconnect call",
                "phone rakh do", "कॉल बंद करो",
                "फोन काट दो"
            ) -> JarvisCommand(
                CommandType.END_CALL, text
            )

            // Outgoing calls
            isLastNumberCall(command) -> JarvisCommand(
                CommandType.CALL_LAST_NUMBER,
                text
            )

            isCallCommand(command) -> JarvisCommand(
                type = CommandType.CALL_CONTACT,
                originalText = text,
                argument = extractCallTarget(text),
                taskDescription = text
            )

            // Messaging
            isMessagingCommand(command) -> JarvisCommand(
                type = CommandType.SEND_MESSAGE,
                originalText = text,
                argument = text,
                messagingApp = detectMessagingApp(command),
                taskDescription = text
            )

            // Greetings
            containsAny(
                command,
                "hello", "hi jarvis", "hey jarvis",
                "good morning", "good evening",
                "नमस्ते", "नमस्कार", "हेलो"
            ) -> JarvisCommand(
                CommandType.GREETING, text
            )

            // Time
            containsAny(
                command,
                "what time is it", "current time",
                "tell me the time", "time please",
                "समय बताओ", "अभी कितने बजे",
                "कितने बजे हैं"
            ) -> JarvisCommand(
                CommandType.GET_TIME, text
            )

            // Date
            containsAny(
                command,
                "today's date", "what is the date",
                "what date is it", "tell me the date",
                "आज की तारीख", "आज की डेट बताओ",
                "तारीख बताओ"
            ) -> JarvisCommand(
                CommandType.GET_DATE, text
            )

            // Reminders
            containsAny(
                command,
                "remind me", "set reminder",
                "create reminder", "reminder",
                "याद दिलाना", "रिमाइंडर लगाओ",
                "मुझे याद दिलाना"
            ) -> JarvisCommand(
                CommandType.SET_REMINDER,
                text,
                text,
                taskDescription = text
            )

            // Notes and memory
            containsAny(
                command,
                "save note", "take a note",
                "make a note", "remember this",
                "याद रखना", "नोट बनाओ",
                "नोट सेव करो"
            ) -> JarvisCommand(
                CommandType.SAVE_NOTE,
                text,
                text,
                taskDescription = text
            )

            // App-related tasks
            isAppTask(command) -> JarvisCommand(
                type = CommandType.APP_TASK,
                originalText = text,
                argument = text,
                appName = extractAppHint(text),
                taskDescription = text
            )

            // Open an app
            containsAny(
                command,
                "open ", "launch ", "start app ",
                "खोलो", "खोल दो", "चालू करो"
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

    private fun isLastNumberCall(command: String): Boolean {
        return containsAny(
            command,
            "call last number",
            "call last dialled number",
            "call last dialed number",
            "redial",
            "last number par call",
            "आखिरी नंबर पर कॉल करो",
            "लास्ट नंबर पर कॉल लगाओ"
        )
    }

    private fun isCallCommand(command: String): Boolean {
        return containsAny(
            command,
            "call ",
            "phone ",
            "dial ",
            "फोन लगाओ",
            "कॉल करो",
            "कॉल लगाओ",
            "को फोन करो",
            "को कॉल करो"
        )
    }

    private fun extractCallTarget(text: String): String {
        val patterns = listOf(
            Regex(
                """(?i)\b(?:call|phone|dial)\s+(.+)$"""
            ),
            Regex(
                """(?i)(.+?)\s+को\s+(?:कॉल|फोन)\s+(?:करो|लगाओ)"""
            ),
            Regex(
                """(?:फोन|कॉल)\s+(?:लगाओ|करो)\s+(.+)"""
            )
        )

        for (pattern in patterns) {
            val match = pattern.find(text.trim())
            if (match != null) {
                return match.groupValues[1].trim()
                    .removeSuffix(".")
            }
        }

        return ""
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

    private fun detectMessagingApp(
        command: String
    ): MessagingApp {
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
            Regex(
                """(?i)\b(?:open|launch)\s+(.+?)(?:\s+and\s+|\s+to\s+|$)"""
            ),
            Regex(
                """(?i)(?:खोलो|खोल दो)\s+(.+)"""
            )
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
