package com.example.ui.gate

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
import com.example.QuizGateApplication
import com.example.core.policy.BlockReason
import com.example.service.QuizGateAccessibilityService
import com.example.ui.components.PinPadModal
import com.example.ui.theme.QuizAmberSecondary
import com.example.ui.theme.QuizBluePrimary
import com.example.ui.theme.QuizGateTheme
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
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
            QuizGateTheme {
                BackHandler {
                    // Prevent bypass
                }

                val app = application as QuizGateApplication
                val repo = app.repository

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

    override fun onDestroy() {
        super.onDestroy()
        QuizGateAccessibilityService.isGateActive = false
    }
}

@Composable
fun HardBlockScreen(
    reason: String,
    repository: com.example.core.data.QuizGateRepository,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showPinModal by remember { mutableStateOf(false) }

    val isBedtime = reason == BlockReason.OUTSIDE_ALLOWED_HOURS.name

    val title = if (isBedtime) "Đã Đến Giờ Đi Ngủ!" else "Đã Hết Thời Gian Xem Hôm Nay!"
    val subtitle = if (isBedtime) {
        "Thiết bị tạm nghỉ ngơi cho đến 06:00 sáng mai.\nChúc con ngủ thật ngon và mơ đẹp!"
    } else {
        "Con đã đạt giới hạn thời gian xem màn hình trong ngày.\nHãy đi vận động hoặc đọc sách nhé!"
    }

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
                    contentDescription = "Chặn cứng",
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
                        contentDescription = "Mở khóa",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ba/Mẹ ơi (Mở bằng PIN)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (showPinModal) {
            PinPadModal(
                repository = repository,
                title = "Xác nhận mở khóa",
                subtitle = "Nhập mã PIN để bật Parent Mode (4 giờ) hoặc tắt chặn",
                onPinSuccess = {
                    showPinModal = false
                    coroutineScope.launch {
                        // Start Parent Mode for 4 hours
                        repository.startParentMode(4)
                        onDismiss()
                    }
                },
                onDismiss = { showPinModal = false }
            )
        }
    }
}
