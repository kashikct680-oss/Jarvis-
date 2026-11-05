package com.kishore.jarvis.automation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val title = intent.getStringExtra(
            "reminder_title"
        ) ?: "JARVIS Reminder"

        val reminderId = intent.getStringExtra(
            "reminder_id"
        ) ?: System.currentTimeMillis().toString()

        val channelId = "jarvis_reminders"

        // Android 8.0 and later require a notification channel.
        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "JARVIS Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for JARVIS reminders"
            }

            notificationManager.createNotificationChannel(
                channel
            )
        }

        // Android 13 and later require notification permission.
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(
                android.R.drawable.ic_dialog_info
            )
            .setContentTitle("JARVIS Reminder")
            .setContentText(title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(title)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            reminderId.hashCode(),
            notification
        )
    }
}
