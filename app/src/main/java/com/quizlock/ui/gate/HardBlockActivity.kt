package com.quizlock.ui.gate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizlock.QuizGateApplication
import com.quizlock.core.i18n.AppLanguage
import com.quizlock.core.i18n.AppStrings
import com.quizlock.core.i18n.LocalAppLanguage
import com.quizlock.core.i18n.LocalStrings
import com.quizlock.core.policy.BlockReason
import com.quizlock.service.QuizGateAccessibilityService
import com.quizlock.ui.components.PinPadModal
import com.quizlock.ui.theme.QuizAmberSecondary
import com.quizlock.ui.theme.QuizBluePrimary
import com.quizlock.ui.theme.QuizGateTheme
import com.quizlock.ui.theme.Slate400
import com.quizlock.ui.theme.Slate800
import com.quizlock.ui.theme.Slate900
import kotlinx.coroutines.launch

class HardBlockActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCK_REASON = "extra_block_reason"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QuizGateAccessibilityService.isGateActive = true

        val reasonStr = intent.getStringExtra(EXTRA_BLOCK_REASON) ?: BlockReason.DAILY_CAP_REACHED.name

        setContent {
            val app = application as QuizGateApplication
            val repo = app.repository
            val appLanguage by repo.appLanguageFlow.collectAsState(initial = AppLanguage.VI)

            CompositionLocalProvider(
                LocalStrings provides AppStrings.of(appLanguage),
                LocalAppLanguage provides appLanguage
            ) {
                QuizGateTheme {
                    BackHandler {
                        // Prevent bypass
                    }

                    HardBlockScreen(
                        reason = reasonStr,
                        repository = repo,
                        onDismiss = {
                            QuizGateAccessibilityService.isGateActive = false
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        QuizGateAccessibilityService.isGateActive = false
        QuizGateAccessibilityService.requestEvaluation()
    }
}

@Composable
fun HardBlockScreen(
    reason: String,
    repository: com.quizlock.core.data.QuizGateRepository,
    onDismiss: () -> Unit
) {
    val s = LocalStrings.current
    val coroutineScope = rememberCoroutineScope()
    var showPinModal by remember { mutableStateOf(false) }

    val isBedtime = reason == BlockReason.OUTSIDE_ALLOWED_HOURS.name

    val title = if (isBedtime) s.hardBlockBedtimeTitle else s.hardBlockDailyCapTitle
    val subtitle = if (isBedtime) s.hardBlockBedtimeSubtitle else s.hardBlockDailyCapSubtitle

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Slate900, Color(0xFF020617), Slate900)
                )
            )
            .testTag("hard_block_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 520.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(36.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isBedtime) Icons.Default.Bedtime else Icons.Default.HourglassBottom,
                    contentDescription = s.hardBlockDesc,
                    tint = QuizAmberSecondary,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Slate400,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { showPinModal = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("parent_unlock_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QuizBluePrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = s.unlockDesc,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = s.hardBlockParentButton,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (showPinModal) {
            PinPadModal(
                repository = repository,
                title = s.hardBlockPinTitle,
                subtitle = s.hardBlockPinSubtitle,
                onPinSuccess = {
                    showPinModal = false
                    coroutineScope.launch {
                        // Start Parent Mode for 4 hours
                        repository.startParentMode(4)
                        QuizGateAccessibilityService.requestEvaluation()
                        onDismiss()
                    }
                },
                onDismiss = { showPinModal = false }
            )
        }
    }
}
