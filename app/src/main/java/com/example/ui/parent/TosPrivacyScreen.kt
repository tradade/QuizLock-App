package com.example.ui.parent

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.QuizAmberSecondary
import com.example.ui.theme.QuizBluePrimary
import com.example.ui.theme.QuizGreenSuccess

@Composable
fun TosPrivacyScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .testTag("tos_privacy_screen")
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("tos_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Điều khoản & Quyền riêng tư",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cam kết bảo vệ tuyệt đối an toàn cho trẻ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tab Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Quyền riêng tư (Privacy)", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.PrivacyTip, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_privacy")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Điều khoản dịch vụ (TOS)", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_tos")
                )
            }

            // Tab Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (selectedTab == 0) {
                    PrivacyPolicyContent()
                } else {
                    TermsOfServiceContent()
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyContent() {
    // Hero Privacy Banner
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(QuizGreenSuccess),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "100% Cục bộ & Bảo mật trên thiết bị",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Không máy chủ, không thu thập dữ liệu cá nhân, không quảng cáo.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                )
            }
        }
    }

    PrivacySectionCard(
        icon = Icons.Default.Storage,
        title = "1. Thu thập và lưu trữ dữ liệu",
        body = "Ứng dụng Quiz Gate hoạt động theo nguyên tắc On-device First:\n" +
                "• Toàn bộ hồ sơ của bé, thời gian xem màn hình và lịch sử làm câu đố được lưu trữ cục bộ trong cơ sở dữ liệu SQLite (Room) mã hóa trên thiết bị của bạn.\n" +
                "• Ứng dụng KHÔNG truyền bất kỳ thông tin nhận dạng cá nhân nào (tên, độ tuổi, kết quả học tập) lên máy chủ bên thứ ba hay đám mây.\n" +
                "• Không sử dụng cookies theo dõi, Google Analytics hay bất kỳ SDK quảng cáo thương mại nào."
    )

    PrivacySectionCard(
        icon = Icons.Default.Visibility,
        title = "2. Minh bạch về quyền Trợ năng (Accessibility)",
        body = "Dịch vụ trợ năng QuizGateAccessibilityService chỉ được sử dụng với mục đích duy nhất:\n" +
                "• Nhận diện tên gói ứng dụng (package name) đang hiển thị ở tiền cảnh để tính thời gian xem các app giải trí (như YouTube, Games).\n" +
                "• Ứng dụng TUYỆT ĐỐI KHÔNG đọc nội dung màn hình, KHÔNG ghi nhận văn bản gõ phím, KHÔNG thu thập mật khẩu, tin nhắn hay thông tin tài khoản ngân hàng của gia đình."
    )

    PrivacySectionCard(
        icon = Icons.Default.Lock,
        title = "3. Bảo mật mã PIN & Mã khôi phục",
        body = "• Mã PIN phụ huynh được mã hóa một chiều bằng thuật toán tiêu chuẩn công nghiệp PBKDF2-HMAC-SHA256 với 150.000 vòng lặp kèm muối ngẫu nhiên (Salt).\n" +
                "• Không ai, kể cả lập trình viên, có thể giải mã ngược mã PIN từ bộ nhớ.\n" +
                "• Mã khôi phục 8 ký tự được tạo ngẫu nhiên lưu cục bộ giúp phụ huynh thiết lập lại mã PIN khi cần mà không phải phụ thuộc vào email hay dịch vụ đám mây."
    )

    PrivacySectionCard(
        icon = Icons.Default.Delete,
        title = "4. Quyền xóa dữ liệu",
        body = "Phụ huynh có toàn quyền kiểm soát dữ liệu:\n" +
                "• Bạn có thể xóa hồ sơ của bé, reset lịch sử câu đố trực tiếp trong ứng dụng.\n" +
                "• Khi gỡ cài đặt ứng dụng Quiz Gate, 100% dữ liệu liên quan sẽ bị xóa sạch hoàn toàn khỏi thiết bị."
    )
}

@Composable
private fun TermsOfServiceContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(QuizBluePrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Điều khoản sử dụng Quiz Gate",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Công cụ đồng hành cùng cha mẹ xây dựng thói quen số lành mạnh",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }
        }
    }

    PrivacySectionCard(
        icon = Icons.Default.CheckCircle,
        title = "1. Mục đích ứng dụng",
        body = "Quiz Gate được thiết kế như một giải pháp giáo dục kết hợp quản lý thời gian sử dụng thiết bị (Parental Control & Screen Time Gate). Thay vì cấm đoán cực đoan, ứng dụng khuyến khích trẻ rèn luyện tư duy Toán học, Tiếng Việt và Logic để đổi lấy thời gian giải trí tích cực."
    )

    PrivacySectionCard(
        icon = Icons.Default.Gavel,
        title = "2. Trách nhiệm của phụ huynh",
        body = "• Phụ huynh chịu trách nhiệm bảo mật mã PIN 6 số và lưu giữ mã khôi phục an toàn.\n" +
                "• Phụ huynh nên cùng thảo luận với con về hạn mức xem và chiến lược đố vui để tạo sự đồng thuận trong gia đình.\n" +
                "• Không chia sẻ mã PIN cho trẻ để đảm bảo cơ chế bảo vệ thời gian hoạt động hiệu quả."
    )

    PrivacySectionCard(
        icon = Icons.Default.Block,
        title = "3. Giới hạn trách nhiệm",
        body = "• Quiz Gate là công cụ hỗ trợ gia đình trên cơ sở thiện chí. Ứng dụng không thay thế hoàn toàn sự giám sát trực tiếp của cha mẹ.\n" +
                "• Các câu hỏi trong ngân hàng dữ liệu được biên soạn phù hợp chương trình giáo dục tiểu học và mầm non Việt Nam. Phụ huynh có thể tùy chỉnh hoặc nạp thêm câu hỏi theo nhu cầu học tập riêng."
    )
}

@Composable
private fun PrivacySectionCard(
    icon: ImageVector,
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = QuizBluePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}
