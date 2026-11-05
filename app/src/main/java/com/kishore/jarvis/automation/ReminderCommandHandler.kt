package com.kishore.jarvis.automation

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ReminderCommandHandler(
    private val reminderManager: ReminderManager
) {

    fun createReminder(input: String): CommandExecutionResult {
        val text = input.trim().lowercase(Locale.ROOT)
        val now = ZonedDateTime.now()

        val triggerTime = parseTriggerTime(text, now)
            ?: return CommandExecutionResult(
                false,
                "Please specify a time, such as 6 PM or in 30 minutes."
            )

        if (!triggerTime.isAfter(now)) {
            return CommandExecutionResult(
                false,
                "That time has already passed. Please choose a future time."
            )
        }

        val title = extractTitle(text)

        if (title.isBlank()) {
            return CommandExecutionResult(
                false,
                "Please tell me what I should remind you about."
            )
        }

        return try {
            val result = reminderManager.scheduleReminder(
                title,
                triggerTime.toInstant().toEpochMilli()
            )

            result.fold(
                onSuccess = {
                    val formattedTime = triggerTime.format(
                        DateTimeFormatter.ofPattern(
                            "dd MMM, hh:mm a",
                            Locale.getDefault()
                        )
                    )

                    CommandExecutionResult(
                        true,
                        "Reminder set: $title, at $formattedTime."
                    )
                },
                onFailure = {
                    CommandExecutionResult(
                        false,
                        it.message ?: "Unable to schedule the reminder."
                    )
                }
            )
        } catch (exception: Exception) {
            CommandExecutionResult(
                false,
                exception.message ?: "Unable to create reminder."
            )
        }
    }

    private fun parseTriggerTime(
        text: String,
        now: ZonedDateTime
    ): ZonedDateTime? {

        val relative = Regex(
            """in\s+(\d+)\s+(minutes?|mins?|hours?|hrs?)"""
        ).find(text)

        if (relative != null) {
            val amount = relative.groupValues[1].toLongOrNull() ?: return null
            val unit = relative.groupValues[2]

            return when {
                unit.startsWith("min") -> now.plusMinutes(amount)
                else -> now.plusHours(amount)
            }
        }

        val timeMatch = Regex(
            """(?:at\s+)?(\d{1,2})(?::(\d{2}))?\s*(am|pm)"""
        ).find(text) ?: return null

        val hour = timeMatch.groupValues[1].toIntOrNull() ?: return null
        val minute = timeMatch.groupValues[2].toIntOrNull() ?: 0
        val meridiem = timeMatch.groupValues[3]

        if (hour !in 1..12 || minute !in 0..59) return null

        val hour24 = when {
            meridiem == "am" && hour == 12 -> 0
            meridiem == "pm" && hour != 12 -> hour + 12
            else -> hour
        }

        val time = LocalTime.of(hour24, minute)

        val date = when {
            text.contains("tomorrow") -> now.toLocalDate().plusDays(1)
            text.contains("day after tomorrow") ->
                now.toLocalDate().plusDays(2)
            else -> now.toLocalDate()
        }

        var result = ZonedDateTime.of(
            date,
            time,
            now.zone
        )

        if (!text.contains("tomorrow") &&
            !text.contains("day after tomorrow") &&
            !result.isAfter(now)
        ) {
            result = result.plusDays(1)
        }

        return result
    }

    private fun extractTitle(text: String): String {
        return text
            .replace(
                Regex("""remind me to|remind me|set a reminder to|set reminder for|set a reminder for|reminder to"""),
                ""
            )
            .replace(Regex("""tomorrow|today|day after tomorrow"""), "")
            .replace(
                Regex("""at\s+\d{1,2}(?::\d{2})?\s*(am|pm)"""),
                ""
            )
            .replace(
                Regex("""in\s+\d+\s+(minutes?|mins?|hours?|hrs?)"""),
                ""
            )
            .trim(' ', '.', ',')
    }
}
