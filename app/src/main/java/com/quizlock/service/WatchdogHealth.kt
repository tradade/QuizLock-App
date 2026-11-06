package com.quizlock.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat

data class WatchdogHealth(
    val accessibilityEnabled: Boolean,
    val notificationsEnabled: Boolean,
    val batteryOptimizationIgnored: Boolean
) {
    val isOperational: Boolean
        get() = accessibilityEnabled
}

object WatchdogHealthChecker {

    fun check(context: Context): WatchdogHealth {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return WatchdogHealth(
            accessibilityEnabled = QuizGateAccessibilityService.isEnabled(context) ||
                QuizGateAccessibilityService.isServiceRunning,
            notificationsEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED,
            batteryOptimizationIgnored = powerManager
                ?.isIgnoringBatteryOptimizations(context.packageName) == true
        )
    }
}
