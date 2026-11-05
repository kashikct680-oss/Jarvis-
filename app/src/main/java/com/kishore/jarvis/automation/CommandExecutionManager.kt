package com.kishore.jarvis.automation

import android.content.Context
import com.kishore.jarvis.core.CommandRouter
import com.kishore.jarvis.core.CommandType
import com.kishore.jarvis.memory.LocalStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CommandExecutionResult(
    val success: Boolean,
    val response: String,
    val requiresConfirmation: Boolean = false
)

class CommandExecutionManager(context: Context) {

    private val appLauncher = AppLauncher(context)
    private val phoneCallManager = PhoneCallManager(context)
    private val localStorage = LocalStorage(context)
    private val commandRouter = CommandRouter()

    fun execute(input: String): CommandExecutionResult {

        if (input.isBlank()) {
            return CommandExecutionResult(
                false,
                "Please tell me what you want me to do."
            )
        }

        val command = commandRouter.process(input)

        return when (command.type) {

            CommandType.GREETING -> {
                CommandExecutionResult(
                    true,
                    "Hello! JARVIS is ready. How can I help you?"
                )
            }

            CommandType.GET_TIME -> {
                val time = SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())

                CommandExecutionResult(
                    true,
                    "The current time is $time."
                )
            }

            CommandType.GET_DATE -> {
                val date = SimpleDateFormat(
                    "EEEE, dd MMMM yyyy",
                    Locale.getDefault()
                ).format(Date())

                CommandExecutionResult(
                    true,
                    "Today is $date."
                )
            }

            CommandType.OPEN_APP -> {
                val appName = command.argument.orEmpty()

                val result = appLauncher.launchApp(appName)

                CommandExecutionResult(
                    result.success,
                    result.message
                )
            }

            CommandType.SAVE_NOTE -> {
                val note = command.argument.orEmpty()

                if (note.isBlank()) {
                    CommandExecutionResult(
                        false,
                        "Please tell me what note to save."
                    )
                } else {
                    val key = "note_${System.currentTimeMillis()}"

                    localStorage.saveNote(key, note)

                    CommandExecutionResult(
                        true,
                        "I have saved your note."
                    )
                }
            }

            CommandType.CALL_CONTACT -> {
                val contactName = command.argument.orEmpty()

                if (contactName.isBlank()) {
                    CommandExecutionResult(
                        false,
                        "Please tell me the contact name."
                    )
                } else {
                    val result = phoneCallManager.callContact(contactName)

                    CommandExecutionResult(
                        result.success,
                        result.message,
                        requiresConfirmation = result.success
                    )
                }
            }

            CommandType.UNKNOWN,
            CommandType.SET_REMINDER,
            CommandType.SEND_MESSAGE,
            CommandType.CALL_LAST_NUMBER,
            CommandType.ANSWER_CALL,
            CommandType.REJECT_CALL,
            CommandType.END_CALL,
            CommandType.APP_TASK -> {
                CommandExecutionResult(
                    false,
                    "This command needs additional integration. I can assist you once the relevant module is connected."
                )
            }
        }
    }
}
