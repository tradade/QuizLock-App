package com.quizlock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.quizlock.QuizGateApplication
import com.quizlock.service.WatchdogForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_BOOT_COMPLETED != action && Intent.ACTION_MY_PACKAGE_REPLACED != action) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? QuizGateApplication
                if (app != null && app.repository.isPinConfigured()) {
                    WatchdogForegroundService.start(context)
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Unable to restart watchdog after boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
