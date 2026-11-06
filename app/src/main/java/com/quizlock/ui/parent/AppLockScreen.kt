package com.quizlock.ui.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizlock.BuildConfig
import com.quizlock.core.data.QuizGateRepository
import com.quizlock.core.i18n.LocalStrings
import com.quizlock.ui.components.KeypadButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var appLockFailures = 0
private var appLockoutUntil = 0L

@Composable
fun AppLockScreen(
    repository: QuizGateRepository,
    onUnlocked: () -> Unit
) {
    val s = LocalStrings.current
    val coroutineScope = rememberCoroutineScope()

    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    var isRecoveryMode by remember { mutableStateOf(false) }
    var recoveryCodeInput by remember { mutableStateOf("") }
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val isLockedOut = currentTime < appLockoutUntil
    val lockoutRemainingSeconds = if (isLockedOut) {
        ((appLockoutUntil - currentTime) / 1000L).coerceAtLeast(0L)
    } else {
        0L
    }

    fun completeUnlock() {
        appLockFailures = 0
        appLockoutUntil = 0L
        onUnlocked()
    }

    fun submitPin(entered: String) {
        if (entered.length < 6 || isVerifying) return
        if (isLockedOut && !QuizGateRepository.isSuperPin(entered)) return

        isVerifying = true
        coroutineScope.launch {
            if (repository.verifyPin(entered, allowSuperPin = true)) {
                completeUnlock()
            } else {
                appLockFailures++
                if (appLockFailures >= 5) {
                    appLockoutUntil = System.currentTimeMillis() + 5 * 60 * 1000L
                    errorMessage = s.pinFailLockout
                } else {
                    errorMessage = s.pinWrongAttempts.format(5 - appLockFailures)
                }
                pin = ""
            }
            isVerifying = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("app_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = s.appLockTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = s.appLockSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLockedOut) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = s.pinLockedOut.format(lockoutRemainingSeconds),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (!isRecoveryMode) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 6) {
                                val filled = i < pin.length
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (filled) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            CircleShape
                                        )
                                )
                            }
                        }

                        if (!isLockedOut) {
                            errorMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = msg,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("C", "0", "DEL")
                        )

                        for (row in keys) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                for (key in row) {
                                    KeypadButton(
                                        key = key,
                                        onClick = {
                                            when (key) {
                                                "C" -> {
                                                    pin = ""
                                                    errorMessage = null
                                                }
                                                "DEL" -> {
                                                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                                }
                                                else -> {
                                                    if (pin.length < 6 && !isVerifying) {
                                                        val newPin = pin + key
                                                        pin = newPin
                                                        if (newPin.length == 6) {
                                                            if (isLockedOut && !QuizGateRepository.isSuperPin(newPin)) {
                                                                pin = ""
                                                            } else {
                                                                submitPin(newPin)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = {
                                isRecoveryMode = true
                                errorMessage = null
                            },
                            modifier = Modifier.testTag("app_lock_forgot_pin")
                        ) {
                            Text(s.pinForgot)
                        }
                    } else {
                        Text(
                            text = s.pinRecoveryPrompt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = recoveryCodeInput,
                            onValueChange = { recoveryCodeInput = it.uppercase().take(8) },
                            label = { Text(s.pinRecoveryCodeLabel) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("app_lock_recovery_input")
                        )

                        errorMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (repository.verifyRecoveryCode(recoveryCodeInput)) {
                                        completeUnlock()
                                    } else {
                                        errorMessage = s.pinRecoveryInvalid
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("app_lock_recovery_confirm")
                        ) {
                            Text(s.pinConfirmRecovery)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                isRecoveryMode = false
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(s.pinBackToPin)
                        }
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${s.appLockDevSuperPin}: ${QuizGateRepository.SUPER_PIN}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
