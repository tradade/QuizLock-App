package com.example

import com.example.core.policy.BlockReason
import com.example.core.policy.LedgerState
import com.example.core.policy.PolicyConfig
import com.example.core.policy.PolicyEngine
import com.example.core.policy.PolicyStrategy
import com.example.core.policy.WatchdogDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class PolicyEngineTest {

    private val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    private val engine = PolicyEngine(zoneId)

    @Test
    fun `armed when watched time is below gate interval`() {
        val config = PolicyConfig(
            strategy = PolicyStrategy.DAILY_CAP,
            gateIntervalMinutes = 30,
            dailyCapMinutes = 120,
            allowedStartTime = "06:00",
            allowedEndTime = "21:00"
        )
        val state = LedgerState(
            watchedSecondsToday = 15 * 60L, // 15 mins watched
            gatesPassedToday = 0,
            lastGateAnchorSeconds = 0L
        )

        // 10:00 AM today
        val now = ZonedDateTime.of(2026, 11, 5, 10, 0, 0, 0, zoneId).toInstant().toEpochMilli()

        val decision = engine.decide(
            state = state,
            config = config,
            currentTimeMillis = now,
            isForegroundExcluded = false,
            parentModeUntilMillis = null
        )

        assertEquals(WatchdogDecision.Armed, decision)
    }

    @Test
    fun `triggers gate when watched interval X is reached`() {
        val config = PolicyConfig(
            strategy = PolicyStrategy.DAILY_CAP,
            gateIntervalMinutes = 30,
            dailyCapMinutes = 120,
            allowedStartTime = "06:00",
            allowedEndTime = "21:00"
        )
        val state = LedgerState(
            watchedSecondsToday = 30 * 60L, // 30 mins watched
            gatesPassedToday = 0,
            lastGateAnchorSeconds = 0L
        )

        val now = ZonedDateTime.of(2026, 11, 5, 10, 0, 0, 0, zoneId).toInstant().toEpochMilli()

        val decision = engine.decide(
            state = state,
            config = config,
            currentTimeMillis = now,
            isForegroundExcluded = false,
            parentModeUntilMillis = null
        )

        assertTrue(decision is WatchdogDecision.TriggerGate)
        assertEquals(1, (decision as WatchdogDecision.TriggerGate).questionCount)
    }

    @Test
    fun `escalating strategy increases question count per gate passed`() {
        val config = PolicyConfig(
            strategy = PolicyStrategy.ESCALATING,
            gateIntervalMinutes = 30,
            maxQuestionsPerGate = 5
        )
        val state = LedgerState(
            watchedSecondsToday = 60 * 60L,
            gatesPassedToday = 2, // 2 gates passed previously
            lastGateAnchorSeconds = 30 * 60L // exactly 30 mins since last anchor
        )

        val now = ZonedDateTime.of(2026, 11, 5, 10, 0, 0, 0, zoneId).toInstant().toEpochMilli()

        val decision = engine.decide(
            state = state,
            config = config,
            currentTimeMillis = now,
            isForegroundExcluded = false,
            parentModeUntilMillis = null
        )

        assertTrue(decision is WatchdogDecision.TriggerGate)
        assertEquals(3, (decision as WatchdogDecision.TriggerGate).questionCount)
    }

    @Test
    fun `hard block when daily cap is reached`() {
        val config = PolicyConfig(
            strategy = PolicyStrategy.DAILY_CAP,
            dailyCapMinutes = 120
        )
        val state = LedgerState(
            watchedSecondsToday = 120 * 60L, // exactly 120 mins
            gatesPassedToday = 4,
            lastGateAnchorSeconds = 120 * 60L
        )

        val now = ZonedDateTime.of(2026, 11, 5, 14, 0, 0, 0, zoneId).toInstant().toEpochMilli()

        val decision = engine.decide(
            state = state,
            config = config,
            currentTimeMillis = now,
            isForegroundExcluded = false,
            parentModeUntilMillis = null
        )

        assertTrue(decision is WatchdogDecision.BlockedHard)
        assertEquals(BlockReason.DAILY_CAP_REACHED, (decision as WatchdogDecision.BlockedHard).reason)
    }

    @Test
    fun `hard block when outside allowed bedtime window`() {
        val config = PolicyConfig(
            allowedStartTime = "06:00",
            allowedEndTime = "21:00"
        )
        val state = LedgerState(watchedSecondsToday = 10 * 60L)

        // 22:30 PM (after bedtime)
        val now = ZonedDateTime.of(2026, 11, 5, 22, 30, 0, 0, zoneId).toInstant().toEpochMilli()

        val decision = engine.decide(
            state = state,
            config = config,
            currentTimeMillis = now,
            isForegroundExcluded = false,
            parentModeUntilMillis = null
        )

        assertTrue(decision is WatchdogDecision.BlockedHard)
        assertEquals(BlockReason.OUTSIDE_ALLOWED_HOURS, (decision as WatchdogDecision.BlockedHard).reason)
    }

    @Test
    fun `in parent mode pauses counting and gates`() {
        val config = PolicyConfig(gateIntervalMinutes = 30)
        val state = LedgerState(watchedSecondsToday = 45 * 60L)

        val now = ZonedDateTime.of(2026, 11, 5, 10, 0, 0, 0, zoneId).toInstant().toEpochMilli()
        val parentUntil = now + (2 * 3600 * 1000L) // 2 hours

        val decision = engine.decide(
            state = state,
            config = config,
            currentTimeMillis = now,
            isForegroundExcluded = false,
            parentModeUntilMillis = parentUntil
        )

        assertTrue(decision is WatchdogDecision.InParentMode)
    }

    @Test
    fun `excluded app is never blocked or gated`() {
        val config = PolicyConfig(
            dailyCapMinutes = 60,
            allowedStartTime = "06:00",
            allowedEndTime = "21:00"
        )
        val state = LedgerState(watchedSecondsToday = 120 * 60L)

        // Even after bedtime on an excluded learning app
        val now = ZonedDateTime.of(2026, 11, 5, 23, 0, 0, 0, zoneId).toInstant().toEpochMilli()

        val decision = engine.decide(
            state = state,
            config = config,
            currentTimeMillis = now,
            isForegroundExcluded = true,
            parentModeUntilMillis = null
        )

        assertEquals(WatchdogDecision.Armed, decision)
    }
}
