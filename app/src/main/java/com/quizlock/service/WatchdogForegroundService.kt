package com.quizlock.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.quizlock.MainActivity
import com.quizlock.QuizGateApplication
import com.quizlock.R
import com.quizlock.core.i18n.AppStrings
import com.quizlock.receiver.WatchdogRestartReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WatchdogForegroundService : Service() {

    companion object {
        private const val TAG = "WatchdogFgs"
        private const val NOTIFICATION_REFRESH_MILLIS = 60_000L
        private const val HEALTH_CHECK_INTERVAL_MILLIS = 60L * 60L * 1000L

        const val CHANNEL_ID = "quizgate_watchdog_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.quizlock.quizgate.action.START"
        const val ACTION_PING = "com.quizlock.quizgate.action.PING"

        fun start(context: Context) {
            val intent = Intent(context, WatchdogForegroundService::class.java).apply {
                action = ACTION_START
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Android 12+ can reject foreground-service starts from the background.
                // The accessibility service and BootReceiver will retry when allowed.
                Log.w(TAG, "Unable to start watchdog foreground service", e)
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
        val notification = buildNotification(
            getString(R.string.fgs_title),
            getString(R.string.fgs_content)
        )
        startForeground(NOTIFICATION_ID, notification)

        startPeriodicCheck()
        return START_STICKY
    }

    private fun startPeriodicCheck() {
        periodicJob?.cancel()
        periodicJob = serviceScope.launch {
            val app = application as? QuizGateApplication ?: return@launch
            val repo = app.repository
            var lastSignature: String? = null
            var lastHealth: WatchdogHealth? = null
            var lastHealthCheckElapsed = 0L

            while (isActive) {
                try {
                    val strings = AppStrings.of(repo.getAppLanguage())

                    // Permission health is checked hourly; the progress notification
                    // still refreshes every minute.
                    val nowElapsed = SystemClock.elapsedRealtime()
                    if (lastHealth == null ||
                        nowElapsed - lastHealthCheckElapsed >= HEALTH_CHECK_INTERVAL_MILLIS
                    ) {
                        lastHealth = WatchdogHealthChecker.check(this@WatchdogForegroundService)
                        lastHealthCheckElapsed = nowElapsed
                    }
                    val health = lastHealth
                        ?: WatchdogHealthChecker.check(this@WatchdogForegroundService)

                    val profile = repo.getActiveProfile()
                    val parentModeUntil = repo.getParentModeUntil()

                    val title: String
                    val contentText: String
                    val showFixAction: Boolean

                    if (!health.accessibilityEnabled) {
                        title = strings.notifBrokenTitle
                        contentText = strings.notifBrokenContent
                        showFixAction = true
                    } else if (profile != null) {
                        val ledger = repo.getTodayLedger(profile.id)
                        val watchedMins = ledger.watchedSeconds / 60
                        val capMins = profile.dailyCapMinutes ?: 120
                        title = strings.notifTitle
                        contentText = if (parentModeUntil != null) {
                            val remainingMins = (parentModeUntil - System.currentTimeMillis()) / 60_000
                            strings.notifParentModeRemaining.format(remainingMins)
                        } else {
                            strings.notifWatchProgress.format(profile.name, watchedMins, capMins)
                        }
                        showFixAction = false
                    } else {
                        title = strings.notifTitle
                        contentText = strings.notifNoProfile
                        showFixAction = false
                    }

                    val signature = "$title|$contentText|$showFixAction"
                    if (signature != lastSignature) {
                        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.notify(
                            NOTIFICATION_ID,
                            buildNotification(title, contentText, showFixAction)
                        )
                        lastSignature = signature
                    }
                } catch (_: Exception) {
                }
                delay(NOTIFICATION_REFRESH_MILLIS) // Notify only on change
            }
        }
    }

    private fun buildNotification(
        title: String,
        content: String,
        showFixAction: Boolean = false
    ): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_stat_quizgate)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (showFixAction) {
            val fixPendingIntent = PendingIntent.getActivity(
                this,
                1,
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, getString(R.string.notif_fix_action), fixPendingIntent)
        }

        return builder.build()
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        scheduleRestart()
    }

    private fun scheduleRestart() {
        try {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val restartIntent = Intent(this, WatchdogRestartReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                2,
                restartIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 5_000L,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.w(TAG, "Unable to schedule watchdog restart", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        periodicJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
