package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.QuizGateApplication
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WatchdogForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "quizgate_watchdog_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.quizgate.action.START"
        const val ACTION_PING = "com.example.quizgate.action.PING"

        fun start(context: Context) {
            val intent = Intent(context, WatchdogForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var periodicJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification("Đang canh chừng thời gian màn hình", "Bảo vệ hoạt động")
        startForeground(NOTIFICATION_ID, notification)

        startPeriodicCheck()
        return START_STICKY
    }

    private fun startPeriodicCheck() {
        periodicJob?.cancel()
        periodicJob = serviceScope.launch {
            val app = application as? QuizGateApplication ?: return@launch
            val repo = app.repository

            while (isActive) {
                try {
                    val profile = repo.getActiveProfile()
                    if (profile != null) {
                        val ledger = repo.getTodayLedger(profile.id)
                        val watchedMins = ledger.watchedSeconds / 60
                        val capMins = profile.dailyCapMinutes ?: 120
                        val parentModeUntil = repo.getParentModeUntil()

                        val contentText = if (parentModeUntil != null) {
                            val remainingMins = (parentModeUntil - System.currentTimeMillis()) / 60_000
                            "Parent Mode: Còn ${remainingMins}p"
                        } else {
                            "${profile.name}: Đã xem ${watchedMins}p / $capMins p"
                        }

                        val updatedNotification = buildNotification("Quiz Gate đang bảo vệ", contentText)
                        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.notify(NOTIFICATION_ID, updatedNotification)
                    }
                } catch (_: Exception) {
                }
                delay(30_000L) // Update every 30s
            }
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        periodicJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
