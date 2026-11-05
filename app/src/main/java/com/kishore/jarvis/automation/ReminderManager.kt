package com.kishore.jarvis.automation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.UUID

data class JarvisReminder(
    val id: String,
    val title: String,
    val triggerAtMillis: Long
)

class ReminderManager(
    private val context: Context
) {

    fun scheduleReminder(
        title: String,
        triggerAtMillis: Long
    ): Result<JarvisReminder> {

        if (title.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Reminder title cannot be empty.")
            )
        }

        if (triggerAtMillis <= System.currentTimeMillis()) {
            return Result.failure(
                IllegalArgumentException("Please choose a future time.")
            )
        }

        return try {
            val alarmManager = context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                return Result.failure(
                    IllegalStateException(
                        "Exact alarm permission is required. Please enable it in Android settings."
                    )
                )
            }

            val id = UUID.randomUUID().toString()

            val intent = Intent(
                context,
                ReminderReceiver::class.java
            ).apply {
                action = "com.kishore.jarvis.REMINDER"
                putExtra("reminder_id", id)
                putExtra("reminder_title", title.trim())
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )

            Result.success(
                JarvisReminder(
                    id = id,
                    title = title.trim(),
                    triggerAtMillis = triggerAtMillis
                )
            )

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun cancelReminder(id: String): Boolean {
        return try {
            val intent = Intent(
                context,
                ReminderReceiver::class.java
            ).apply {
                action = "com.kishore.jarvis.REMINDER"
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or
                    PendingIntent.FLAG_IMMUTABLE
            ) ?: return false

            val alarmManager = context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            true

        } catch (exception: Exception) {
            false
        }
    }
}
