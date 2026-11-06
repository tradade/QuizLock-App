package com.quizlock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quizlock.service.WatchdogForegroundService

class WatchdogRestartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Best-effort restart when the task is swiped away or an OEM killer stops the service.
        // WatchdogForegroundService.start() safely handles platform background-start limits.
        WatchdogForegroundService.start(context)
    }
}
