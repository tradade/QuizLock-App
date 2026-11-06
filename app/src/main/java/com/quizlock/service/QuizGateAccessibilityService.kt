package com.quizlock.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import com.quizlock.QuizGateApplication
import com.quizlock.core.data.QuizGateRepository
import com.quizlock.core.policy.LedgerState
import com.quizlock.core.policy.PolicyConfig
import com.quizlock.core.policy.PolicyEngine
import com.quizlock.core.policy.PolicyStrategy
import com.quizlock.core.policy.WatchdogDecision
import com.quizlock.ui.gate.HardBlockActivity
import com.quizlock.ui.gate.QuizGateActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class QuizGateAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "QuizGateAccessibility"

        private const val MAX_ACCOUNT_SECONDS = 2 * 60 * 60L
        private const val FLUSH_INTERVAL_MILLIS = 30_000L
        private const val MAX_WAKE_MILLIS = 5 * 60_000L
        private const val EVENT_DEBOUNCE_MILLIS = 150L

        @Volatile
        var isServiceRunning = false
            private set

        @Volatile
        var currentForegroundPackage: String? = null
            private set

        @Volatile
        var isGateActive = false

        @Volatile
        private var instance: QuizGateAccessibilityService? = null

        fun isEnabled(context: Context): Boolean {
            val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                ?: return false
            return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .any { it.resolveInfo?.serviceInfo?.packageName == context.packageName }
        }

        /**
         * Wakes the watchdog immediately after config, profile, gate or Parent Mode changes.
         * Cheap no-op when the service is not connected.
         */
        fun requestEvaluation() {
            instance?.restartTracking()
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val evaluationMutex = Mutex()
    private val policyEngine = PolicyEngine()
    private var trackingJob: Job? = null
    private var screenStateReceiver: BroadcastReceiver? = null

    private var lastAccountedElapsed = 0L
    private var wasWatching = false
    private var pendingWatchSeconds = 0L
    private var pendingProfileId: Long? = null
    private var lastFlushElapsedRealtime = 0L

    @Volatile
    private var isScreenOn = true

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        instance = this
        currentForegroundPackage = null
        Log.d(TAG, "QuizGateAccessibilityService connected.")
        registerScreenStateReceiver()
        restartTracking()

        // Fallback: on newer Android versions a BOOT_COMPLETED-triggered specialUse
        // foreground service may be rejected. Start it from the bound accessibility
        // service as soon as setup is complete.
        serviceScope.launch {
            val app = application as? QuizGateApplication ?: return@launch
            if (app.repository.isPinConfigured()) {
                WatchdogForegroundService.start(this@QuizGateAccessibilityService)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkgName = event.packageName?.toString() ?: return
        if (pkgName == currentForegroundPackage) return
        currentForegroundPackage = pkgName
        restartTracking()
    }

    override fun onInterrupt() {
        Log.w(TAG, "QuizGateAccessibilityService interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        if (instance === this) instance = null
        trackingJob?.cancel()
        screenStateReceiver?.let { receiver ->
            runCatching { unregisterReceiver(receiver) }
        }
        screenStateReceiver = null
        serviceScope.cancel()
    }

    private fun registerScreenStateReceiver() {
        if (screenStateReceiver != null) return

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        isScreenOn = powerManager?.isInteractive ?: true

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                isScreenOn = intent?.action == Intent.ACTION_SCREEN_ON
                restartTracking()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        screenStateReceiver = receiver
    }

    private fun restartTracking() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            delay(EVENT_DEBOUNCE_MILLIS)
            while (isActive) {
                val nextDelay = try {
                    evaluateAndGetNextDelay()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Watchdog evaluation failed", e)
                    MAX_WAKE_MILLIS
                }
                delay(nextDelay)
            }
        }
    }

    private suspend fun evaluateAndGetNextDelay(): Long = evaluationMutex.withLock {
        val nowElapsed = SystemClock.elapsedRealtime()
        val nowMillis = System.currentTimeMillis()

        if (lastAccountedElapsed == 0L) {
            lastAccountedElapsed = nowElapsed
            lastFlushElapsedRealtime = nowElapsed
        }

        val app = application as? QuizGateApplication ?: return@withLock MAX_WAKE_MILLIS
        val repo = app.repository

        val profile = repo.getActiveProfile()
        val config = if (profile != null) {
            repo.getEffectivePolicyConfig(profile)
        } else {
            repo.getPolicyConfig()
        }
        val pkg = currentForegroundPackage ?: packageName
        val isOurApp = pkg == packageName
        val isExcluded = isOurApp || repo.isAppExcluded(pkg)
        val parentModeUntil = repo.getParentModeUntil()
        val isParentModeActive = parentModeUntil != null && nowMillis < parentModeUntil
        val withinAllowedHours = policyEngine.isWithinAllowedHours(
            nowMillis,
            config.allowedStartTime,
            config.allowedEndTime
        )

        // Account the interval since the last evaluation if we were watching during it.
        if (wasWatching && pendingProfileId != null) {
            val elapsedSeconds = ((nowElapsed - lastAccountedElapsed) / 1000L)
                .coerceIn(0L, MAX_ACCOUNT_SECONDS)
            if (elapsedSeconds > 0) {
                pendingWatchSeconds += elapsedSeconds
            }
        }
        lastAccountedElapsed = nowElapsed

        val shouldWatch = profile != null && isScreenOn && !isExcluded && !isGateActive &&
            !isParentModeActive && withinAllowedHours
        wasWatching = shouldWatch

        // If the active profile changed, flush pending seconds to the previous profile.
        if (pendingProfileId != null && pendingProfileId != profile?.id) {
            flushPendingWatchSeconds(repo)
            pendingProfileId = null
        }
        if (shouldWatch && pendingProfileId == null) {
            pendingProfileId = profile?.id
        }

        val ledger = profile?.let { repo.getTodayLedger(it.id) }
        val effectiveWatchedSeconds = (ledger?.watchedSeconds ?: 0L) +
            if (pendingProfileId == profile?.id) pendingWatchSeconds else 0L

        // Flush pending time at most once every 30 seconds.
        if (pendingWatchSeconds > 0 &&
            nowElapsed - lastFlushElapsedRealtime >= FLUSH_INTERVAL_MILLIS
        ) {
            flushPendingWatchSeconds(repo)
        }

        var nextWakeMillis = MAX_WAKE_MILLIS

        if (profile != null && ledger != null) {
            val state = LedgerState(
                watchedSecondsToday = effectiveWatchedSeconds,
                gatesPassedToday = ledger.gatesPassed,
                lastGateAnchorSeconds = ledger.gateAnchorSeconds
            )

            val decision = policyEngine.decide(
                state = state,
                config = config,
                currentTimeMillis = nowMillis,
                isForegroundExcluded = isExcluded,
                parentModeUntilMillis = parentModeUntil
            )

            when (decision) {
                is WatchdogDecision.TriggerGate -> {
                    if (isScreenOn && !isGateActive && !isOurApp) {
                        flushPendingWatchSeconds(repo)
                        wasWatching = false
                        lastAccountedElapsed = SystemClock.elapsedRealtime()
                        launchQuizGate(decision.questionCount)
                    }
                }
                is WatchdogDecision.BlockedHard -> {
                    if (isScreenOn && !isGateActive && !isOurApp) {
                        flushPendingWatchSeconds(repo)
                        wasWatching = false
                        lastAccountedElapsed = SystemClock.elapsedRealtime()
                        launchHardBlock(decision.reason.name)
                    }
                }
                else -> Unit
            }

            // Wake exactly at the next boundary that can change the decision.
            val boundaries = mutableListOf<Long>()

            if (shouldWatch) {
                boundaries += FLUSH_INTERVAL_MILLIS / 1000L
                val gateAtSeconds = state.lastGateAnchorSeconds + config.gateIntervalMinutes * 60L
                boundaries += (gateAtSeconds - effectiveWatchedSeconds).coerceAtLeast(1L)
                if (config.strategy == PolicyStrategy.DAILY_CAP) {
                    boundaries += (config.dailyCapMinutes * 60L - effectiveWatchedSeconds)
                        .coerceAtLeast(1L)
                }
            }
            if (isParentModeActive) {
                val parentModeEnd = parentModeUntil ?: nowMillis
                boundaries += ((parentModeEnd - nowMillis) / 1000L).coerceAtLeast(1L)
            }
            boundaries += secondsUntilAllowedHoursBoundary(nowMillis, config)
            boundaries += secondsUntilMidnight(nowMillis)

            val nextSeconds = boundaries.minOrNull() ?: (MAX_WAKE_MILLIS / 1000L)
            nextWakeMillis = (nextSeconds * 1000L).coerceIn(1_000L, MAX_WAKE_MILLIS)
        }

        if (!isScreenOn) {
            nextWakeMillis = MAX_WAKE_MILLIS
        }

        nextWakeMillis
    }

    private suspend fun flushPendingWatchSeconds(repo: QuizGateRepository) {
        val profileId = pendingProfileId ?: return
        val seconds = pendingWatchSeconds
        if (seconds <= 0) return

        repo.recordWatchSeconds(profileId, seconds)
        pendingWatchSeconds = 0L
        lastFlushElapsedRealtime = SystemClock.elapsedRealtime()
    }

    private fun secondsUntilAllowedHoursBoundary(nowMillis: Long, config: PolicyConfig): Long {
        return try {
            val zone = ZoneId.systemDefault()
            val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
            val start = LocalTime.parse(config.allowedStartTime, timeFormatter)
            val end = LocalTime.parse(config.allowedEndTime, timeFormatter)
            val isWithin = policyEngine.isWithinAllowedHours(
                nowMillis,
                config.allowedStartTime,
                config.allowedEndTime
            )
            val targetTime = if (isWithin) end else start
            var target = now.toLocalDate().atTime(targetTime).atZone(zone)
            if (!target.isAfter(now)) target = target.plusDays(1)
            Duration.between(now, target).seconds.coerceAtLeast(1L)
        } catch (_: Exception) {
            MAX_WAKE_MILLIS / 1000L
        }
    }

    private fun secondsUntilMidnight(nowMillis: Long): Long {
        return try {
            val zone = ZoneId.systemDefault()
            val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
            Duration.between(now, nextMidnight).seconds.coerceAtLeast(1L)
        } catch (_: Exception) {
            MAX_WAKE_MILLIS / 1000L
        }
    }

    private fun launchQuizGate(questionCount: Int) {
        isGateActive = true
        val intent = Intent(this, QuizGateActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(QuizGateActivity.EXTRA_QUESTION_COUNT, questionCount)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            isGateActive = false
            Log.e(TAG, "Unable to launch quiz gate", e)
        }
    }

    private fun launchHardBlock(reason: String) {
        isGateActive = true
        val intent = Intent(this, HardBlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(HardBlockActivity.EXTRA_BLOCK_REASON, reason)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            isGateActive = false
            Log.e(TAG, "Unable to launch hard block", e)
        }
    }
}
