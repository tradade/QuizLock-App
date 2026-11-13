package com.quizlock.ui.parent

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.window.DialogProperties
import com.quizlock.core.data.ProfileEntity
import com.quizlock.core.data.QuizGateRepository
import com.quizlock.core.data.hasCustomPolicy
import com.quizlock.core.i18n.AppLanguage
import com.quizlock.core.i18n.LocalAppLanguage
import com.quizlock.core.i18n.LocalStrings
import com.quizlock.core.policy.PolicyConfig
import com.quizlock.core.policy.PolicyStrategy
import com.quizlock.service.QuizGateAccessibilityService
import com.quizlock.ui.components.PinPadModal
import com.quizlock.ui.components.TimePickerModal
import com.quizlock.ui.theme.QuizBluePrimary
import com.quizlock.ui.theme.Slate400
import com.quizlock.ui.theme.TvFocusBorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val isExcluded: Boolean
)

@Composable
fun SettingsScreen(
    repository: QuizGateRepository,
    onBack: () -> Unit,
    profile: ProfileEntity? = null
) {
    val s = LocalStrings.current
    val currentLang = LocalAppLanguage.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isProfileMode = profile != null
    var currentProfile by remember(profile) { mutableStateOf(profile) }
    var inheritsGlobal by remember(profile) {
        mutableStateOf(profile?.hasCustomPolicy != true)
    }

    var selectedSettingsTab by remember { mutableIntStateOf(0) }

    var policyConfig by remember { mutableStateOf(PolicyConfig()) }
    var gateIntervalX by remember { mutableFloatStateOf(30f) }
    var dailyCap by remember { mutableFloatStateOf(120f) }
    var startTime by remember { mutableStateOf("06:00") }
    var endTime by remember { mutableStateOf("21:00") }
    var selectedStrategy by remember { mutableStateOf(PolicyStrategy.DAILY_CAP) }

    val installedApps = remember { mutableStateListOf<InstalledAppInfo>() }
    var isAppsLoading by remember { mutableStateOf(true) }
    var appSearchQuery by remember { mutableStateOf("") }

    var showPinModalForChange by remember { mutableStateOf(false) }
    var showNewPinDialog by remember { mutableStateOf(false) }
    var showTosPrivacyScreen by remember { mutableStateOf(false) }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    if (isProfileMode) {
        BackHandler { onBack() }
    }

    if (showTosPrivacyScreen) {
        TosPrivacyScreen(onBack = { showTosPrivacyScreen = false })
        return
    }

    fun parseTimeToMinutes(timeStr: String): Int? {
        val parts = timeStr.trim().split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    val startMinutes = parseTimeToMinutes(startTime)
    val endMinutes = parseTimeToMinutes(endTime)

    val timeValidationError: String? = when {
        startMinutes == null || endMinutes == null -> s.timeInvalidFormat
        startMinutes >= endMinutes -> s.timeStartBeforeEnd.format(startTime, endTime)
        (endMinutes - startMinutes) < 30 -> s.timeWindowTooShort
        else -> null
    }

    val totalWindowMinutes = if (startMinutes != null && endMinutes != null && startMinutes < endMinutes) {
        endMinutes - startMinutes
    } else null

    LaunchedEffect(Unit) {
        val targetProfile = profile
        val cfg = if (targetProfile != null) {
            repository.getEffectivePolicyConfig(targetProfile)
        } else {
            repository.getPolicyConfig()
        }
        policyConfig = cfg
        gateIntervalX = cfg.gateIntervalMinutes.coerceIn(10, 90).toFloat()
        dailyCap = cfg.dailyCapMinutes.coerceIn(30, 240).toFloat()
        startTime = cfg.allowedStartTime
        endTime = cfg.allowedEndTime
        selectedStrategy = cfg.strategy
        inheritsGlobal = targetProfile?.hasCustomPolicy != true

        // Load installed launchable apps only for the global settings screen
        if (targetProfile == null) {
            withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
                val appList = resolveInfos.mapNotNull { ri ->
                    val pkg = ri.activityInfo.packageName
                    val label = ri.loadLabel(pm).toString()
                    val isExc = repository.isAppExcluded(pkg)
                    InstalledAppInfo(packageName = pkg, label = label, isExcluded = isExc)
                }.sortedBy { it.label }

                withContext(Dispatchers.Main) {
                    installedApps.clear()
                    installedApps.addAll(appList)
                    isAppsLoading = false
                }
            }
        }
    }

    fun saveConfigChanges(newStart: String = startTime, newEnd: String = endTime) {
        val sMin = parseTimeToMinutes(newStart)
        val eMin = parseTimeToMinutes(newEnd)
        if (sMin == null || eMin == null || sMin >= eMin) {
            return
        }

        val targetProfile = currentProfile
        if (targetProfile != null) {
            if (inheritsGlobal) return
            val updated = targetProfile.copy(
                strategy = selectedStrategy.name,
                gateIntervalMinutes = gateIntervalX.toInt(),
                dailyCapMinutes = dailyCap.toInt(),
                allowedStartTime = newStart,
                allowedEndTime = newEnd
            )
            currentProfile = updated
            coroutineScope.launch {
                repository.updateProfile(updated)
                QuizGateAccessibilityService.requestEvaluation()
            }
        } else {
            coroutineScope.launch {
                val updated = policyConfig.copy(
                    strategy = selectedStrategy,
                    gateIntervalMinutes = gateIntervalX.toInt(),
                    dailyCapMinutes = dailyCap.toInt(),
                    allowedStartTime = newStart,
                    allowedEndTime = newEnd
                )
                repository.savePolicyConfig(updated)
                policyConfig = updated
                QuizGateAccessibilityService.requestEvaluation()
            }
        }
    }

    fun setInheritGlobal(inherit: Boolean) {
        val targetProfile = currentProfile ?: return
        inheritsGlobal = inherit
        if (inherit) {
            val cleared = targetProfile.copy(
                strategy = null,
                gateIntervalMinutes = null,
                dailyCapMinutes = null,
                allowedStartTime = null,
                allowedEndTime = null
            )
            currentProfile = cleared
            coroutineScope.launch {
                repository.updateProfile(cleared)
                QuizGateAccessibilityService.requestEvaluation()
                val cfg = repository.getPolicyConfig()
                gateIntervalX = cfg.gateIntervalMinutes.toFloat()
                dailyCap = cfg.dailyCapMinutes.toFloat()
                startTime = cfg.allowedStartTime
                endTime = cfg.allowedEndTime
                selectedStrategy = cfg.strategy
            }
        } else {
            val custom = targetProfile.copy(
                strategy = selectedStrategy.name,
                gateIntervalMinutes = gateIntervalX.toInt(),
                dailyCapMinutes = dailyCap.toInt(),
                allowedStartTime = startTime,
                allowedEndTime = endTime
            )
            currentProfile = custom
            coroutineScope.launch {
                repository.updateProfile(custom)
                QuizGateAccessibilityService.requestEvaluation()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) {
        // Top Header with Title and Quick Language Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isProfileMode) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("profile_settings_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = s.back,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isProfileMode) {
                        s.profileSettingsTitle.format(profile?.name ?: "")
                    } else {
                        s.settingsTitle
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isProfileMode) s.profileSettingsSubtitle else s.globalPolicyDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                onClick = {
                    coroutineScope.launch {
                        val next = if (currentLang == AppLanguage.VI) AppLanguage.EN else AppLanguage.VI
                        repository.setAppLanguage(next)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.testTag("settings_quick_lang_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currentLang.flag} ${currentLang.code.uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Navigation Tabs (4 Tabs). Profile mode only exposes per-profile policy.
        if (!isProfileMode) {
            TabRow(
                selectedTabIndex = selectedSettingsTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_tab_row")
            ) {
                Tab(
                    selected = selectedSettingsTab == 0,
                    onClick = { selectedSettingsTab = 0 },
                    text = { Text(s.tabPolicy, maxLines = 1, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_rules")
                )
                Tab(
                    selected = selectedSettingsTab == 1,
                    onClick = { selectedSettingsTab = 1 },
                    text = { Text(s.tabQuestions, maxLines = 1, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_questions")
                )
                Tab(
                    selected = selectedSettingsTab == 2,
                    onClick = { selectedSettingsTab = 2 },
                    text = { Text(s.tabExclusions, maxLines = 1, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.AppBlocking, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_exclusions")
                )
                Tab(
                    selected = selectedSettingsTab == 3,
                    onClick = { selectedSettingsTab = 3 },
                    text = { Text(s.tabSecurity, maxLines = 1, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_security")
                )
            }
        }

        // Tab Contents
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (if (isProfileMode) 0 else selectedSettingsTab) {
                0 -> {
                    // TAB 0: Policy Strategy & Allowed Hours
                    if (isProfileMode) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = s.customRulesToggle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (inheritsGlobal) s.inheritGlobalRulesNote else s.customRulesActiveNote,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = !inheritsGlobal,
                                    onCheckedChange = { setInheritGlobal(!it) },
                                    modifier = Modifier.testTag("profile_custom_rules_switch")
                                )
                            }
                        }
                    }

                    if (!isProfileMode || !inheritsGlobal) {
                    // Section 1: Policy Strategy
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = QuizBluePrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = s.strategyTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            StrategyRadioItem(
                                title = s.policyDailyCapTitle,
                                desc = s.policyDailyCapDesc,
                                selected = selectedStrategy == PolicyStrategy.DAILY_CAP,
                                onClick = {
                                    selectedStrategy = PolicyStrategy.DAILY_CAP
                                    saveConfigChanges()
                                }
                            )

                            StrategyRadioItem(
                                title = s.policyInfiniteTitle,
                                desc = s.policyInfiniteDesc,
                                selected = selectedStrategy == PolicyStrategy.INFINITE,
                                onClick = {
                                    selectedStrategy = PolicyStrategy.INFINITE
                                    saveConfigChanges()
                                }
                            )

                            StrategyRadioItem(
                                title = s.policyEscalatingTitle,
                                desc = s.policyEscalatingDesc,
                                selected = selectedStrategy == PolicyStrategy.ESCALATING,
                                onClick = {
                                    selectedStrategy = PolicyStrategy.ESCALATING
                                    saveConfigChanges()
                                }
                            )
                        }
                    }

                    // Section 2: Intervals & Limits
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = s.gateIntervalXTitle.format(gateIntervalX.toInt()),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = s.gateIntervalXDesc.format(gateIntervalX.toInt()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Slider(
                                value = gateIntervalX,
                                onValueChange = {
                                    gateIntervalX = it
                                    saveConfigChanges()
                                },
                                valueRange = 10f..60f,
                                steps = 9
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = s.dailyCapLimitTitle.format(dailyCap.toInt()),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Slider(
                                value = dailyCap,
                                onValueChange = {
                                    dailyCap = it
                                    saveConfigChanges()
                                },
                                valueRange = 30f..240f,
                                steps = 13
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = QuizBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = s.allowedHoursCardTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = s.allowedHoursCardDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick schedule presets
                            Text(
                                text = s.quickPresetsLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val presets = listOf(
                                    Triple(s.presetAllDay, "07:00", "21:00"),
                                    Triple(s.presetAfterSchool, "16:30", "20:30"),
                                    Triple(s.presetEvening, "18:00", "20:30"),
                                    Triple(s.presetWeekend, "08:00", "21:30")
                                )

                                presets.forEach { (label, pStart, pEnd) ->
                                    val isPresetActive = startTime == pStart && endTime == pEnd
                                    Surface(
                                        onClick = {
                                            startTime = pStart
                                            endTime = pEnd
                                            saveConfigChanges(pStart, pEnd)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isPresetActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (isPresetActive) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$label ($pStart - $pEnd)",
                                            fontSize = 12.sp,
                                            fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isPresetActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Visual Interactive Time Cards (Tappable & D-pad friendly)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TimePickerCard(
                                    title = s.startAllowedUpper,
                                    time = startTime,
                                    icon = Icons.Default.WbSunny,
                                    onClick = { showStartTimePicker = true },
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = s.toLabel,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )

                                TimePickerCard(
                                    title = s.endBedtimeUpper,
                                    time = endTime,
                                    icon = Icons.Default.Bedtime,
                                    onClick = { showEndTimePicker = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Validation & Duration Feedback Banner
                            if (timeValidationError != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = s.error,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = timeValidationError,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else if (totalWindowMinutes != null) {
                                val h = totalWindowMinutes / 60
                                val m = totalWindowMinutes % 60
                                val durStr = if (m == 0) "$h ${s.hours}" else "$h ${s.hours} $m ${s.minutes}"

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = s.timeValid,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = s.timeWindowValid.format(durStr, endTime),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }
                }

                1 -> {
                    // TAB 1: Question Bank & URL Import
                    QuestionBankTab(repository = repository)
                }

                2 -> {
                    // TAB 2: App Exclusions
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AppBlocking, contentDescription = null, tint = QuizBluePrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = s.exclusionsCardTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = s.exclusionsCardDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Search Filter
                            OutlinedTextField(
                                value = appSearchQuery,
                                onValueChange = { appSearchQuery = it },
                                placeholder = { Text(s.searchAppsHint) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_apps_input")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            val filteredApps = installedApps.filter {
                                appSearchQuery.isBlank() || it.label.contains(appSearchQuery, ignoreCase = true) || it.packageName.contains(appSearchQuery, ignoreCase = true)
                            }

                            if (filteredApps.isEmpty()) {
                                Text(
                                    text = if (isAppsLoading) s.appsLoading else s.noAppsFound,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            } else {
                                filteredApps.take(20).forEach { app ->
                                    val isOwnApp = app.packageName == context.packageName
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = app.label,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = app.packageName,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Switch(
                                            checked = app.isExcluded || isOwnApp,
                                            enabled = !isOwnApp,
                                            onCheckedChange = { checked ->
                                                coroutineScope.launch {
                                                    if (checked) {
                                                        repository.addExclusion(app.packageName, app.label)
                                                    } else {
                                                        repository.removeExclusion(app.packageName)
                                                    }
                                                    val idx = installedApps.indexOfFirst { it.packageName == app.packageName }
                                                    if (idx != -1) {
                                                        installedApps[idx] = app.copy(isExcluded = checked)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: Language, Security, PIN & System Permissions

                    // Card 0: Language Selection
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_language_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = QuizBluePrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = s.languageSelectorTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = s.languageSelectorDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    val isSelected = currentLang == lang
                                    Surface(
                                        onClick = {
                                            coroutineScope.launch {
                                                repository.setAppLanguage(lang)
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) QuizBluePrimary else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("lang_select_${lang.code}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(text = lang.flag, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = lang.displayName,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = QuizBluePrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = s.pinSecurityTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = s.pinSecurityDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { showPinModalForChange = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("change_pin_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(s.changePinButton)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = s.systemPermissionsTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = s.systemPermissionsDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(s.accessibilityPermissionButton)
                            }

                        }
                    }

                    // Card 3: Legal & Privacy Policy
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = QuizBluePrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = s.legalPrivacyCardTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = s.legalPrivacyCardDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedButton(
                                onClick = { showTosPrivacyScreen = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(s.viewTosPrivacyButton)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Verify old PIN before changing PIN
    if (showPinModalForChange) {
        PinPadModal(
            repository = repository,
            title = s.verifyCurrentPinTitle,
            subtitle = s.verifyCurrentPinSubtitle,
            onPinSuccess = {
                showPinModalForChange = false
                showNewPinDialog = true
            },
            onDismiss = { showPinModalForChange = false }
        )
    }

    // Modal: Enter new PIN with NUMBER keyboard
    if (showNewPinDialog) {
        ChangePinDialog(
            onSavePin = { newPin ->
                coroutineScope.launch {
                    repository.changePin(newPin)
                    showNewPinDialog = false
                }
            },
            onDismiss = { showNewPinDialog = false }
        )
    }

    if (showStartTimePicker) {
        TimePickerModal(
            title = s.startTimePickerTitle,
            initialTime = startTime,
            onTimeSelected = { picked ->
                startTime = picked
                showStartTimePicker = false
                saveConfigChanges(newStart = picked, newEnd = endTime)
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        TimePickerModal(
            title = s.endTimePickerTitle,
            initialTime = endTime,
            onTimeSelected = { picked ->
                endTime = picked
                showEndTimePicker = false
                saveConfigChanges(newStart = startTime, newEnd = picked)
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@Composable
private fun ChangePinDialog(
    onSavePin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalStrings.current
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(20.dp)
                .width(360.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .testTag("change_pin_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = s.setNewPinTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = s.close)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPin = it },
                    label = { Text(s.pinNewInputLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_pin_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) confirmPin = it },
                    label = { Text(s.pinNewConfirmInputLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_new_pin_input")
                )

                errorMsg?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (newPin.length < 6) {
                            errorMsg = s.pinMustBe6Digits
                            return@Button
                        }
                        if (newPin != confirmPin) {
                            errorMsg = s.pinMismatch
                            return@Button
                        }
                        onSavePin(newPin)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_new_pin_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(s.saveNewPin, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TimePickerCard(
    title: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = when {
            isFocused -> BorderStroke(3.dp, TvFocusBorder)
            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        },
        modifier = modifier
            .focusable(interactionSource = interactionSource)
            .testTag("time_picker_card_$title")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = QuizBluePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = time,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = s.tapToEdit,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun StrategyRadioItem(
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = desc, fontSize = 12.sp, color = Slate400)
        }
    }
}
