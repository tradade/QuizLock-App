package com.example.ui.parent

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.DayLedgerEntity
import com.example.core.data.ProfileEntity
import com.example.core.data.QuizGateRepository
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalStrings
import com.example.service.QuizGateAccessibilityService
import com.example.ui.gate.QuizGateActivity
import com.example.ui.theme.QuizAmberLight
import com.example.ui.theme.QuizAmberSecondary
import com.example.ui.theme.QuizBlueLight
import com.example.ui.theme.QuizBluePrimary
import com.example.ui.theme.QuizGreenLight
import com.example.ui.theme.QuizGreenSuccess
import com.example.ui.theme.QuizRedError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    repository: QuizGateRepository,
    onNavigateToProfiles: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val s = LocalStrings.current
    val currentLang = LocalAppLanguage.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val activeProfile by repository.getActiveProfileFlow().collectAsState(initial = null)
    var todayLedger by remember { mutableStateOf<DayLedgerEntity?>(null) }
    var parentModeUntil by remember { mutableStateOf<Long?>(null) }
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(activeProfile) {
        activeProfile?.let {
            todayLedger = repository.getTodayLedger(it.id)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            parentModeUntil = repository.getParentModeUntil()
            activeProfile?.let {
                todayLedger = repository.getTodayLedger(it.id)
            }
            delay(5000L)
        }
    }

    val isParentModeActive = (parentModeUntil != null && currentTime < (parentModeUntil ?: 0L))
    val parentModeRemainingMinutes = if (isParentModeActive) {
        ((parentModeUntil!! - currentTime) / 60_000L).coerceAtLeast(0L)
    } else 0L

    val isAccessibilityOn = QuizGateAccessibilityService.isServiceRunning
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Badge Header with Quick Language Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = s.dashboardTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Language Toggle
                Surface(
                    onClick = {
                        coroutineScope.launch {
                            val next = if (currentLang == AppLanguage.VI) AppLanguage.EN else AppLanguage.VI
                            repository.setAppLanguage(next)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.testTag("dashboard_lang_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentLang.flag} ${currentLang.code.uppercase()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAccessibilityOn) QuizGreenLight else QuizAmberLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isAccessibilityOn) QuizGreenSuccess else QuizAmberSecondary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAccessibilityOn) s.systemStatusRunning else s.systemStatusAttention,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAccessibilityOn) Color(0xFF065F46) else Color(0xFF92400E)
                        )
                    }
                }
            }
        }

        // Active Child Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(QuizBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeProfile?.name?.take(1) ?: "K",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = QuizBluePrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = activeProfile?.name ?: s.notSet,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${activeProfile?.age ?: 6} ${s.ageUnit} • Nhóm ${activeProfile?.ageBand ?: "4-6"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateToProfiles,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("switch_profile_button")
                    ) {
                        Text(s.switchChild, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Watch time progress
                val watchedMins = (todayLedger?.watchedSeconds ?: 0L) / 60
                val capMins = activeProfile?.dailyCapMinutes ?: 120
                val progress = if (capMins > 0) (watchedMins.toFloat() / capMins.toFloat()).coerceIn(0f, 1f) else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = s.todayWatchTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (capMins > 0) "$watchedMins ${s.minsAbbr} / $capMins ${s.minsAbbr}" else "$watchedMins ${s.minsAbbr} (${s.strategyInfinite})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (progress >= 1f && capMins > 0) QuizRedError else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = when {
                        progress >= 1f -> QuizRedError
                        progress >= 0.75f -> QuizAmberSecondary
                        else -> QuizBluePrimary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        icon = Icons.Default.Quiz,
                        label = s.gatesPassed,
                        value = "${todayLedger?.quizSolved ?: 0}"
                    )
                    StatItem(
                        icon = Icons.Default.HourglassTop,
                        label = s.gatesPassed,
                        value = "${todayLedger?.gatesPassed ?: 0}"
                    )
                    StatItem(
                        icon = Icons.Default.Star,
                        label = "★",
                        value = "${todayLedger?.quizSolved ?: 0}"
                    )
                }
            }
        }

        // Parent Mode Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isParentModeActive) {
                    if (isDark) Color(0xFF451A03) else Color(0xFFFEF3C7)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            border = BorderStroke(
                1.dp,
                if (isParentModeActive) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Parent Mode",
                        tint = if (isParentModeActive) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = s.parentModeTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isParentModeActive) {
                            if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isParentModeActive) {
                        "${s.parentModeActiveDesc} (${parentModeRemainingMinutes} ${s.minutes})"
                    } else {
                        s.parentModeInactiveDesc
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isParentModeActive) {
                        if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isParentModeActive) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.stopParentMode()
                                parentModeUntil = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = QuizRedError),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stop_parent_mode_button")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(s.stopParentMode)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 4, 8).forEach { hours ->
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.startParentMode(hours)
                                        parentModeUntil = repository.getParentModeUntil()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("start_parent_mode_${hours}h")
                            ) {
                                Text("${hours}${s.hoursAbbr}", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quick Test Quiz Gate preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.testGateButton,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = s.onboardingBullet1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        val intent = Intent(context, QuizGateActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("preview_quiz_gate_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(s.apply)
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = QuizBluePrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
