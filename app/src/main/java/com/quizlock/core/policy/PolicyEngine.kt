package com.quizlock.core.policy

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class PolicyStrategy {
    DAILY_CAP,    // Default: Quiz earns time within daily cap
    INFINITE,     // Quiz grants +Y minutes, no daily cap
    ESCALATING    // Cost in questions escalates per gate (1 -> 2 -> 3...)
}

enum class BlockReason {
    OUTSIDE_ALLOWED_HOURS,
    DAILY_CAP_REACHED,
    WATCHDOG_BROKEN
}

sealed class WatchdogDecision {
    data object Armed : WatchdogDecision()
    data class TriggerGate(val questionCount: Int, val elapsedWatchSeconds: Long) : WatchdogDecision()
    data class BlockedHard(val reason: BlockReason) : WatchdogDecision()
    data class InParentMode(val remainingSeconds: Long) : WatchdogDecision()
    data class Broken(val missingPermissions: List<String>) : WatchdogDecision()
}

data class PolicyConfig(
    val strategy: PolicyStrategy = PolicyStrategy.DAILY_CAP,
    val gateIntervalMinutes: Int = 30,      // X
    val rewardMinutes: Int = 30,             // Y
    val dailyCapMinutes: Int = 120,          // Total allowed per day
    val allowedStartTime: String = "06:00",  // HH:mm
    val allowedEndTime: String = "21:00",    // HH:mm
    val maxQuestionsPerGate: Int = 5
)

data class LedgerState(
    val watchedSecondsToday: Long = 0L,
    val gatesPassedToday: Int = 0,
    val lastGateAnchorSeconds: Long = 0L
)

class PolicyEngine(
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun isWithinAllowedHours(currentTimeMillis: Long, startTimeStr: String, endTimeStr: String): Boolean {
        return try {
            val nowTime = Instant.ofEpochMilli(currentTimeMillis).atZone(zoneId).toLocalTime()
            val start = LocalTime.parse(startTimeStr, timeFormatter)
            val end = LocalTime.parse(endTimeStr, timeFormatter)

            if (end.isAfter(start)) {
                !nowTime.isBefore(start) && nowTime.isBefore(end)
            } else {
                // Crosses midnight (e.g. 21:00 to 06:00)
                !nowTime.isBefore(start) || nowTime.isBefore(end)
            }
        } catch (_: Exception) {
            true // Default to allowed if parsing error
        }
    }

    fun getLocalDate(currentTimeMillis: Long): String {
        return Instant.ofEpochMilli(currentTimeMillis).atZone(zoneId).toLocalDate().toString()
    }

    fun decide(
        state: LedgerState,
        config: PolicyConfig,
        currentTimeMillis: Long,
        isForegroundExcluded: Boolean,
        parentModeUntilMillis: Long?,
        missingPermissions: List<String> = emptyList()
    ): WatchdogDecision {
        // 1. Check permissions / health
        if (missingPermissions.isNotEmpty()) {
            return WatchdogDecision.Broken(missingPermissions)
        }

        // 2. Check Parent Mode
        if (parentModeUntilMillis != null && currentTimeMillis < parentModeUntilMillis) {
            val remainingSec = (parentModeUntilMillis - currentTimeMillis) / 1000L
            return WatchdogDecision.InParentMode(remainingSec.coerceAtLeast(0L))
        }

        // 3. Excluded apps (learning apps / Quiz Gate itself) are exempt
        if (isForegroundExcluded) {
            return WatchdogDecision.Armed
        }

        // 4. Allowed Time Window check
        if (!isWithinAllowedHours(currentTimeMillis, config.allowedStartTime, config.allowedEndTime)) {
            return WatchdogDecision.BlockedHard(BlockReason.OUTSIDE_ALLOWED_HOURS)
        }

        // 5. Daily Cap check (for DAILY_CAP strategy)
        val dailyCapSeconds = config.dailyCapMinutes * 60L
        if (config.strategy == PolicyStrategy.DAILY_CAP && state.watchedSecondsToday >= dailyCapSeconds) {
            return WatchdogDecision.BlockedHard(BlockReason.DAILY_CAP_REACHED)
        }

        // 6. Gate Interval Check (X minutes since last gate anchor)
        val intervalSeconds = config.gateIntervalMinutes * 60L
        val secondsSinceLastGate = state.watchedSecondsToday - state.lastGateAnchorSeconds

        if (secondsSinceLastGate >= intervalSeconds) {
            val questionsRequired = when (config.strategy) {
                PolicyStrategy.DAILY_CAP, PolicyStrategy.INFINITE -> 1
                PolicyStrategy.ESCALATING -> (1 + state.gatesPassedToday).coerceAtMost(config.maxQuestionsPerGate)
            }
            return WatchdogDecision.TriggerGate(
                questionCount = questionsRequired,
                elapsedWatchSeconds = state.watchedSecondsToday
            )
        }

        return WatchdogDecision.Armed
    }
}
