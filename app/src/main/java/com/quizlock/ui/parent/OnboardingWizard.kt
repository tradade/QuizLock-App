package com.quizlock.ui.parent

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.quizlock.core.data.QuizGateRepository
import com.quizlock.core.i18n.AppLanguage
import com.quizlock.core.i18n.LocalAppLanguage
import com.quizlock.core.i18n.LocalStrings
import com.quizlock.core.policy.PolicyStrategy
import com.quizlock.service.WatchdogForegroundService
import com.quizlock.service.WatchdogHealthChecker
import com.quizlock.ui.theme.QuizAmberSecondary
import com.quizlock.ui.theme.QuizBlueLight
import com.quizlock.ui.theme.QuizBluePrimary
import com.quizlock.ui.theme.QuizGreenLight
import com.quizlock.ui.theme.QuizGreenSuccess
import com.quizlock.ui.theme.QuizRedError
import com.quizlock.ui.theme.Slate200
import com.quizlock.ui.theme.Slate400
import com.quizlock.ui.theme.Slate700
import com.quizlock.ui.theme.Slate800
import kotlinx.coroutines.launch

@Composable
fun OnboardingWizard(
    repository: QuizGateRepository,
    onCompleted: () -> Unit
) {
    val s = LocalStrings.current
    val currentLang = LocalAppLanguage.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableIntStateOf(1) }

    // TOS / Privacy screen
    var showTosPrivacy by remember { mutableStateOf(false) }

    // Step 3 state: PIN & Recovery Code
    var pinInput by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var generatedRecoveryCode by remember { mutableStateOf("") }
    var hasConfirmedRecoveryCode by remember { mutableStateOf(false) }
    var pinErrorMessage by remember { mutableStateOf<String?>(null) }

    // Step 4 state: Child Profile, Policy Strategy & Gate Intervals
    var childName by remember { mutableStateOf("") }
    var childAge by remember { mutableIntStateOf(7) }
    var selectedAvatar by remember { mutableIntStateOf(1) }
    var selectedStrategy by remember { mutableStateOf(PolicyStrategy.DAILY_CAP) }
    var selectedGateInterval by remember { mutableIntStateOf(30) }
    var selectedDailyCap by remember { mutableIntStateOf(120) }

    if (showTosPrivacy) {
        TosPrivacyScreen(onBack = { showTosPrivacy = false })
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Step Progress Indicator
            StepProgressBar(currentStep = currentStep, totalSteps = 5)

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .widthIn(max = 580.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                when (currentStep) {
                    1 -> Step1Welcome(
                        currentLang = currentLang,
                        onLanguageChange = { lang ->
                            coroutineScope.launch {
                                repository.setAppLanguage(lang)
                            }
                        },
                        onNext = { currentStep = 2 },
                        onOpenTos = { showTosPrivacy = true }
                    )
                    2 -> Step2Permissions(
                        onNext = { currentStep = 3 }
                    )
                    3 -> Step3PinSetup(
                        pin = pinInput,
                        onPinChange = { pinInput = it },
                        pinConfirm = pinConfirm,
                        onPinConfirmChange = { pinConfirm = it },
                        generatedRecoveryCode = generatedRecoveryCode,
                        hasConfirmed = hasConfirmedRecoveryCode,
                        onConfirmedChange = { hasConfirmedRecoveryCode = it },
                        errorMessage = pinErrorMessage,
                        onGenerateAndSave = {
                            if (pinInput.length < 6) {
                                pinErrorMessage = s.pinMinLengthError
                                return@Step3PinSetup
                            }
                            if (pinInput != pinConfirm) {
                                pinErrorMessage = s.pinMismatch
                                return@Step3PinSetup
                            }
                            coroutineScope.launch {
                                val code = repository.setupPin(pinInput)
                                generatedRecoveryCode = code
                                pinErrorMessage = null
                            }
                        },
                        onNext = {
                            if (generatedRecoveryCode.isNotEmpty() && hasConfirmedRecoveryCode) {
                                currentStep = 4
                            }
                        }
                    )
                    4 -> Step4ChildProfile(
                        name = childName,
                        onNameChange = { childName = it },
                        age = childAge,
                        onAgeChange = { childAge = it },
                        avatarId = selectedAvatar,
                        onAvatarChange = { selectedAvatar = it },
                        strategy = selectedStrategy,
                        onStrategyChange = { selectedStrategy = it },
                        gateIntervalMinutes = selectedGateInterval,
                        onGateIntervalChange = { selectedGateInterval = it },
                        dailyCapMinutes = selectedDailyCap,
                        onDailyCapChange = { selectedDailyCap = it },
                        onNext = {
                            if (childName.isNotBlank()) {
                                coroutineScope.launch {
                                    val id = repository.addProfile(
                                        name = childName.trim(),
                                        age = childAge,
                                        avatarId = selectedAvatar,
                                        dailyCap = if (selectedStrategy == PolicyStrategy.DAILY_CAP) selectedDailyCap else 0
                                    )
                                    repository.setActiveProfileId(id)

                                    val currentConfig = repository.getPolicyConfig()
                                    repository.savePolicyConfig(
                                        currentConfig.copy(
                                            strategy = selectedStrategy,
                                            gateIntervalMinutes = selectedGateInterval,
                                            dailyCapMinutes = selectedDailyCap
                                        )
                                    )

                                    currentStep = 5
                                }
                            }
                        }
                    )
                    5 -> Step5Finalize(
                        onFinish = {
                            WatchdogForegroundService.start(context)
                            onCompleted()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepProgressBar(currentStep: Int, totalSteps: Int) {
    val s = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            val isActive = i == currentStep
            val isDone = i < currentStep

            val inactiveBg = MaterialTheme.colorScheme.surfaceVariant
            val inactiveText = MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDone -> QuizGreenSuccess
                            isActive -> QuizBluePrimary
                            else -> inactiveBg
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = s.complete,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = i.toString(),
                        color = if (isActive) Color.White else inactiveText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (i < totalSteps) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(3.dp)
                        .background(if (isDone) QuizGreenSuccess else MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}

@Composable
private fun Step1Welcome(
    currentLang: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNext: () -> Unit,
    onOpenTos: () -> Unit
) {
    val s = LocalStrings.current

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language selector row at the very top of onboarding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                AppLanguage.values().forEach { lang ->
                    val isSelected = currentLang == lang
                    Surface(
                        onClick = { onLanguageChange(lang) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) QuizBluePrimary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .testTag("onboarding_lang_${lang.code}")
                    ) {
                        Text(
                            text = "${lang.flag} ${lang.displayName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = QuizBluePrimary,
                modifier = Modifier.size(68.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = s.onboardingWelcomeTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = s.onboardingWelcomeSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            FeatureBullet(s.onboardingBullet1)
            FeatureBullet(s.onboardingBullet2)
            FeatureBullet(s.onboardingBullet3)

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_start_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(s.onboardingStartButton, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onOpenTos,
                modifier = Modifier.testTag("onboarding_open_tos_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PrivacyTip,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = s.tosTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun FeatureBullet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = QuizGreenSuccess,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Step2Permissions(onNext: () -> Unit) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionRefresh by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permissionRefresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val health = remember(permissionRefresh) { WatchdogHealthChecker.check(context) }
    val isAccessibilityGranted = health.accessibilityEnabled
    val isBatteryOptimizationIgnored = health.batteryOptimizationIgnored
    val isNotificationsGranted = health.notificationsEnabled

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        permissionRefresh++
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = s.onboardingPermissionsTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = s.onboardingPermissionsSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            PermissionCard(
                title = s.onboardingPermissionAccessibilityTitle,
                desc = s.onboardingPermissionAccessibilityDesc,
                isGranted = isAccessibilityGranted,
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                title = s.onboardingPermissionBatteryTitle,
                desc = s.onboardingPermissionBatteryDesc,
                isGranted = isBatteryOptimizationIgnored,
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        }
                    }
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Spacer(modifier = Modifier.height(12.dp))

                PermissionCard(
                    title = s.onboardingPermissionNotificationsTitle,
                    desc = s.onboardingPermissionNotificationsDesc,
                    isGranted = isNotificationsGranted,
                    onClick = {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("permissions_continue_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(s.continueText, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    desc: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    val s = LocalStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (isGranted) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isGranted) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    fontSize = 13.sp,
                    color = if (isGranted) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isGranted) QuizGreenSuccess else QuizBluePrimary
            ) {
                Text(
                    text = if (isGranted) s.granted else s.grantPermission,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun Step3PinSetup(
    pin: String,
    onPinChange: (String) -> Unit,
    pinConfirm: String,
    onPinConfirmChange: (String) -> Unit,
    generatedRecoveryCode: String,
    hasConfirmed: Boolean,
    onConfirmedChange: (Boolean) -> Unit,
    errorMessage: String?,
    onGenerateAndSave: () -> Unit,
    onNext: () -> Unit
) {
    val s = LocalStrings.current
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = s.onboardingPinTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = s.onboardingPinSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (generatedRecoveryCode.isEmpty()) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onPinChange(it) },
                    label = { Text(s.onboardingPinLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_pin_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pinConfirm,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onPinConfirmChange(it) },
                    label = { Text(s.onboardingConfirmPinLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_pin_confirm")
                )

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = QuizRedError, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onGenerateAndSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_pin_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(s.onboardingGeneratePinButton, fontSize = 16.sp)
                }
            } else {
                // Show recovery code
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = s.onboardingRecoveryCodeUpper,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = generatedRecoveryCode,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = s.onboardingRecoveryCodeNote,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasConfirmed,
                        onCheckedChange = onConfirmedChange,
                        modifier = Modifier.testTag("confirm_recovery_checkbox")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = s.onboardingRecoveryCodeNotice,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNext,
                    enabled = hasConfirmed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("pin_continue_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(s.continueText, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun Step4ChildProfile(
    name: String,
    onNameChange: (String) -> Unit,
    age: Int,
    onAgeChange: (Int) -> Unit,
    avatarId: Int,
    onAvatarChange: (Int) -> Unit,
    strategy: PolicyStrategy,
    onStrategyChange: (PolicyStrategy) -> Unit,
    gateIntervalMinutes: Int,
    onGateIntervalChange: (Int) -> Unit,
    dailyCapMinutes: Int,
    onDailyCapChange: (Int) -> Unit,
    onNext: () -> Unit
) {
    val s = LocalStrings.current
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = s.onboardingProfileTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = s.onboardingProfileSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Tên của bé
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(s.onboardingChildNameLabel) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("child_name_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Độ tuổi
            Text(
                text = "${s.childAgeLabel}: $age ${s.ageUnit} (${when {
                    age <= 6 -> s.ageBand4to6
                    age <= 9 -> s.ageBand7to9
                    else -> s.ageBand10to12
                }})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(5, 7, 10).forEach { sampleAge ->
                    val isSelected = age == sampleAge
                    Surface(
                        onClick = { onAgeChange(sampleAge) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) QuizBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = "$sampleAge ${s.ageUnit}",
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(20.dp))

            // 3. CHIẾN LƯỢC KIỂM SOÁT
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = QuizBluePrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = s.strategyTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Option 1: DAILY_CAP
            StrategyOptionCard(
                title = s.strategyDailyCap,
                desc = s.strategyDailyCapDesc,
                isSelected = strategy == PolicyStrategy.DAILY_CAP,
                onClick = { onStrategyChange(PolicyStrategy.DAILY_CAP) },
                testTag = "strategy_daily_cap"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 2: INFINITE
            StrategyOptionCard(
                title = s.strategyInfinite,
                desc = s.strategyInfiniteDesc,
                isSelected = strategy == PolicyStrategy.INFINITE,
                onClick = { onStrategyChange(PolicyStrategy.INFINITE) },
                testTag = "strategy_infinite"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 3: ESCALATING
            StrategyOptionCard(
                title = s.strategyEscalating,
                desc = s.strategyEscalatingDesc,
                isSelected = strategy == PolicyStrategy.ESCALATING,
                onClick = { onStrategyChange(PolicyStrategy.ESCALATING) },
                testTag = "strategy_escalating"
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(20.dp))

            // 4. THỜI GIAN CỔNG (GATE INTERVAL)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = QuizAmberSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = s.gateIntervalTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = s.onboardingPauseEvery.format(gateIntervalMinutes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Interval chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(15, 30, 45, 60).forEach { mins ->
                    val isSelected = gateIntervalMinutes == mins
                    Surface(
                        onClick = { onGateIntervalChange(mins) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) QuizBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "$mins ${s.minutes}",
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Slider(
                value = gateIntervalMinutes.toFloat(),
                onValueChange = { onGateIntervalChange(it.toInt()) },
                valueRange = 10f..90f,
                steps = 15,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_interval_slider")
            )

            // DAILY CAP SECTION (if DAILY_CAP strategy)
            if (strategy == PolicyStrategy.DAILY_CAP) {
                Spacer(modifier = Modifier.height(16.dp))

                val hours = dailyCapMinutes / 60
                val mins = dailyCapMinutes % 60
                val displayCap = if (mins > 0) "$hours${s.hoursAbbr} $mins${s.minsAbbr}" else "$hours${s.hoursAbbr}"

                Text(
                    text = s.onboardingDailyCapLabel.format(displayCap, dailyCapMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Daily Cap chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(60, 90, 120, 180).forEach { cap ->
                        val isSelected = dailyCapMinutes == cap
                        val label = when (cap) {
                            60 -> "1${s.hoursAbbr}"
                            90 -> "1.5${s.hoursAbbr}"
                            120 -> "2${s.hoursAbbr}"
                            180 -> "3${s.hoursAbbr}"
                            else -> "$cap${s.minsAbbr}"
                        }
                        Surface(
                            onClick = { onDailyCapChange(cap) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) QuizBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Slider(
                    value = dailyCapMinutes.toFloat(),
                    onValueChange = { onDailyCapChange(it.toInt()) },
                    valueRange = 30f..300f,
                    steps = 17,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_daily_cap_slider")
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onNext,
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("child_continue_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(s.continueText, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun StrategyOptionCard(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) QuizBluePrimary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Step5Finalize(onFinish: () -> Unit) {
    val s = LocalStrings.current
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = s.complete,
                tint = QuizGreenSuccess,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = s.onboardingReadyTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = s.onboardingReadySubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("finish_onboarding_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(s.onboardingFinishButton, fontSize = 16.sp)
            }
        }
    }
}
