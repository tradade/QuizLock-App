package com.example.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class QuizGateAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Quiz Gate: Đã kích hoạt bảo vệ chống gỡ ứng dụng", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Cảnh báo: Tắt quyền Quản trị viên thiết bị sẽ vô hiệu hóa tính năng bảo vệ thời gian sử dụng thiết bị cho trẻ em."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Quiz Gate: Quyền quản trị đã bị tắt", Toast.LENGTH_SHORT).show()
    }
}
