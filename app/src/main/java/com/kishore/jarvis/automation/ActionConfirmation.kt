package com.kishore.jarvis.automation

enum class SensitiveActionType {
    SEND_MESSAGE,
    MAKE_CALL,
    BOOK_TICKET,
    BOOK_HOTEL,
    PLACE_ORDER,
    MAKE_PAYMENT,
    CANCEL_BOOKING,
    OTHER
}

data class PendingAction(
    val id: String,
    val type: SensitiveActionType,
    val title: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

class ActionConfirmationManager {

    private var pendingAction: PendingAction? = null

    fun requestConfirmation(
        type: SensitiveActionType,
        title: String,
        description: String
    ): PendingAction {
        val action = PendingAction(
            id = java.util.UUID.randomUUID().toString(),
            type = type,
            title = title,
            description = description
        )

        pendingAction = action
        return action
    }

    fun getPendingAction(): PendingAction? {
        return pendingAction
    }

    fun confirm(actionId: String): PendingAction? {
        val action = pendingAction ?: return null

        if (action.id != actionId) return null

        pendingAction = null
        return action
    }

    fun cancel(actionId: String): Boolean {
        if (pendingAction?.id != actionId) return false

        pendingAction = null
        return true
    }

    fun clear() {
        pendingAction = null
    }
}
