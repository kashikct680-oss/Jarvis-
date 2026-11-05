package com.kishore.jarvis.automation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

data class AppLaunchResult(
    val success: Boolean,
    val message: String,
    val packageName: String = ""
)

class AppLauncher(
    private val context: Context
) {

    private val packageManager: PackageManager =
        context.packageManager

    fun launchApp(appName: String): AppLaunchResult {

        if (appName.isBlank()) {
            return AppLaunchResult(
                false,
                "Please tell me which app to open."
            )
        }

        val launcherIntent = Intent(
            Intent.ACTION_MAIN
        ).addCategory(
            Intent.CATEGORY_LAUNCHER
        )

        val apps = packageManager.queryIntentActivities(
            launcherIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        val searchName = appName.trim()
            .lowercase()

        val matchingApp = apps.firstOrNull {
            it.loadLabel(packageManager)
                .toString()
                .trim()
                .lowercase() == searchName
        } ?: apps.firstOrNull {
            it.loadLabel(packageManager)
                .toString()
                .trim()
                .lowercase()
                .contains(searchName)
        }

        if (matchingApp == null) {
            return AppLaunchResult(
                false,
                "I could not find $appName on your phone."
            )
        }

        val packageName =
            matchingApp.activityInfo.packageName

        return try {

            val launchIntent =
                packageManager.getLaunchIntentForPackage(
                    packageName
                )

            if (launchIntent == null) {
                AppLaunchResult(
                    false,
                    "This app cannot be opened directly.",
                    packageName
                )
            } else {

                launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                context.startActivity(launchIntent)

                AppLaunchResult(
                    true,
                    "Opening ${matchingApp.loadLabel(packageManager)}.",
                    packageName
                )
            }

        } catch (exception: Exception) {

            AppLaunchResult(
                false,
                "Unable to open $appName.",
                packageName
            )
        }
    }
}
