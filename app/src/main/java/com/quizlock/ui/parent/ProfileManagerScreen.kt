package com.quizlock.ui.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quizlock.core.data.ProfileEntity
import com.quizlock.core.data.QuizGateRepository
import com.quizlock.core.data.hasCustomPolicy
import com.quizlock.core.i18n.LocalStrings
import com.quizlock.core.i18n.ageBandShortLabel
import com.quizlock.core.policy.PolicyStrategy
import com.quizlock.service.QuizGateAccessibilityService
import com.quizlock.ui.components.TimePickerModal
import com.quizlock.ui.theme.QuizAmberSecondary
import com.quizlock.ui.theme.QuizBluePrimary
import com.quizlock.ui.theme.QuizGreenLight
import com.quizlock.ui.theme.QuizGreenSuccess
import com.quizlock.ui.theme.QuizRedError
import kotlinx.coroutines.launch

@Composable
fun ProfileManagerScreen(
    repository: QuizGateRepository,
    onBack: () -> Unit
) {
    val s = LocalStrings.current
    val coroutineScope = rememberCoroutineScope()
    val profiles by repository.allProfilesFlow.collectAsState(initial = emptyList())
    val activeProfile by repository.getActiveProfileFlow().collectAsState(initial = null)

    var showAddDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<ProfileEntity?>(null) }
    var profileToDelete by remember { mutableStateOf<ProfileEntity?>(null) }
    var settingsProfile by remember { mutableStateOf<ProfileEntity?>(null) }

    settingsProfile?.let { profile ->
        SettingsScreen(
            repository = repository,
            onBack = { settingsProfile = null },
            profile = profile
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_profile_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = s.addProfileFab)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("profile_manager_screen")
        ) {
            Text(
                text = s.profilesTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = s.profilesSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(profiles) { profile ->
                    val isActive = profile.id == activeProfile?.id
                    ProfileCard(
                        profile = profile,
                        isActive = isActive,
                        onClick = { settingsProfile = profile },
                        onEdit = { editingProfile = profile },
                        onSelectActive = {
                            coroutineScope.launch {
                                repository.setActiveProfileId(profile.id)
                                QuizGateAccessibilityService.requestEvaluation()
                            }
                        }
                    )
                }
            }
        }

        // Add Profile Dialog
        if (showAddDialog) {
            AddProfileDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, age, cap, interval, strat, start, end ->
                    coroutineScope.launch {
                        val id = repository.addProfile(
                            name = name,
                            age = age,
                            avatarId = 1,
                            dailyCap = cap,
                            gateIntervalMinutes = interval,
                            strategy = strat,
                            allowedStartTime = start,
                            allowedEndTime = end
                        )
                        repository.setActiveProfileId(id)
                        QuizGateAccessibilityService.requestEvaluation()
                        showAddDialog = false
                    }
                }
            )
        }

        // Edit Profile Custom Settings Dialog
        editingProfile?.let { profile ->
            CustomProfileSettingsDialog(
                profile = profile,
                isActive = profile.id == activeProfile?.id,
                canDelete = profiles.size > 1,
                onDismiss = { editingProfile = null },
                onSave = { updated ->
                    coroutineScope.launch {
                        repository.updateProfile(updated)
                        QuizGateAccessibilityService.requestEvaluation()
                        editingProfile = null
                    }
                },
                onSetActive = {
                    coroutineScope.launch {
                        repository.setActiveProfileId(profile.id)
                    }
                },
                onDelete = {
                    profileToDelete = profile
                    editingProfile = null
                }
            )
        }

        // Confirm Delete Dialog
        profileToDelete?.let { profile ->
            AlertDialog(
                onDismissRequest = { profileToDelete = null },
                title = { Text(s.deleteProfileTitle) },
                text = { Text("${s.deleteProfileConfirmMsg} (${profile.name})") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.deleteProfile(profile.id)
                                QuizGateAccessibilityService.requestEvaluation()
                                profileToDelete = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = QuizRedError)
                    ) {
                        Text(s.delete, color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { profileToDelete = null }) {
                        Text(s.cancel)
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileCard(
    profile: ProfileEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onSelectActive: () -> Unit
) {
    val s = LocalStrings.current
    val isDark = isSystemInDarkTheme()

    val (bandColor, _) = when (profile.ageBand) {
        "4-6" -> Pair(Color(0xFFEC4899), Icons.Default.ChildCare)
        "7-9" -> Pair(QuizBluePrimary, Icons.Default.Face)
        else -> Pair(QuizAmberSecondary, Icons.Default.School)
    }

    val hasCustomRules = profile.hasCustomPolicy

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("profile_card_${profile.id}")
            .then(
                if (isActive) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(20.dp)
                    )
                } else {
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(20.dp)
                    )
                }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFEFF6FF)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(if (isActive) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Content: Avatar + Information
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(bandColor.copy(alpha = if (isDark) 0.25f else 0.15f))
                        .border(1.5.dp, bandColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.name.trim().take(1).uppercase(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else bandColor
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hasCustomRules) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = s.customBadge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "${profile.age} ${s.ageUnit} • ${s.ageGroupLabel} ${s.ageBandShortLabel(profile.ageBand)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = bandColor
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val intervalStr = profile.gateIntervalMinutes?.let { " • ${s.gateShortLabel} $it${s.minsAbbr}" } ?: ""
                    Text(
                        text = "${s.dailyCapShortLabel}: ${profile.dailyCapMinutes ?: 120} ${s.minutes}$intervalStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right Action: edit profile, then active badge or select button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_profile_${profile.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = s.editProfileTitle,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier.wrapContentWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (isActive) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDark) Color(0xFF064E3B) else QuizGreenLight,
                            border = BorderStroke(1.dp, QuizGreenSuccess)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = QuizGreenSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = s.watching,
                                    color = if (isDark) Color(0xFFA7F3D0) else Color(0xFF065F46),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onSelectActive,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = s.select,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomProfileSettingsDialog(
    profile: ProfileEntity,
    isActive: Boolean,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onSave: (ProfileEntity) -> Unit,
    onSetActive: () -> Unit,
    onDelete: () -> Unit
) {
    val s = LocalStrings.current
    var name by remember { mutableStateOf(profile.name) }
    var age by remember { mutableIntStateOf(profile.age) }
    var avatarId by remember { mutableIntStateOf(profile.avatarId) }

    // Custom Rules State
    var hasCustomRules by remember {
        mutableStateOf(profile.strategy != null || profile.gateIntervalMinutes != null || profile.allowedStartTime != null)
    }
    var strategy by remember {
        mutableStateOf(
            profile.strategy?.let {
                try { PolicyStrategy.valueOf(it) } catch (_: Exception) { PolicyStrategy.DAILY_CAP }
            } ?: PolicyStrategy.DAILY_CAP
        )
    }
    var gateIntervalMinutes by remember { mutableIntStateOf(profile.gateIntervalMinutes ?: 30) }
    var dailyCapMinutes by remember { mutableIntStateOf(profile.dailyCapMinutes ?: 120) }
    var startTime by remember { mutableStateOf(profile.allowedStartTime ?: "06:00") }
    var endTime by remember { mutableStateOf(profile.allowedEndTime ?: "21:00") }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = s.editProfileTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${profile.name} • ${profile.age} ${s.ageUnit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isActive) {
                        OutlinedButton(
                            onClick = onSetActive,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(s.setAsActiveChild, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 1. Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.childNameLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Age Selection
                Text(
                    text = "${s.childAgeLabel}: $age ${s.ageUnit} (${when {
                        age <= 6 -> s.ageBand4to6
                        age <= 9 -> s.ageBand7to9
                        else -> s.ageBand10to12
                    }})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(5, 7, 10).forEach { a ->
                        val isSelected = age == a
                        Surface(
                            onClick = { age = a },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) QuizBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "$a ${s.ageUnit}",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // 3. Custom Rules Section (Per-Profile Override)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasCustomRules) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (hasCustomRules) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = if (hasCustomRules) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = s.customRulesTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (hasCustomRules) s.customRulesActiveNote else s.inheritGlobalRulesNote,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = hasCustomRules,
                                onCheckedChange = { hasCustomRules = it },
                                modifier = Modifier.testTag("custom_rules_switch")
                            )
                        }

                        // Detailed Custom Controls when enabled
                        if (hasCustomRules) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(14.dp))

                            // Strategy Selection
                            Text(
                                text = s.strategyTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            CustomStrategyCard(
                                title = s.strategyDailyCap,
                                desc = s.strategyDailyCapDesc,
                                isSelected = strategy == PolicyStrategy.DAILY_CAP,
                                onClick = { strategy = PolicyStrategy.DAILY_CAP }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomStrategyCard(
                                title = s.strategyInfinite,
                                desc = s.strategyInfiniteDesc,
                                isSelected = strategy == PolicyStrategy.INFINITE,
                                onClick = { strategy = PolicyStrategy.INFINITE }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomStrategyCard(
                                title = s.strategyEscalating,
                                desc = s.strategyEscalatingDesc,
                                isSelected = strategy == PolicyStrategy.ESCALATING,
                                onClick = { strategy = PolicyStrategy.ESCALATING }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Gate Interval
                            Text(
                                text = "${s.gateIntervalTitle}: $gateIntervalMinutes ${s.minutes}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(15, 30, 45, 60).forEach { mins ->
                                    val isSel = gateIntervalMinutes == mins
                                    Surface(
                                        onClick = { gateIntervalMinutes = mins },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) QuizBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Text(
                                            text = "$mins ${s.minsAbbr}",
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            Slider(
                                value = gateIntervalMinutes.toFloat(),
                                onValueChange = { gateIntervalMinutes = it.toInt() },
                                valueRange = 10f..90f,
                                steps = 15,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Daily Cap (if DAILY_CAP strategy)
                            if (strategy == PolicyStrategy.DAILY_CAP) {
                                Spacer(modifier = Modifier.height(12.dp))
                                val h = dailyCapMinutes / 60
                                val m = dailyCapMinutes % 60
                                val capLabel = if (m > 0) "$h${s.hoursAbbr} $m${s.minsAbbr}" else "$h${s.hoursAbbr}"
                                Text(
                                    text = "${s.dailyCapLabel}: $capLabel ($dailyCapMinutes ${s.minutes})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf(60, 90, 120, 180).forEach { cap ->
                                        val isSel = dailyCapMinutes == cap
                                        Surface(
                                            onClick = { dailyCapMinutes = cap },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSel) QuizBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                text = "${cap / 60}${s.hoursAbbr}",
                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                                Slider(
                                    value = dailyCapMinutes.toFloat(),
                                    onValueChange = { dailyCapMinutes = it.toInt() },
                                    valueRange = 30f..300f,
                                    steps = 17,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Allowed Hours
                            Text(
                                text = s.allowedHoursTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    onClick = { showStartTimePicker = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(s.startTimeLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(startTime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }

                                Surface(
                                    onClick = { showEndTimePicker = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(s.endTimeLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(endTime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (canDelete) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = QuizRedError)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.delete)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Row {
                        OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                            Text(s.cancel)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val band = when {
                                        age <= 6 -> "4-6"
                                        age <= 9 -> "7-9"
                                        else -> "10-12"
                                    }
                                    val updated = profile.copy(
                                        name = name.trim(),
                                        age = age,
                                        ageBand = band,
                                        avatarId = avatarId,
                                        dailyCapMinutes = if (hasCustomRules && strategy == PolicyStrategy.DAILY_CAP) dailyCapMinutes else if (hasCustomRules) 0 else null,
                                        gateIntervalMinutes = if (hasCustomRules) gateIntervalMinutes else null,
                                        strategy = if (hasCustomRules) strategy.name else null,
                                        allowedStartTime = if (hasCustomRules) startTime else null,
                                        allowedEndTime = if (hasCustomRules) endTime else null
                                    )
                                    onSave(updated)
                                }
                            },
                            enabled = name.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(s.save)
                        }
                    }
                }
            }
        }
    }

    if (showStartTimePicker) {
        TimePickerModal(
            title = s.startTimeLabel,
            initialTime = startTime,
            onDismiss = { showStartTimePicker = false },
            onTimeSelected = {
                startTime = it
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerModal(
            title = s.endTimeLabel,
            initialTime = endTime,
            onDismiss = { showEndTimePicker = false },
            onTimeSelected = {
                endTime = it
                showEndTimePicker = false
            }
        )
    }
}

@Composable
private fun CustomStrategyCard(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isSelected) QuizBluePrimary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AddProfileDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, age: Int, cap: Int?, interval: Int?, strategy: String?, start: String?, end: String?) -> Unit
) {
    val s = LocalStrings.current
    var name by remember { mutableStateOf("") }
    var age by remember { mutableIntStateOf(6) }
    var capMinutes by remember { mutableStateOf("120") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = s.addProfileTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.childNamePlaceholder) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${s.childAgeLabel}: $age ${s.ageUnit} (${when {
                        age <= 6 -> s.ageBand4to6
                        age <= 9 -> s.ageBand7to9
                        else -> s.ageBand10to12
                    }})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(4, 5, 7, 9, 11).forEach { a ->
                        val isSelected = age == a
                        Surface(
                            onClick = { age = a },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "$a ${s.ageUnit}",
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = capMinutes,
                    onValueChange = { if (it.all { c -> c.isDigit() }) capMinutes = it },
                    label = { Text("${s.dailyCapLabel} (${s.minutes})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(s.cancel)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAdd(name.trim(), age, capMinutes.toIntOrNull() ?: 120, null, null, null, null)
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(s.save)
                    }
                }
            }
        }
    }
}
