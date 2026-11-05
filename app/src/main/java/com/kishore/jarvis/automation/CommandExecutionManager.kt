
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

    private val reminderHandler = ReminderCommandHandler(
        ReminderManager(context)
    )

    fun execute(input: String): CommandExecutionResult {

        if (input.isBlank()) {
            return CommandExecutionResult(
                success = false,
                response = "Please tell me what you want me to do."
            )
        }

        val command = commandRouter.process(input)

        return when (command.type) {

            CommandType.GREETING -> {
                CommandExecutionResult(
                    success = true,
                    response = "Hello! JARVIS is ready. How can I help you?"
                )
            }

            CommandType.GET_TIME -> {
                val time = SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())

                CommandExecutionResult(
                    success = true,
                    response = "The current time is $time."
                )
            }

            CommandType.GET_DATE -> {
                val date = SimpleDateFormat(
                    "EEEE, dd MMMM yyyy",
                    Locale.getDefault()
                ).format(Date())

                CommandExecutionResult(
                    success = true,
                    response = "Today is $date."
                )
            }

            CommandType.OPEN_APP -> {
                val result = appLauncher.launchApp(
                    command.argument.orEmpty()
                )

                CommandExecutionResult(
                    success = result.success,
                    response = result.message
                )
            }

            CommandType.SAVE_NOTE -> {
                val note = command.argument.orEmpty()

                if (note.isBlank()) {
                    CommandExecutionResult(
                        success = false,
                        response = "Please tell me what note to save."
                    )
                } else {
                    localStorage.saveNote(
                        "note_${System.currentTimeMillis()}",
                        note
                    )

                    CommandExecutionResult(
                        success = true,
                        response = "Your note has been saved."
                    )
                }
            }

            CommandType.CALL_CONTACT -> {
                val result = phoneCallManager.callContact(
                    command.argument.orEmpty()
                )

                CommandExecutionResult(
                    success = result.success,
                    response = result.message,
                    requiresConfirmation = result.success
                )
            }

            CommandType.SET_REMINDER -> {
                reminderHandler.createReminder(input)
            }

            else -> {
                CommandExecutionResult(
                    success = false,
                    response = "This command needs additional integration."
                )
            }
        }
    }
}
