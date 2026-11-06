package com.quizlock.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quizlock.core.data.QuizGateRepository
import com.quizlock.core.i18n.LocalStrings
import com.quizlock.ui.theme.QuizRedError
import com.quizlock.ui.theme.Slate200
import com.quizlock.ui.theme.Slate400
import com.quizlock.ui.theme.Slate700
import com.quizlock.ui.theme.Slate800
import com.quizlock.ui.theme.TvFocusBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var consecutiveFailures = 0
private var lockoutUntilTimestamp = 0L

@Composable
fun PinPadModal(
    repository: QuizGateRepository,
    title: String? = null,
    subtitle: String? = null,
    onPinSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalStrings.current
    val effectiveTitle = title ?: s.pinPadDefaultTitle
    val effectiveSubtitle = subtitle ?: s.pinPadDefaultSubtitle
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

    val isLockedOut = currentTime < lockoutUntilTimestamp
    val lockoutRemainingSeconds = if (isLockedOut) ((lockoutUntilTimestamp - currentTime) / 1000L).coerceAtLeast(0L) else 0L

    fun checkPin(entered: String) {
        if (entered.length < 6 || isLockedOut || isVerifying) return

        isVerifying = true
        coroutineScope.launch {
            val verified = repository.verifyPin(entered)
            if (verified) {
                consecutiveFailures = 0
                lockoutUntilTimestamp = 0L
                onPinSuccess()
            } else {
                consecutiveFailures++
                if (consecutiveFailures >= 5) {
                    lockoutUntilTimestamp = System.currentTimeMillis() + 5 * 60 * 1000L
                    errorMessage = s.pinFailLockout
                } else {
                    errorMessage = s.pinWrongAttempts.format(5 - consecutiveFailures)
                }
                pin = ""
            }
            isVerifying = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .width(360.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .testTag("pin_pad_modal"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = s.pinLockIconDesc,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRecoveryMode) s.pinRecoveryTitle else effectiveTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("pin_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.close)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isLockedOut) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = s.pinLockedOut.format(lockoutRemainingSeconds),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else if (!isRecoveryMode) {
                    Text(
                        text = effectiveSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6 Dots for PIN
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
                                        else Slate200
                                    )
                                    .border(1.dp, Slate400, CircleShape)
                            )
                        }
                    }

                    errorMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            color = QuizRedError,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3x4 Keypad Grid
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
                                                if (pin.length < 6) {
                                                    val newPin = pin + key
                                                    pin = newPin
                                                    if (newPin.length == 6) {
                                                        checkPin(newPin)
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
                        onClick = { isRecoveryMode = true },
                        modifier = Modifier.testTag("forgot_pin_button")
                    ) {
                        Text(s.pinForgot)
                    }
                } else {
                    // Recovery Code Flow
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
                            .testTag("recovery_code_input")
                    )

                    errorMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            color = QuizRedError,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val ok = repository.verifyRecoveryCode(recoveryCodeInput)
                                if (ok) {
                                    consecutiveFailures = 0
                                    lockoutUntilTimestamp = 0L
                                    onPinSuccess()
                                } else {
                                    errorMessage = s.pinRecoveryInvalid
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_recovery_button")
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
    }
}

@Composable
internal fun KeypadButton(
    key: String,
    onClick: () -> Unit
) {
    val s = LocalStrings.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = if (key == "C" || key == "DEL") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .size(64.dp)
            .focusable(interactionSource = interactionSource)
            .then(
                if (isFocused) Modifier.border(3.dp, TvFocusBorder, CircleShape)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            .testTag("pin_key_$key")
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (key == "DEL") {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = s.deleteDigitDesc,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = key,
                    fontSize = if (key == "C") 16.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
