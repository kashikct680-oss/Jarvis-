package com.kishore.jarvis.automation

import android.content.Context

data class AppTaskResult(
    val appOpened: Boolean,
    val appName: String,
    val task: AppTask?,
    val message: String
)

class AppIntegrationManager(context: Context) {

    private val appLauncher = AppLauncher(context)
    private val taskPlanner = AppTaskPlanner()

    fun executeRequest(
        appName: String,
        userRequest: String
    ): AppTaskResult {

        if (appName.isBlank()) {
            return AppTaskResult(
                appOpened = false,
                appName = "",
                task = null,
                message = "Please tell me which app to open."
            )
        }

        val plan = taskPlanner.createPlan(
            appName = appName,
            userRequest = userRequest
        )

        val launchResult = appLauncher.launchApp(appName)

        return AppTaskResult(
            appOpened = launchResult.success,
            appName = appName,
            task = plan,
            message = if (launchResult.success) {
                "${launchResult.message} ${plan.statusMessage}"
            } else {
                launchResult.message
            }
        )
    }
}
