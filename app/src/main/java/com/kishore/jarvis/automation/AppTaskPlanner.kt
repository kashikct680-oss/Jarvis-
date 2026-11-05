package com.kishore.jarvis.automation

enum class AppTaskType {
    SEARCH,
    BOOKING,
    ORDERING,
    SHOPPING,
    NAVIGATION,
    MESSAGING,
    INFORMATION,
    GENERAL
}

data class AppTask(
    val appName: String,
    val taskType: AppTaskType,
    val userRequest: String,
    val requiresConfirmation: Boolean,
    val needsMoreInformation: Boolean,
    val statusMessage: String
)

class AppTaskPlanner {

    fun createPlan(
        appName: String,
        userRequest: String
    ): AppTask {

        val request = userRequest.trim().lowercase()

        val taskType = when {
            containsAny(
                request,
                "book", "booking", "reserve",
                "ticket", "hotel", "flight",
                "बुक", "बुकिंग", "टिकट"
            ) -> AppTaskType.BOOKING

            containsAny(
                request,
                "order", "food", "restaurant",
                "delivery", "ऑर्डर", "खाना"
            ) -> AppTaskType.ORDERING

            containsAny(
                request,
                "buy", "purchase", "shopping",
                "price", "compare", "खरीद"
            ) -> AppTaskType.SHOPPING

            containsAny(
                request,
                "navigate", "directions",
                "route", "location", "maps",
                "रास्ता", "लोकेशन"
            ) -> AppTaskType.NAVIGATION

            containsAny(
                request,
                "message", "send text",
                "मैसेज", "संदेश"
            ) -> AppTaskType.MESSAGING

            containsAny(
                request,
                "search", "find", "look for",
                "ढूंढो", "खोजो"
            ) -> AppTaskType.SEARCH

            containsAny(
                request,
                "tell me", "show me", "information",
                "जानकारी बताओ"
            ) -> AppTaskType.INFORMATION

            else -> AppTaskType.GENERAL
        }

        val needsConfirmation = when (taskType) {
            AppTaskType.BOOKING,
            AppTaskType.ORDERING,
            AppTaskType.SHOPPING,
            AppTaskType.MESSAGING -> true

            else -> false
        }

        val needsMoreInformation =
            request.isBlank() || appName.isBlank()

        val message = when {
            needsMoreInformation ->
                "Please tell me the app and what you want to do."

            needsConfirmation ->
                "I will prepare this task. Your confirmation will be required before the final action."

            else ->
                "I have understood your request and will prepare the next steps."
        }

        return AppTask(
            appName = appName.trim(),
            taskType = taskType,
            userRequest = userRequest.trim(),
            requiresConfirmation = needsConfirmation,
            needsMoreInformation = needsMoreInformation,
            statusMessage = message
        )
    }

    private fun containsAny(
        text: String,
        vararg keywords: String
    ): Boolean {
        return keywords.any { text.contains(it) }
    }
}
