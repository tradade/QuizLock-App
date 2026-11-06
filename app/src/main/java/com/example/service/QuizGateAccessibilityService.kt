package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.QuizGateApplication
import com.example.core.policy.LedgerState
import com.example.core.policy.PolicyEngine
import com.example.core.policy.WatchdogDecision
import com.example.ui.gate.HardBlockActivity
import com.example.ui.gate.QuizGateActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class QuizGateAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "QuizGateAccessibility"
        @Volatile
        var isServiceRunning = false
            private set

        @Volatile
        var currentForegroundPackage: String? = null
            private set

        @Volatile
        var isGateActive = false
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var trackingJob: Job? = null
    private val policyEngine = PolicyEngine()
    private var lastTickElapsedRealtime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Log.d(TAG, "QuizGateAccessibilityService connected.")
        startTracking()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkgName = event.packageName?.toString() ?: return
        currentForegroundPackage = pkgName
    }

    override fun onInterrupt() {
        Log.w(TAG, "QuizGateAccessibilityService interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        trackingJob?.cancel()
    }

    private fun startTracking() {
        trackingJob?.cancel()
        lastTickElapsedRealtime = SystemClock.elapsedRealtime()

        trackingJob = serviceScope.launch {
            val app = application as? QuizGateApplication ?: return@launch
            val repo = app.repository

            while (isActive) {
                delay(2000L) // Check every 2 seconds
                val nowRealtime = SystemClock.elapsedRealtime()
                val deltaSeconds = ((nowRealtime - lastTickElapsedRealtime) / 1000L).coerceAtLeast(0L)
                lastTickElapsedRealtime = nowRealtime

                try {
                    val profile = repo.getActiveProfile() ?: continue
                    val pkg = currentForegroundPackage ?: packageName
                    val isOurApp = (pkg == packageName)
                    val isExcluded = isOurApp || repo.isAppExcluded(pkg)

                    // Only accumulate watch time if NOT an excluded app and gate is NOT currently active
                    if (!isExcluded && !isGateActive && deltaSeconds > 0) {
                        repo.recordWatchSeconds(profile.id, deltaSeconds)
                    }

                    // Evaluate policy (with per-profile custom overrides if set)
                    val ledger = repo.getTodayLedger(profile.id)
                    val config = repo.getEffectivePolicyConfig(profile)
                    val parentModeUntil = repo.getParentModeUntil()

                    val state = LedgerState(
                        watchedSecondsToday = ledger.watchedSeconds,
                        gatesPassedToday = ledger.gatesPassed,
                        lastGateAnchorSeconds = ledger.gateAnchorSeconds
                    )

                    val decision = policyEngine.decide(
                        state = state,
                        config = config,
                        currentTimeMillis = System.currentTimeMillis(),
                        isForegroundExcluded = isExcluded,
                        parentModeUntilMillis = parentModeUntil
                    )

                    when (decision) {
                        is WatchdogDecision.TriggerGate -> {
                            if (!isGateActive && !isOurApp) {
                                launchQuizGate(decision.questionCount)
                            }
                        }
                        is WatchdogDecision.BlockedHard -> {
                            if (!isGateActive && !isOurApp) {
                                launchHardBlock(decision.reason.name)
                            }
                        }
                        else -> {
                            // Armed or InParentMode -> do nothing
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in watchdog tracking loop", e)
                }
            }
        }
    }

    private fun launchQuizGate(questionCount: Int) {
        isGateActive = true
        val intent = Intent(this, QuizGateActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(QuizGateActivity.EXTRA_QUESTION_COUNT, questionCount)
        }
        startActivity(intent)
    }

    private fun launchHardBlock(reason: String) {
        isGateActive = true
        val intent = Intent(this, HardBlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(HardBlockActivity.EXTRA_BLOCK_REASON, reason)
        }
        startActivity(intent)
    }
}
