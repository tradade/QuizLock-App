package com.example.ui.parent

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.QuizGateRepository
import com.example.ui.theme.QuizAmberSecondary
import com.example.ui.theme.QuizBluePrimary
import com.example.ui.theme.QuizGreenSuccess
import kotlinx.coroutines.launch

@Composable
fun QuestionBankTab(
    repository: QuizGateRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var totalCount by remember { mutableIntStateOf(0) }
    var count4to6 by remember { mutableIntStateOf(0) }
    var count7to9 by remember { mutableIntStateOf(0) }
    var count10to12 by remember { mutableIntStateOf(0) }

    var urlInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isStatusSuccess by remember { mutableStateOf(false) }
    var showJsonSample by remember { mutableStateOf(false) }

    fun refreshStats() {
        coroutineScope.launch {
            totalCount = repository.getQuestionsCount()
            count4to6 = repository.getQuestionCountByAgeBand("4-6")
            count7to9 = repository.getQuestionCountByAgeBand("7-9")
            count10to12 = repository.getQuestionCountByAgeBand("10-12")
        }
    }

    LaunchedEffect(Unit) {
        refreshStats()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Overview & Statistics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = QuizBluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ngân hàng câu hỏi hiện tại",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$totalCount câu",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Số lượng câu hỏi được phân loại theo từng nhóm tuổi của trẻ:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AgeBandStatBadge(
                        title = "Nhóm 4–6 tuổi",
                        count = count4to6,
                        modifier = Modifier.weight(1f)
                    )
                    AgeBandStatBadge(
                        title = "Nhóm 7–9 tuổi",
                        count = count7to9,
                        modifier = Modifier.weight(1f)
                    )
                    AgeBandStatBadge(
                        title = "Nhóm 10–12 tuổi",
                        count = count10to12,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Import from URL Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = QuizBluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cập nhật từ đường dẫn URL (JSON)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Nhập đường link trực tiếp (HTTP/HTTPS) chứa tệp JSON câu hỏi. Hệ thống sẽ tải và hợp nhất vào cơ sở dữ liệu trên máy mà không làm mất tiến độ học.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = {
                        urlInput = it
                        statusMessage = null
                    },
                    label = { Text("Đường dẫn URL chứa file JSON câu hỏi") },
                    placeholder = { Text("https://example.com/questions-custom.json") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_url_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick sample URL buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            urlInput = "https://raw.githubusercontent.com/quizgate/starter-packs/main/vietnamese_math_science_200.json"
                        }
                    ) {
                        Text("Điền link mẫu thử nghiệm", fontSize = 12.sp)
                    }

                    if (urlInput.isNotEmpty()) {
                        TextButton(onClick = { urlInput = ""; statusMessage = null }) {
                            Text("Xóa", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val trimmedUrl = urlInput.trim()
                        if (trimmedUrl.isEmpty() || (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://"))) {
                            statusMessage = "Vui lòng nhập đường dẫn URL hợp lệ bắt đầu bằng https:// hoặc http://"
                            isStatusSuccess = false
                            return@Button
                        }

                        isLoading = true
                        statusMessage = null
                        coroutineScope.launch {
                            val result = repository.importQuestionsFromUrl(trimmedUrl)
                            isLoading = false
                            if (result.isSuccess) {
                                val count = result.getOrNull() ?: 0
                                isStatusSuccess = true
                                statusMessage = "Đã tải và nạp thành công $count câu hỏi mới vào ngân hàng!"
                                refreshStats()
                            } else {
                                isStatusSuccess = false
                                val errMsg = result.exceptionOrNull()?.localizedMessage ?: "Lỗi kết nối mạng"
                                statusMessage = "Không thể cập nhật: $errMsg"
                            }
                        }
                    },
                    enabled = !isLoading && urlInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("update_questions_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đang tải dữ liệu...")
                    } else {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tải & Cập nhật ngân hàng câu hỏi")
                    }
                }

                // Status Banner
                statusMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isStatusSuccess) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isStatusSuccess) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isStatusSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = if (isStatusSuccess) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isStatusSuccess) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        // 3. Reset / Restore Default Starter Pack
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = QuizAmberSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Khôi phục dữ liệu gốc",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Khôi phục lại gói 200 câu hỏi mẫu tiếng Việt (Toán, Tiếng Việt, Logic) được biên soạn sẵn từ ứng dụng.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val count = repository.restoreDefaultQuestions(context)
                            isStatusSuccess = true
                            statusMessage = "Đã khôi phục thành công $count câu hỏi chuẩn mặc định!"
                            refreshStats()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("restore_default_questions_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Khôi phục 200 câu hỏi chuẩn ban đầu")
                }
            }
        }

        // 4. Schema Reference / Format Guide Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Định dạng JSON câu hỏi chuẩn",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = { showJsonSample = !showJsonSample }) {
                        Text(if (showJsonSample) "Thu gọn" else "Xem mẫu")
                    }
                }

                AnimatedVisibility(visible = showJsonSample) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = """[
  {
    "id": "q_math_101",
    "topic": "Toán",
    "age_band": "7-9",
    "difficulty": 1,
    "question": "8 x 7 bằng bao nhiêu?",
    "options": ["54", "56", "58", "64"],
    "answer": "56",
    "hint": "Gợi ý: 8 x 5 = 40, cộng thêm 16",
    "explanation": "8 x 7 = 56"
  }
]""",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgeBandStatBadge(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count câu",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
