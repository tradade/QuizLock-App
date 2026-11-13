package com.quizlock.core.i18n

import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    VI("vi", "Tiếng Việt", "🇻🇳"),
    EN("en", "English", "🇬🇧");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return when (code?.lowercase()) {
                "en" -> EN
                else -> VI
            }
        }
    }
}

class Strings internal constructor(private val values: Map<String, String>) {
    // Common
    val cancel: String by values
    val confirm: String by values
    val save: String by values
    val back: String by values
    val done: String by values
    val delete: String by values
    val edit: String by values
    val close: String by values
    val continueText: String by values
    val select: String by values
    val selected: String by values
    val active: String by values
    val watching: String by values
    val notSet: String by values
    val apply: String by values
    val success: String by values
    val error: String by values
    val clear: String by values
    val loading: String by values
    val complete: String by values
    val granted: String by values
    val grantPermission: String by values
    val minutes: String by values
    val hours: String by values
    val minsAbbr: String by values
    val hoursAbbr: String by values

    // Navigation
    val navDashboard: String by values
    val navProfiles: String by values
    val navSettings: String by values

    // Language Selector
    val languageSelectorTitle: String by values
    val languageSelectorDesc: String by values

    // Per-profile settings
    val profileSettingsTitle: String by values
    val profileSettingsSubtitle: String by values

    // Dashboard
    val dashboardTitle: String by values
    val activeChildBadge: String by values
    val switchChild: String by values
    val todayWatchTime: String by values
    val gatesPassed: String by values
    val quizSolvedLabel: String by values
    val starsLabel: String by values
    val quizAccuracy: String by values
    val ageGroupLabel: String by values
    val parentModeTitle: String by values
    val parentModeActiveDesc: String by values
    val parentModeInactiveDesc: String by values
    val startParentMode: String by values
    val stopParentMode: String by values
    val emergencyQuick30m: String by values
    val emergencyQuick1h: String by values
    val emergencyQuick2h: String by values
    val testGateButton: String by values
    val systemStatusRunning: String by values
    val systemStatusAttention: String by values

    // Age bands
    val band4to6Short: String by values
    val band7to9Short: String by values
    val band10to12Short: String by values

    // Profiles Manager & Custom Settings
    val profilesTitle: String by values
    val profilesSubtitle: String by values
    val addProfileFab: String by values
    val addProfileTitle: String by values
    val editProfileTitle: String by values
    val childNameLabel: String by values
    val childNamePlaceholder: String by values
    val childAgeLabel: String by values
    val ageUnit: String by values
    val ageBand4to6: String by values
    val ageBand7to9: String by values
    val ageBand10to12: String by values
    val avatarLabel: String by values
    val customBadge: String by values
    val dailyCapLabel: String by values
    val dailyCapShortLabel: String by values
    val gateShortLabel: String by values
    val customRulesTitle: String by values
    val customRulesSubtitle: String by values
    val customRulesToggle: String by values
    val inheritGlobalRulesNote: String by values
    val customRulesActiveNote: String by values
    val strategyTitle: String by values
    val strategyDailyCap: String by values
    val strategyDailyCapDesc: String by values
    val strategyInfinite: String by values
    val strategyInfiniteDesc: String by values
    val strategyEscalating: String by values
    val strategyEscalatingDesc: String by values
    val gateIntervalTitle: String by values
    val gateIntervalSubtitle: String by values
    val allowedHoursTitle: String by values
    val startTimeLabel: String by values
    val endTimeLabel: String by values
    val setAsActiveChild: String by values
    val deleteProfileTitle: String by values
    val deleteProfileConfirmMsg: String by values
    val cannotDeleteOnlyProfile: String by values
    val profileSavedSuccess: String by values

    // Policy & Allowed Hours (shared by Settings)
    val policyDailyCapTitle: String by values
    val policyDailyCapDesc: String by values
    val policyInfiniteTitle: String by values
    val policyInfiniteDesc: String by values
    val policyEscalatingTitle: String by values
    val policyEscalatingDesc: String by values
    val gateIntervalXTitle: String by values
    val gateIntervalXDesc: String by values
    val dailyCapLimitTitle: String by values
    val allowedHoursCardTitle: String by values
    val allowedHoursCardDesc: String by values
    val quickPresetsLabel: String by values
    val presetAllDay: String by values
    val presetAfterSchool: String by values
    val presetEvening: String by values
    val presetWeekend: String by values
    val startAllowedUpper: String by values
    val endBedtimeUpper: String by values
    val tapToEdit: String by values
    val timeInvalidFormat: String by values
    val timeStartBeforeEnd: String by values
    val timeWindowTooShort: String by values
    val timeWindowValid: String by values
    val timeValid: String by values
    val startTimePickerTitle: String by values
    val endTimePickerTitle: String by values
    val toLabel: String by values

    // Settings
    val settingsTitle: String by values
    val tabPolicy: String by values
    val tabQuestions: String by values
    val tabExclusions: String by values
    val tabSecurity: String by values
    val globalPolicyDesc: String by values
    val questionsStatTitle: String by values
    val importQuestionsUrlTitle: String by values
    val importQuestionsUrlPlaceholder: String by values
    val importQuestionsButton: String by values
    val restoreStarterQuestions: String by values
    val exclusionsDesc: String by values
    val exclusionsCardTitle: String by values
    val exclusionsCardDesc: String by values
    val searchAppsPlaceholder: String by values
    val searchAppsHint: String by values
    val appsLoading: String by values
    val noAppsFound: String by values
    val changePinTitle: String by values
    val changePinDesc: String by values
    val pinSecurityTitle: String by values
    val pinSecurityDesc: String by values
    val changePinButton: String by values
    val currentPinLabel: String by values
    val newPinLabel: String by values
    val confirmNewPinLabel: String by values
    val recoveryCodeTitle: String by values
    val recoveryCodeDesc: String by values
    val legalPrivacyCardTitle: String by values
    val legalPrivacyCardDesc: String by values
    val systemPermissionsTitle: String by values
    val systemPermissionsDesc: String by values
    val accessibilityPermissionButton: String by values
    val viewTosPrivacyButton: String by values
    val verifyCurrentPinTitle: String by values
    val verifyCurrentPinSubtitle: String by values
    val setNewPinTitle: String by values
    val pinNewInputLabel: String by values
    val pinNewConfirmInputLabel: String by values
    val pinMustBe6Digits: String by values
    val pinMismatch: String by values
    val saveNewPin: String by values

    // Onboarding
    val onboardingWelcomeTitle: String by values
    val onboardingWelcomeSubtitle: String by values
    val onboardingStartButton: String by values
    val onboardingBullet1: String by values
    val onboardingBullet2: String by values
    val onboardingBullet3: String by values
    val onboardingPinTitle: String by values
    val onboardingPinSubtitle: String by values
    val onboardingPinLabel: String by values
    val onboardingConfirmPinLabel: String by values
    val onboardingGeneratePinButton: String by values
    val onboardingRecoveryCodeNotice: String by values
    val onboardingProfileTitle: String by values
    val onboardingProfileSubtitle: String by values
    val onboardingReadyTitle: String by values
    val onboardingReadySubtitle: String by values
    val onboardingFinishButton: String by values
    val onboardingPermissionsTitle: String by values
    val onboardingPermissionsSubtitle: String by values
    val onboardingPermissionAccessibilityTitle: String by values
    val onboardingPermissionAccessibilityDesc: String by values
    val onboardingPermissionBatteryTitle: String by values
    val onboardingPermissionBatteryDesc: String by values
    val onboardingPermissionNotificationsTitle: String by values
    val onboardingPermissionNotificationsDesc: String by values
    val onboardingRecoveryCodeUpper: String by values
    val onboardingRecoveryCodeNote: String by values
    val onboardingChildNameLabel: String by values
    val onboardingPauseEvery: String by values
    val onboardingDailyCapLabel: String by values
    val pinMinLengthError: String by values

    // App Lock
    val appLockTitle: String by values
    val appLockSubtitle: String by values
    val appLockDevSuperPin: String by values

    // PIN Pad
    val pinPadDefaultTitle: String by values
    val pinPadDefaultSubtitle: String by values
    val pinRecoveryTitle: String by values
    val pinLockedOut: String by values
    val pinWrongAttempts: String by values
    val pinFailLockout: String by values
    val pinRecoveryPrompt: String by values
    val pinRecoveryCodeLabel: String by values
    val pinConfirmRecovery: String by values
    val pinRecoveryInvalid: String by values
    val pinBackToPin: String by values
    val pinForgot: String by values
    val pinLockIconDesc: String by values
    val deleteDigitDesc: String by values

    // Time Picker
    val hourRangeLabel: String by values
    val minuteRangeLabel: String by values
    val selectTime: String by values
    val decreaseHour: String by values
    val increaseHour: String by values
    val decreaseMinutes: String by values
    val increaseMinutes: String by values

    // TOS & Privacy
    val tosTitle: String by values
    val privacyTabTitle: String by values
    val termsTabTitle: String by values
    val tosCommitmentSubtitle: String by values
    val privacyHeroTitle: String by values
    val privacyHeroSubtitle: String by values
    val privacySection1Title: String by values
    val privacySection1Body: String by values
    val privacySection2Title: String by values
    val privacySection2Body: String by values
    val privacySection3Title: String by values
    val privacySection3Body: String by values
    val privacySection4Title: String by values
    val privacySection4Body: String by values
    val tosHeroTitle: String by values
    val tosHeroSubtitle: String by values
    val tosSection1Title: String by values
    val tosSection1Body: String by values
    val tosSection2Title: String by values
    val tosSection2Body: String by values
    val tosSection3Title: String by values
    val tosSection3Body: String by values

    // Gate Overlay
    val gateQuizWith: String by values
    val gateSubmit: String by values
    val gateTryAgain: String by values
    val gateCorrect: String by values
    val gateHint: String by values
    val gateEmergencyPin: String by values
    val gateContinueSubtitle: String by values
    val gateFallbackChildName: String by values
    val gateParentShort: String by values
    val gateTopicFallback: String by values
    val gateHintPrefix: String by values
    val gateWrongFirst: String by values
    val gateWrongSecond: String by values
    val gateVictoryTitle: String by values
    val gateVictorySubtitle: String by values
    val gateVictoryDefaultExplanation: String by values
    val gateReturning: String by values
    val gateParentModalTitle: String by values
    val gateParentModalSubtitle: String by values
    val gateUnlockDesc: String by values
    val gateReadQuestionDesc: String by values
    val gateVictoryDesc: String by values
    val gateStarDesc: String by values

    // Hard Block
    val hardBlockBedtimeTitle: String by values
    val hardBlockBedtimeSubtitle: String by values
    val hardBlockDailyCapTitle: String by values
    val hardBlockDailyCapSubtitle: String by values
    val hardBlockParentButton: String by values
    val hardBlockPinTitle: String by values
    val hardBlockPinSubtitle: String by values
    val hardBlockDesc: String by values
    val unlockDesc: String by values

    // Question Bank
    val questionBankTitle: String by values
    val questionCountSuffix: String by values
    val questionBankStatsDesc: String by values
    val bandGroup4to6: String by values
    val bandGroup7to9: String by values
    val bandGroup10to12: String by values
    val updateFromUrlTitle: String by values
    val updateFromUrlDesc: String by values
    val urlFieldLabel: String by values
    val fillSampleLink: String by values
    val invalidUrlMessage: String by values
    val importSuccess: String by values
    val importFailed: String by values
    val networkError: String by values
    val loadingData: String by values
    val downloadUpdateButton: String by values
    val restoreDataTitle: String by values
    val restoreDataDesc: String by values
    val restoreSuccess: String by values
    val restoreButton: String by values
    val jsonFormatTitle: String by values
    val collapse: String by values
    val showSample: String by values
    val excelImportTitle: String by values
    val excelImportDesc: String by values
    val excelImportFormatHint: String by values
    val excelImportButton: String by values
    val excelImportAgeBandLabel: String by values
    val excelImportLanguageLabel: String by values
    val excelImportTemplateCopy: String by values
    val excelImportTemplateCopied: String by values
    val excelImportSuccess: String by values
    val excelImportErrorEmpty: String by values
    val excelImportErrorOldFormat: String by values
    val excelImportErrorUnreadable: String by values
    val excelImportErrorNoQuestions: String by values
    val deleteAllQuestionsTitle: String by values
    val deleteAllQuestionsDesc: String by values
    val deleteAllQuestionsButton: String by values
    val deleteAllQuestionsConfirmTitle: String by values
    val deleteAllQuestionsConfirmMsg: String by values
    val deleteAllQuestionsAction: String by values
    val deleteAllQuestionsSuccess: String by values
    val deleteAllQuestionsError: String by values

    // Notification
    val notifBrokenTitle: String by values
    val notifBrokenContent: String by values
    val notifNoProfile: String by values
    val notifWatchingTitle: String by values
    val notifProtectionActive: String by values
    val notifParentModeRemaining: String by values
    val notifWatchProgress: String by values
    val notifTitle: String by values
}

val ViStrings = Strings(
    mapOf(
        "cancel" to ("Hủy"),
        "confirm" to ("Xác nhận"),
        "save" to ("Lưu thay đổi"),
        "back" to ("Quay lại"),
        "done" to ("Xong"),
        "delete" to ("Xóa"),
        "edit" to ("Chỉnh sửa"),
        "close" to ("Đóng"),
        "continueText" to ("Tiếp tục"),
        "select" to ("Chọn"),
        "selected" to ("Đã chọn"),
        "active" to ("Đang kích hoạt"),
        "watching" to ("Đang xem"),
        "notSet" to ("Chưa thiết lập"),
        "apply" to ("Áp dụng"),
        "success" to ("Thành công"),
        "error" to ("Lỗi"),
        "clear" to ("Xóa"),
        "loading" to ("Đang tải..."),
        "complete" to ("Hoàn tất"),
        "granted" to ("Đã bật"),
        "grantPermission" to ("Cấp quyền"),
        "minutes" to ("phút"),
        "hours" to ("giờ"),
        "minsAbbr" to ("p"),
        "hoursAbbr" to ("h"),
        "navDashboard" to ("Tổng quan"),
        "navProfiles" to ("Hồ sơ bé"),
        "navSettings" to ("Cài đặt"),
        "languageSelectorTitle" to ("Ngôn ngữ hiển thị"),
        "languageSelectorDesc" to ("Chọn ngôn ngữ giao diện ứng dụng"),
        "profileSettingsTitle" to ("Cài đặt của %s"),
        "profileSettingsSubtitle" to ("Chiến lược, cổng câu đố và khung giờ riêng cho bé."),
        "dashboardTitle" to ("Bảng điều khiển phụ huynh"),
        "activeChildBadge" to ("Bé đang dùng thiết bị"),
        "switchChild" to ("Đổi bé"),
        "todayWatchTime" to ("Thời gian xem hôm nay"),
        "gatesPassed" to ("Cổng câu đố đã vượt qua"),
        "quizSolvedLabel" to ("Câu trả lời đúng"),
        "starsLabel" to ("Sao"),
        "quizAccuracy" to ("Tỷ lệ trả lời đúng"),
        "ageGroupLabel" to ("Nhóm"),
        "parentModeTitle" to ("Chế độ phụ huynh"),
        "parentModeActiveDesc" to ("Cổng đố vui đang tạm dừng, mở toàn bộ thiết bị cho phụ huynh."),
        "parentModeInactiveDesc" to ("Bật khi phụ huynh mượn máy làm việc hoặc cần tắt cổng tạm thời."),
        "startParentMode" to ("Bật chế độ phụ huynh"),
        "stopParentMode" to ("Khóa lại ngay"),
        "emergencyQuick30m" to ("Tạm dừng 30p"),
        "emergencyQuick1h" to ("Tạm dừng 1h"),
        "emergencyQuick2h" to ("Tạm dừng 2h"),
        "testGateButton" to ("Thử nghiệm cổng câu đố"),
        "systemStatusRunning" to ("Hệ thống bảo vệ đang hoạt động"),
        "systemStatusAttention" to ("Cần kiểm tra cấp quyền hệ thống"),
        "band4to6Short" to ("4–6 tuổi"),
        "band7to9Short" to ("7–9 tuổi"),
        "band10to12Short" to ("10–12 tuổi"),
        "profilesTitle" to ("Quản lý hồ sơ trẻ"),
        "profilesSubtitle" to ("Nhấp vào hồ sơ bé để tùy chỉnh chiến lược, cổng câu đố và khung giờ riêng."),
        "addProfileFab" to ("Thêm bé"),
        "addProfileTitle" to ("Thêm hồ sơ bé mới"),
        "editProfileTitle" to ("Tùy chỉnh hồ sơ bé"),
        "childNameLabel" to ("Tên của bé"),
        "childNamePlaceholder" to ("Ví dụ: Bé Bo, Minh Anh..."),
        "childAgeLabel" to ("Tuổi của bé"),
        "ageUnit" to ("tuổi"),
        "ageBand4to6" to ("4–6 tuổi: Đếm số, con vật, nhận biết"),
        "ageBand7to9" to ("7–9 tuổi: Bảng nhân chia, từ vựng"),
        "ageBand10to12" to ("10–12 tuổi: Phân số, đố vui logic"),
        "avatarLabel" to ("Chọn biểu tượng đại diện"),
        "customBadge" to ("Tùy chỉnh"),
        "dailyCapLabel" to ("Hạn mức tối đa/ngày (Trần ngày)"),
        "dailyCapShortLabel" to ("Trần ngày"),
        "gateShortLabel" to ("Cổng"),
        "customRulesTitle" to ("Quy định riêng cho bé"),
        "customRulesSubtitle" to ("Tùy chỉnh thời gian ngắt, trần ngày và chiến lược độc lập cho riêng bé này."),
        "customRulesToggle" to ("Áp dụng quy định riêng cho bé"),
        "inheritGlobalRulesNote" to ("Đang kế thừa quy định chung trong Cài đặt hệ thống."),
        "customRulesActiveNote" to ("Quy định riêng đang được áp dụng ưu tiên cho bé này."),
        "strategyTitle" to ("Chiến lược kiểm soát"),
        "strategyDailyCap" to ("Trần ngày (Daily Cap)"),
        "strategyDailyCapDesc" to ("Khóa máy khi đạt tổng thời gian trong ngày. Sau mỗi X phút ngắt giải 1 câu đố."),
        "strategyInfinite" to ("Vô hạn (Không áp trần)"),
        "strategyInfiniteDesc" to ("Cứ sau mỗi X phút bé phải giải đúng 1 câu đố để xem tiếp. Không khống chế trần ngày."),
        "strategyEscalating" to ("Tăng dần (Thử thách lũy tiến)"),
        "strategyEscalatingDesc" to ("Mỗi chu kỳ cổng tiếp theo, số câu đố phải giải tăng dần (1 -> 2 -> 3...) để rèn luyện."),
        "gateIntervalTitle" to ("Khoảng thời gian ngắt cổng"),
        "gateIntervalSubtitle" to ("Màn hình sẽ tạm ngắt để mở câu đố sau mỗi"),
        "allowedHoursTitle" to ("Khung giờ được phép xem"),
        "startTimeLabel" to ("Giờ bắt đầu"),
        "endTimeLabel" to ("Giờ kết thúc"),
        "setAsActiveChild" to ("Đặt làm bé đang xem"),
        "deleteProfileTitle" to ("Xóa hồ sơ"),
        "deleteProfileConfirmMsg" to ("Bạn có chắc chắn muốn xóa hồ sơ bé này không? Dữ liệu lịch sử giải câu đố của bé sẽ được lưu trữ an toàn."),
        "cannotDeleteOnlyProfile" to ("Không thể xóa hồ sơ duy nhất còn lại."),
        "profileSavedSuccess" to ("Đã lưu cài đặt hồ sơ bé thành công!"),
        "policyDailyCapTitle" to ("Trần ngày (Mặc định)"),
        "policyDailyCapDesc" to ("Quiz kiếm thêm phút trong hạn mức ngày. Chạm trần sẽ khóa cứng."),
        "policyInfiniteTitle" to ("Vô hạn"),
        "policyInfiniteDesc" to ("Cứ mỗi lần giải đúng được xem thêm +X phút, không giới hạn tổng."),
        "policyEscalatingTitle" to ("Giảm dần chi phí"),
        "policyEscalatingDesc" to ("Mỗi cổng kế tiếp cần giải nhiều câu hơn (1 -> 2 -> 3 câu, tối đa 5)."),
        "gateIntervalXTitle" to ("Khoảng cách cổng (X): %d phút"),
        "gateIntervalXDesc" to ("Sau mỗi %d phút xem màn hình, cổng quiz sẽ xuất hiện."),
        "dailyCapLimitTitle" to ("Hạn mức xem tối đa trong ngày: %d phút"),
        "allowedHoursCardTitle" to ("Khung giờ được phép xem"),
        "allowedHoursCardDesc" to ("Ngoài khung giờ này, ứng dụng sẽ tự động khóa cứng chúc bé ngủ ngon."),
        "quickPresetsLabel" to ("Lịch mẫu chọn nhanh:"),
        "presetAllDay" to ("Cả ngày"),
        "presetAfterSchool" to ("Sau giờ học"),
        "presetEvening" to ("Tối thư giãn"),
        "presetWeekend" to ("Cuối tuần"),
        "startAllowedUpper" to ("BẮT ĐẦU CHO PHÉP"),
        "endBedtimeUpper" to ("BẮT ĐẦU ĐI NGỦ"),
        "tapToEdit" to ("Chạm để chỉnh"),
        "timeInvalidFormat" to ("Định dạng giờ không hợp lệ (cần dạng HH:mm)"),
        "timeStartBeforeEnd" to ("Giờ bắt đầu (%s) phải sớm hơn giờ kết thúc (%s). Khung giờ hoạt động trong ngày."),
        "timeWindowTooShort" to ("Khung giờ cho phép xem nên tối thiểu 30 phút."),
        "timeWindowValid" to ("Thời lượng cửa sổ xem: %s/ngày. Sau %s sẽ tự động khóa cứng ngủ ngon."),
        "timeValid" to ("Hợp lệ"),
        "startTimePickerTitle" to ("Giờ Bắt Đầu Cho Phép"),
        "endTimePickerTitle" to ("Giờ Bắt Đầu Đi Ngủ"),
        "toLabel" to ("đến"),
        "settingsTitle" to ("Cài đặt & Quản trị"),
        "tabPolicy" to ("Quy định"),
        "tabQuestions" to ("Câu hỏi"),
        "tabExclusions" to ("Loại trừ"),
        "tabSecurity" to ("Bảo mật"),
        "globalPolicyDesc" to ("Quy định mặc định áp dụng chung cho toàn hệ thống khi hồ sơ không bật quy định riêng."),
        "questionsStatTitle" to ("Thống kê ngân hàng câu hỏi"),
        "importQuestionsUrlTitle" to ("Cập nhật câu hỏi từ đường dẫn URL"),
        "importQuestionsUrlPlaceholder" to ("https://example.com/questions.json"),
        "importQuestionsButton" to ("Tải về & Nạp câu hỏi"),
        "restoreStarterQuestions" to ("Khôi phục gói câu hỏi mặc định"),
        "exclusionsDesc" to ("Các ứng dụng học tập hoặc cuộc gọi khẩn cấp được miễn trừ, không bao giờ bị khóa màn hình."),
        "exclusionsCardTitle" to ("Ứng dụng loại trừ (Không đếm giờ)"),
        "exclusionsCardDesc" to ("Bật cho các ứng dụng học tập, lớp học trực tuyến hoặc sách để trẻ dùng không bị trừ giờ."),
        "searchAppsPlaceholder" to ("Tìm kiếm ứng dụng..."),
        "searchAppsHint" to ("Tìm ứng dụng theo tên..."),
        "appsLoading" to ("Đang nạp danh sách ứng dụng..."),
        "noAppsFound" to ("Không tìm thấy ứng dụng phù hợp."),
        "changePinTitle" to ("Đổi mã PIN phụ huynh"),
        "changePinDesc" to ("Đổi mã PIN 6 số dùng để mở cài đặt và xác thực phụ huynh."),
        "pinSecurityTitle" to ("Mã PIN & Bảo mật"),
        "pinSecurityDesc" to ("Mã PIN 6 số dùng để truy cập cài đặt, tạm dừng giám sát hoặc mở khóa khẩn cấp."),
        "changePinButton" to ("Đổi mã PIN phụ huynh mới"),
        "currentPinLabel" to ("Mã PIN hiện tại"),
        "newPinLabel" to ("Mã PIN mới (6 số)"),
        "confirmNewPinLabel" to ("Xác nhận mã PIN mới"),
        "recoveryCodeTitle" to ("Mã khôi phục khẩn cấp"),
        "recoveryCodeDesc" to ("Sử dụng mã này khi bạn quên mã PIN phụ huynh."),
        "legalPrivacyCardTitle" to ("Pháp lý & Quyền riêng tư"),
        "legalPrivacyCardDesc" to ("Xem Điều khoản dịch vụ và Chính sách bảo vệ quyền riêng tư 100% On-device."),
        "systemPermissionsTitle" to ("Kiểm tra quyền hệ thống"),
        "systemPermissionsDesc" to ("Nếu dịch vụ giám sát chạy ngầm bị dừng, hãy bật lại quyền Trợ năng:"),
        "accessibilityPermissionButton" to ("1. Dịch vụ Trợ năng (Accessibility)"),
        "viewTosPrivacyButton" to ("Xem Điều khoản & Chính sách quyền riêng tư"),
        "verifyCurrentPinTitle" to ("Xác nhận PIN hiện tại"),
        "verifyCurrentPinSubtitle" to ("Nhập mã PIN hiện tại để xác thực phụ huynh"),
        "setNewPinTitle" to ("Đặt mã PIN mới"),
        "pinNewInputLabel" to ("Mã PIN mới (6 chữ số)"),
        "pinNewConfirmInputLabel" to ("Nhập lại mã PIN mới"),
        "pinMustBe6Digits" to ("Mã PIN phải đủ 6 chữ số."),
        "pinMismatch" to ("Xác nhận mã PIN không trùng khớp."),
        "saveNewPin" to ("Lưu mã PIN mới"),
        "onboardingWelcomeTitle" to ("Chào mừng đến với QuizLock!"),
        "onboardingWelcomeSubtitle" to ("Giải pháp thông minh giúp phụ huynh kiểm soát thời gian sử dụng thiết bị của trẻ bằng các câu đố giáo dục bổ ích."),
        "onboardingStartButton" to ("Bắt đầu thiết lập (5 phút)"),
        "onboardingBullet1" to ("Tự động ngắt màn hình sau mỗi X phút để trẻ giải 1 câu đố."),
        "onboardingBullet2" to ("200+ câu hỏi Toán, Tiếng Việt & Logic thú vị, vừa học vừa chơi."),
        "onboardingBullet3" to ("100% On-device, không máy chủ, bảo mật tuyệt đối cho gia đình."),
        "onboardingPinTitle" to ("Tạo mã PIN phụ huynh"),
        "onboardingPinSubtitle" to ("Mã PIN gồm 6 số dùng để mở cài đặt, đổi thời gian hoặc tắt cổng khẩn cấp."),
        "onboardingPinLabel" to ("Nhập mã PIN (6 số)"),
        "onboardingConfirmPinLabel" to ("Xác nhận lại mã PIN"),
        "onboardingGeneratePinButton" to ("Tạo mã PIN & Sinh mã khôi phục"),
        "onboardingRecoveryCodeNotice" to ("Tôi đã ghi lại mã khôi phục an toàn"),
        "onboardingProfileTitle" to ("Hồ sơ của bé & Quy định"),
        "onboardingProfileSubtitle" to ("Thiết lập hồ sơ ban đầu, chiến lược kiểm soát và chu kỳ cổng câu đố."),
        "onboardingReadyTitle" to ("Tất cả đã sẵn sàng!"),
        "onboardingReadySubtitle" to ("QuizLock đã sẵn sàng bảo vệ thời gian sử dụng thiết bị.\nBạn có thể điều chỉnh thêm các khung giờ và ứng dụng loại trừ trong Cài Đặt bất kỳ lúc nào."),
        "onboardingFinishButton" to ("Vào Bảng Điều Khiển Phụ Huynh"),
        "onboardingPermissionsTitle" to ("Cấp quyền giám sát cần thiết"),
        "onboardingPermissionsSubtitle" to ("Để đếm giờ và hiển thị cổng câu đố phủ lên YouTube/Games, ứng dụng cần các quyền sau:"),
        "onboardingPermissionAccessibilityTitle" to ("1. Trợ năng (Accessibility Service)"),
        "onboardingPermissionAccessibilityDesc" to ("Bắt buộc: Theo dõi app đang mở và mở cổng câu đố."),
        "onboardingPermissionBatteryTitle" to ("2. Bỏ qua tối ưu hóa pin"),
        "onboardingPermissionBatteryDesc" to ("Giúp dịch vụ giám sát không bị hệ điều hành tắt ngầm."),
        "onboardingPermissionNotificationsTitle" to ("3. Thông báo trạng thái"),
        "onboardingPermissionNotificationsDesc" to ("Hiển thị thông báo thường trực cho biết ứng dụng đang bảo vệ."),
        "onboardingRecoveryCodeUpper" to ("MÃ KHÔI PHỤC CỦA BẠN"),
        "onboardingRecoveryCodeNote" to ("Hãy ghi nhớ hoặc chụp lại mã này. Mã này dùng để reset PIN khi bạn quên."),
        "onboardingChildNameLabel" to ("Tên của bé (ví dụ: Bé Bo, Minh Anh)"),
        "onboardingPauseEvery" to ("Ngắt màn hình sau mỗi: %d phút"),
        "onboardingDailyCapLabel" to ("Tổng thời gian xem tối đa/ngày (Trần ngày): %s (%d phút)"),
        "pinMinLengthError" to ("Mã PIN phải có ít nhất 6 chữ số."),
        "appLockTitle" to ("Ứng dụng đang khóa"),
        "appLockSubtitle" to ("Nhập mã PIN phụ huynh để vào phần quản lý."),
        "appLockDevSuperPin" to ("Dev Super PIN"),
        "pinPadDefaultTitle" to ("Xác thực mã PIN phụ huynh"),
        "pinPadDefaultSubtitle" to ("Nhập mã PIN 6 số để tiếp tục"),
        "pinRecoveryTitle" to ("Khôi phục PIN"),
        "pinLockedOut" to ("Tài khoản bị tạm khóa vì nhập sai nhiều lần.\nVui lòng đợi %d giây nữa."),
        "pinWrongAttempts" to ("Mã PIN không đúng. Còn %d lần thử."),
        "pinFailLockout" to ("Sai 5 lần liên tiếp. Khóa thử trong 5 phút!"),
        "pinRecoveryPrompt" to ("Nhập mã khôi phục 8 ký tự được tạo lúc thiết lập ban đầu."),
        "pinRecoveryCodeLabel" to ("Mã khôi phục (8 ký tự)"),
        "pinConfirmRecovery" to ("Xác nhận mã khôi phục"),
        "pinRecoveryInvalid" to ("Mã khôi phục không hợp lệ."),
        "pinBackToPin" to ("Quay lại nhập PIN"),
        "pinForgot" to ("Quên PIN? Nhập mã khôi phục"),
        "pinLockIconDesc" to ("Khóa"),
        "deleteDigitDesc" to ("Xóa một số"),
        "hourRangeLabel" to ("Giờ (00 - 23)"),
        "minuteRangeLabel" to ("Phút (00 - 59)"),
        "selectTime" to ("Chọn %s"),
        "decreaseHour" to ("Giảm 1 giờ"),
        "increaseHour" to ("Tăng 1 giờ"),
        "decreaseMinutes" to ("Giảm 5 phút"),
        "increaseMinutes" to ("Tăng 5 phút"),
        "tosTitle" to ("Điều khoản & Quyền riêng tư"),
        "privacyTabTitle" to ("Quyền riêng tư"),
        "termsTabTitle" to ("Điều khoản dịch vụ"),
        "tosCommitmentSubtitle" to ("Cam kết bảo vệ tuyệt đối an toàn cho trẻ"),
        "privacyHeroTitle" to ("100% Cục bộ & Bảo mật trên thiết bị"),
        "privacyHeroSubtitle" to ("Không máy chủ, không thu thập dữ liệu cá nhân, không quảng cáo."),
        "privacySection1Title" to ("1. Thu thập và lưu trữ dữ liệu"),
        "privacySection1Body" to ("Ứng dụng QuizLock hoạt động theo nguyên tắc On-device First:\n" +
        "• Toàn bộ hồ sơ của bé, thời gian xem màn hình và lịch sử làm câu đố được lưu trữ cục bộ trong cơ sở dữ liệu SQLite (Room) trên thiết bị của bạn.\n" +
        "• Ứng dụng KHÔNG truyền bất kỳ thông tin nhận dạng cá nhân nào (tên, độ tuổi, kết quả học tập) lên máy chủ bên thứ ba hay đám mây.\n" +
        "• Không sử dụng cookies theo dõi, Google Analytics hay bất kỳ SDK quảng cáo thương mại nào."),
        "privacySection2Title" to ("2. Minh bạch về quyền Trợ năng (Accessibility)"),
        "privacySection2Body" to ("Dịch vụ trợ năng QuizGateAccessibilityService chỉ được sử dụng với mục đích duy nhất:\n" +
        "• Nhận diện tên gói ứng dụng (package name) đang hiển thị ở tiền cảnh để tính thời gian xem các app giải trí (như YouTube, Games).\n" +
        "• Ứng dụng TUYỆT ĐỐI KHÔNG đọc nội dung màn hình, KHÔNG ghi nhận văn bản gõ phím, KHÔNG thu thập mật khẩu, tin nhắn hay thông tin tài khoản ngân hàng của gia đình."),
        "privacySection3Title" to ("3. Bảo mật mã PIN & Mã khôi phục"),
        "privacySection3Body" to ("• Mã PIN phụ huynh được mã hóa một chiều bằng thuật toán tiêu chuẩn công nghiệp PBKDF2-HMAC-SHA256 với 150.000 vòng lặp kèm muối ngẫu nhiên (Salt).\n" +
        "• Không ai, kể cả lập trình viên, có thể giải mã ngược mã PIN từ bộ nhớ.\n" +
        "• Mã khôi phục 8 ký tự được tạo ngẫu nhiên lưu cục bộ giúp phụ huynh thiết lập lại mã PIN khi cần mà không phải phụ thuộc vào email hay dịch vụ đám mây."),
        "privacySection4Title" to ("4. Quyền xóa dữ liệu"),
        "privacySection4Body" to ("Phụ huynh có toàn quyền kiểm soát dữ liệu:\n" +
        "• Bạn có thể xóa hồ sơ của bé, reset lịch sử câu đố trực tiếp trong ứng dụng.\n" +
        "• Khi gỡ cài đặt ứng dụng QuizLock, 100% dữ liệu liên quan sẽ bị xóa sạch hoàn toàn khỏi thiết bị."),
        "tosHeroTitle" to ("Điều khoản sử dụng QuizLock"),
        "tosHeroSubtitle" to ("Công cụ đồng hành cùng cha mẹ xây dựng thói quen số lành mạnh"),
        "tosSection1Title" to ("1. Mục đích ứng dụng"),
        "tosSection1Body" to ("QuizLock được thiết kế như một giải pháp giáo dục kết hợp quản lý thời gian sử dụng thiết bị (Parental Control & Screen Time Gate). Thay vì cấm đoán cực đoan, ứng dụng khuyến khích trẻ rèn luyện tư duy Toán học, Tiếng Việt và Logic để đổi lấy thời gian giải trí tích cực."),
        "tosSection2Title" to ("2. Trách nhiệm của phụ huynh"),
        "tosSection2Body" to ("• Phụ huynh chịu trách nhiệm bảo mật mã PIN 6 số và lưu giữ mã khôi phục an toàn.\n" +
        "• Phụ huynh nên cùng thảo luận với con về hạn mức xem và chiến lược đố vui để tạo sự đồng thuận trong gia đình.\n" +
        "• Không chia sẻ mã PIN cho trẻ để đảm bảo cơ chế bảo vệ thời gian hoạt động hiệu quả."),
        "tosSection3Title" to ("3. Giới hạn trách nhiệm"),
        "tosSection3Body" to ("• QuizLock là công cụ hỗ trợ gia đình trên cơ sở thiện chí. Ứng dụng không thay thế hoàn toàn sự giám sát trực tiếp của cha mẹ.\n" +
        "• Các câu hỏi trong ngân hàng dữ liệu được biên soạn phù hợp chương trình giáo dục tiểu học và mầm non Việt Nam. Phụ huynh có thể tùy chỉnh hoặc nạp thêm câu hỏi theo nhu cầu học tập riêng."),
        "gateQuizWith" to ("Đố vui cùng"),
        "gateSubmit" to ("Kiểm tra đáp án"),
        "gateTryAgain" to ("Chưa đúng rồi! Bé thử lại nhé"),
        "gateCorrect" to ("Chính xác! Giỏi lắm bé ơi 🎉"),
        "gateHint" to ("Gợi ý"),
        "gateEmergencyPin" to ("Mở khóa phụ huynh"),
        "gateContinueSubtitle" to ("Giải đúng để tiếp tục xem nhé!"),
        "gateFallbackChildName" to ("Bé"),
        "gateParentShort" to ("Ba Mẹ"),
        "gateTopicFallback" to ("Toán"),
        "gateHintPrefix" to ("Gợi ý: %s"),
        "gateWrongFirst" to ("Chưa đúng rồi! Hãy thử lại một lần nữa nha bé."),
        "gateWrongSecond" to ("Hãy đọc gợi ý ở trên để tìm đáp án đúng nhé!"),
        "gateVictoryTitle" to ("Tuyệt Vời!"),
        "gateVictorySubtitle" to ("Con đã trả lời chính xác!"),
        "gateVictoryDefaultExplanation" to ("Chúc mừng con đã trả lời chính xác!"),
        "gateReturning" to ("Đang quay lại ứng dụng..."),
        "gateParentModalTitle" to ("Mở khóa cổng Quiz"),
        "gateParentModalSubtitle" to ("Nhập mã PIN để tắt cổng hoặc kích hoạt Parent Mode"),
        "gateUnlockDesc" to ("Mở khóa cha mẹ"),
        "gateReadQuestionDesc" to ("Đọc câu hỏi bằng giọng nói"),
        "gateVictoryDesc" to ("Chiến thắng"),
        "gateStarDesc" to ("Ngôi sao"),
        "hardBlockBedtimeTitle" to ("Đã Đến Giờ Đi Ngủ!"),
        "hardBlockBedtimeSubtitle" to ("Thiết bị tạm nghỉ ngơi cho đến 06:00 sáng mai.\nChúc con ngủ thật ngon và mơ đẹp!"),
        "hardBlockDailyCapTitle" to ("Đã Hết Thời Gian Xem Hôm Nay!"),
        "hardBlockDailyCapSubtitle" to ("Con đã đạt giới hạn thời gian xem màn hình trong ngày.\nHãy đi vận động hoặc đọc sách nhé!"),
        "hardBlockParentButton" to ("Ba/Mẹ ơi (Mở bằng PIN)"),
        "hardBlockPinTitle" to ("Xác nhận mở khóa"),
        "hardBlockPinSubtitle" to ("Nhập mã PIN để bật Parent Mode (4 giờ) hoặc tắt chặn"),
        "hardBlockDesc" to ("Chặn cứng"),
        "unlockDesc" to ("Mở khóa"),
        "questionBankTitle" to ("Ngân hàng câu hỏi hiện tại"),
        "questionCountSuffix" to ("%d câu"),
        "questionBankStatsDesc" to ("Số lượng câu hỏi được phân loại theo từng nhóm tuổi của trẻ:"),
        "bandGroup4to6" to ("Nhóm 4–6 tuổi"),
        "bandGroup7to9" to ("Nhóm 7–9 tuổi"),
        "bandGroup10to12" to ("Nhóm 10–12 tuổi"),
        "updateFromUrlTitle" to ("Cập nhật từ đường dẫn URL (JSON)"),
        "updateFromUrlDesc" to ("Nhập đường link HTTPS trực tiếp chứa tệp JSON câu hỏi. Hệ thống sẽ tải và hợp nhất vào cơ sở dữ liệu trên máy mà không làm mất tiến độ học."),
        "urlFieldLabel" to ("Đường dẫn URL chứa file JSON câu hỏi"),
        "fillSampleLink" to ("Điền link mẫu thử nghiệm"),
        "invalidUrlMessage" to ("Vui lòng nhập đường dẫn HTTPS hợp lệ (bắt đầu bằng https://)."),
        "importSuccess" to ("Đã tải và nạp %d câu hỏi vào ngân hàng!"),
        "importFailed" to ("Không thể cập nhật: %s"),
        "networkError" to ("Lỗi kết nối mạng"),
        "loadingData" to ("Đang tải dữ liệu..."),
        "downloadUpdateButton" to ("Tải & Cập nhật ngân hàng câu hỏi"),
        "restoreDataTitle" to ("Khôi phục dữ liệu gốc"),
        "restoreDataDesc" to ("Khôi phục lại gói 200 câu hỏi mẫu tiếng Việt (Toán, Tiếng Việt, Logic) được biên soạn sẵn từ ứng dụng."),
        "restoreSuccess" to ("Đã khôi phục thành công %d câu hỏi chuẩn mặc định!"),
        "restoreButton" to ("Khôi phục 200 câu hỏi chuẩn ban đầu"),
        "jsonFormatTitle" to ("Định dạng nâng cao (JSON / URL)"),
        "collapse" to ("Thu gọn"),
        "showSample" to ("Xem mẫu"),
        "excelImportTitle" to ("Nhập câu hỏi từ Excel"),
        "excelImportDesc" to ("Cách dễ nhất: mở Excel hoặc Google Sheets, dán mẫu rồi điền mỗi dòng một câu hỏi. Bắt buộc có câu hỏi, ít nhất 2 đáp án và đáp án đúng. File .xlsx, .csv, .tsv và CSV phân cách bằng tab đều được hỗ trợ."),
        "excelImportFormatHint" to ("Mẫu khuyên dùng: Câu hỏi | Đáp án A | Đáp án B | Đáp án C | Đáp án D | Đáp án đúng | Lời giải | Gợi ý. Nhóm tuổi và ngôn ngữ dùng lựa chọn bên dưới; không cần thêm vào file."),
        "excelImportButton" to ("Chọn file Excel / CSV / TSV"),
        "excelImportAgeBandLabel" to ("Áp dụng cho nhóm tuổi"),
        "excelImportLanguageLabel" to ("Ngôn ngữ nội dung"),
        "excelImportTemplateCopy" to ("Sao chép mẫu để dán vào Excel / Sheets"),
        "excelImportTemplateCopied" to ("Đã sao chép mẫu. Hãy dán vào Excel hoặc Google Sheets rồi điền câu hỏi."),
        "excelImportSuccess" to ("Đã nhập %d câu hỏi (bỏ qua %d dòng lỗi, %d câu trùng)."),
        "excelImportErrorEmpty" to ("File trống."),
        "excelImportErrorOldFormat" to ("Chưa hỗ trợ file .xls cũ. Hãy lưu sang .xlsx hoặc .csv."),
        "excelImportErrorUnreadable" to ("Không đọc được file. Kiểm tra định dạng .xlsx/.csv."),
        "excelImportErrorNoQuestions" to ("Không tìm thấy câu hỏi hợp lệ trong file."),
        "deleteAllQuestionsTitle" to ("Xóa toàn bộ ngân hàng câu hỏi"),
        "deleteAllQuestionsDesc" to ("Xóa tất cả câu hỏi đã nhập và câu hỏi mặc định. Cổng đố vui sẽ dùng bộ sinh câu hỏi tự động cho đến khi bạn nhập lại."),
        "deleteAllQuestionsButton" to ("Xóa toàn bộ câu hỏi"),
        "deleteAllQuestionsConfirmTitle" to ("Xác nhận xóa toàn bộ?"),
        "deleteAllQuestionsConfirmMsg" to ("Hành động này không thể hoàn tác. Toàn bộ ngân hàng câu hỏi và lịch sử đã gặp sẽ bị xóa."),
        "deleteAllQuestionsAction" to ("Xóa tất cả"),
        "deleteAllQuestionsSuccess" to ("Đã xóa %d câu hỏi. Cổng đố vui sẽ dùng câu hỏi tự sinh."),
        "deleteAllQuestionsError" to ("Không thể xóa ngân hàng câu hỏi."),
        "notifBrokenTitle" to ("Cần kiểm tra quyền"),
        "notifBrokenContent" to ("Dịch vụ Trợ năng đang tắt — mở QuizLock để sửa."),
        "notifNoProfile" to ("Chưa có hồ sơ trẻ"),
        "notifWatchingTitle" to ("Đang canh chừng thời gian màn hình"),
        "notifProtectionActive" to ("Bảo vệ hoạt động"),
        "notifParentModeRemaining" to ("Parent Mode: Còn %d phút"),
        "notifWatchProgress" to ("%s: Đã xem %d phút / %d phút"),
        "notifTitle" to ("QuizLock đang bảo vệ"),
    )
)

val EnStrings = Strings(
    mapOf(
        "cancel" to ("Cancel"),
        "confirm" to ("Confirm"),
        "save" to ("Save Changes"),
        "back" to ("Back"),
        "done" to ("Done"),
        "delete" to ("Delete"),
        "edit" to ("Edit"),
        "close" to ("Close"),
        "continueText" to ("Continue"),
        "select" to ("Select"),
        "selected" to ("Selected"),
        "active" to ("Active"),
        "watching" to ("Watching"),
        "notSet" to ("Not set"),
        "apply" to ("Apply"),
        "success" to ("Success"),
        "error" to ("Error"),
        "clear" to ("Clear"),
        "loading" to ("Loading..."),
        "complete" to ("Complete"),
        "granted" to ("Enabled"),
        "grantPermission" to ("Grant"),
        "minutes" to ("minutes"),
        "hours" to ("hours"),
        "minsAbbr" to ("m"),
        "hoursAbbr" to ("h"),
        "navDashboard" to ("Dashboard"),
        "navProfiles" to ("Profiles"),
        "navSettings" to ("Settings"),
        "languageSelectorTitle" to ("Display Language"),
        "languageSelectorDesc" to ("Choose application interface language"),
        "profileSettingsTitle" to ("%s's Settings"),
        "profileSettingsSubtitle" to ("Strategy, quiz gates and allowed hours for this child."),
        "dashboardTitle" to ("Parent Dashboard"),
        "activeChildBadge" to ("Current Child Watching"),
        "switchChild" to ("Switch"),
        "todayWatchTime" to ("Today's Screen Time"),
        "gatesPassed" to ("QuizLocks Cleared"),
        "quizSolvedLabel" to ("Correct Answers"),
        "starsLabel" to ("Stars"),
        "quizAccuracy" to ("Quiz Accuracy"),
        "ageGroupLabel" to ("Age group"),
        "parentModeTitle" to ("Parent Mode"),
        "parentModeActiveDesc" to ("Quiz gates are paused, full device is unlocked for parent."),
        "parentModeInactiveDesc" to ("Turn on when parent borrows the device or needs to pause restrictions."),
        "startParentMode" to ("Start Parent Mode"),
        "stopParentMode" to ("Lock Device Now"),
        "emergencyQuick30m" to ("Pause 30m"),
        "emergencyQuick1h" to ("Pause 1h"),
        "emergencyQuick2h" to ("Pause 2h"),
        "testGateButton" to ("Test QuizLock"),
        "systemStatusRunning" to ("Protection system is active"),
        "systemStatusAttention" to ("Requires system permissions check"),
        "band4to6Short" to ("Ages 4–6"),
        "band7to9Short" to ("Ages 7–9"),
        "band10to12Short" to ("Ages 10–12"),
        "profilesTitle" to ("Child Profiles"),
        "profilesSubtitle" to ("Tap a child profile to customize strategy, quiz gates and allowed hours for that child."),
        "addProfileFab" to ("Add Child"),
        "addProfileTitle" to ("Add New Child Profile"),
        "editProfileTitle" to ("Customize Child Profile"),
        "childNameLabel" to ("Child's Name"),
        "childNamePlaceholder" to ("e.g. Oliver, Emma..."),
        "childAgeLabel" to ("Child's Age"),
        "ageUnit" to ("years old"),
        "ageBand4to6" to ("Ages 4–6: Counting, animals & recognition"),
        "ageBand7to9" to ("Ages 7–9: Times tables & vocabulary"),
        "ageBand10to12" to ("Ages 10–12: Fractions & logic puzzles"),
        "avatarLabel" to ("Choose Avatar"),
        "customBadge" to ("Custom"),
        "dailyCapLabel" to ("Maximum Daily Watch Time (Cap)"),
        "dailyCapShortLabel" to ("Daily cap"),
        "gateShortLabel" to ("Gate"),
        "customRulesTitle" to ("Profile Custom Rules"),
        "customRulesSubtitle" to ("Customize gate intervals, daily limits, and policies independently for this child."),
        "customRulesToggle" to ("Enable custom rules for this profile"),
        "inheritGlobalRulesNote" to ("Currently inheriting global rules from Settings."),
        "customRulesActiveNote" to ("Custom rules are active and overriding global settings for this child."),
        "strategyTitle" to ("Control Strategy"),
        "strategyDailyCap" to ("Daily Cap (Recommended)"),
        "strategyDailyCapDesc" to ("Locks device upon reaching daily cap. Prompts a quiz every X minutes."),
        "strategyInfinite" to ("Infinite (No daily cap)"),
        "strategyInfiniteDesc" to ("Every X minutes, child solves 1 quiz to continue watching. No daily limit."),
        "strategyEscalating" to ("Escalating (Progressive challenge)"),
        "strategyEscalatingDesc" to ("Each subsequent gate requires solving more quizzes (1 -> 2 -> 3...) to continue."),
        "gateIntervalTitle" to ("Gate Interval"),
        "gateIntervalSubtitle" to ("Screen pauses to trigger a quiz every"),
        "allowedHoursTitle" to ("Allowed Viewing Hours"),
        "startTimeLabel" to ("Start Time"),
        "endTimeLabel" to ("End Time"),
        "setAsActiveChild" to ("Set as active profile"),
        "deleteProfileTitle" to ("Delete Profile"),
        "deleteProfileConfirmMsg" to ("Are you sure you want to delete this profile? Quiz attempt logs will remain archived."),
        "cannotDeleteOnlyProfile" to ("Cannot delete the only existing profile."),
        "profileSavedSuccess" to ("Child profile settings saved successfully!"),
        "policyDailyCapTitle" to ("Daily Cap (Default)"),
        "policyDailyCapDesc" to ("Quizzes earn extra minutes within the daily cap. Reaching the cap locks the device."),
        "policyInfiniteTitle" to ("Infinite"),
        "policyInfiniteDesc" to ("Each correct answer grants +X more minutes, with no total limit."),
        "policyEscalatingTitle" to ("Escalating cost"),
        "policyEscalatingDesc" to ("Each next gate requires more answers (1 -> 2 -> 3, up to 5)."),
        "gateIntervalXTitle" to ("Gate interval (X): %d minutes"),
        "gateIntervalXDesc" to ("A quiz gate appears after every %d minutes of screen time."),
        "dailyCapLimitTitle" to ("Maximum daily screen time: %d minutes"),
        "allowedHoursCardTitle" to ("Allowed viewing hours"),
        "allowedHoursCardDesc" to ("Outside these hours, the app locks the device automatically and says good night."),
        "quickPresetsLabel" to ("Quick presets:"),
        "presetAllDay" to ("All day"),
        "presetAfterSchool" to ("After school"),
        "presetEvening" to ("Relaxing evening"),
        "presetWeekend" to ("Weekend"),
        "startAllowedUpper" to ("ALLOWED FROM"),
        "endBedtimeUpper" to ("BEDTIME"),
        "tapToEdit" to ("Tap to edit"),
        "timeInvalidFormat" to ("Invalid time format (use HH:mm)"),
        "timeStartBeforeEnd" to ("Start time (%s) must be earlier than end time (%s). Allowed hours must stay within one day."),
        "timeWindowTooShort" to ("The allowed viewing window should be at least 30 minutes."),
        "timeWindowValid" to ("Daily viewing window: %s. The device locks automatically after %s for bedtime."),
        "timeValid" to ("Valid"),
        "startTimePickerTitle" to ("Allowed Start Time"),
        "endTimePickerTitle" to ("Bedtime Start"),
        "toLabel" to ("to"),
        "settingsTitle" to ("Settings & Administration"),
        "tabPolicy" to ("Policy"),
        "tabQuestions" to ("Questions"),
        "tabExclusions" to ("Exclusions"),
        "tabSecurity" to ("Security"),
        "globalPolicyDesc" to ("Default policy applied across all devices when a profile doesn't enable custom rules."),
        "questionsStatTitle" to ("Question Bank Statistics"),
        "importQuestionsUrlTitle" to ("Update Question Bank via URL"),
        "importQuestionsUrlPlaceholder" to ("https://example.com/questions.json"),
        "importQuestionsButton" to ("Download & Import"),
        "restoreStarterQuestions" to ("Restore Starter Question Pack"),
        "exclusionsDesc" to ("Educational or emergency calling apps that are never blocked by QuizLock."),
        "exclusionsCardTitle" to ("Excluded apps (not counted)"),
        "exclusionsCardDesc" to ("Enable for learning, online-class or reading apps so they never count against screen time."),
        "searchAppsPlaceholder" to ("Search applications..."),
        "searchAppsHint" to ("Search apps by name..."),
        "appsLoading" to ("Loading installed apps..."),
        "noAppsFound" to ("No matching apps found."),
        "changePinTitle" to ("Change Parent PIN"),
        "changePinDesc" to ("Change the 6-digit PIN used to access settings and verify parent identity."),
        "pinSecurityTitle" to ("PIN & Security"),
        "pinSecurityDesc" to ("The 6-digit PIN is used to access settings, pause monitoring, or emergency unlock."),
        "changePinButton" to ("Change Parent PIN"),
        "currentPinLabel" to ("Current PIN"),
        "newPinLabel" to ("New 6-digit PIN"),
        "confirmNewPinLabel" to ("Confirm New PIN"),
        "recoveryCodeTitle" to ("Emergency Recovery Code"),
        "recoveryCodeDesc" to ("Use this recovery code in case you ever forget your parent PIN."),
        "legalPrivacyCardTitle" to ("Legal & Privacy Policy"),
        "legalPrivacyCardDesc" to ("View Terms of Service and 100% on-device child privacy protection guarantees."),
        "systemPermissionsTitle" to ("Check system permissions"),
        "systemPermissionsDesc" to ("If background monitoring stops, re-enable Accessibility:"),
        "accessibilityPermissionButton" to ("1. Accessibility Service"),
        "viewTosPrivacyButton" to ("View Terms & Privacy Policy"),
        "verifyCurrentPinTitle" to ("Confirm current PIN"),
        "verifyCurrentPinSubtitle" to ("Enter the current PIN to verify parent identity"),
        "setNewPinTitle" to ("Set a new PIN"),
        "pinNewInputLabel" to ("New PIN (6 digits)"),
        "pinNewConfirmInputLabel" to ("Re-enter new PIN"),
        "pinMustBe6Digits" to ("PIN must be exactly 6 digits."),
        "pinMismatch" to ("PIN confirmation does not match."),
        "saveNewPin" to ("Save New PIN"),
        "onboardingWelcomeTitle" to ("Welcome to QuizLock!"),
        "onboardingWelcomeSubtitle" to ("A smart parental control tool helping balance screen time with educational quiz gates."),
        "onboardingStartButton" to ("Start Setup (5 min)"),
        "onboardingBullet1" to ("Automatically pauses screen after X minutes for an educational quiz."),
        "onboardingBullet2" to ("200+ fun Math, Language & Logic questions tailored to child's age."),
        "onboardingBullet3" to ("100% on-device, no cloud servers, strict family privacy."),
        "onboardingPinTitle" to ("Create Parent PIN"),
        "onboardingPinSubtitle" to ("A 6-digit PIN used to open settings, adjust time, or emergency unlock."),
        "onboardingPinLabel" to ("Enter 6-digit PIN"),
        "onboardingConfirmPinLabel" to ("Confirm 6-digit PIN"),
        "onboardingGeneratePinButton" to ("Create PIN & Generate Recovery Code"),
        "onboardingRecoveryCodeNotice" to ("I have safely written down my recovery code"),
        "onboardingProfileTitle" to ("Child Profile & Rules"),
        "onboardingProfileSubtitle" to ("Set up initial child profile, control policy, and quiz interval."),
        "onboardingReadyTitle" to ("All Set & Ready!"),
        "onboardingReadySubtitle" to ("QuizLock is ready to protect screen time.\nYou can customize hours and app exclusions in Settings anytime."),
        "onboardingFinishButton" to ("Enter Parent Dashboard"),
        "onboardingPermissionsTitle" to ("Required monitoring permissions"),
        "onboardingPermissionsSubtitle" to ("To count screen time and show quiz gates over YouTube/Games, the app needs these permissions:"),
        "onboardingPermissionAccessibilityTitle" to ("1. Accessibility Service"),
        "onboardingPermissionAccessibilityDesc" to ("Required: track the foreground app and open quiz gates."),
        "onboardingPermissionBatteryTitle" to ("2. Ignore battery optimization"),
        "onboardingPermissionBatteryDesc" to ("Keeps the monitoring service from being killed by the system."),
        "onboardingPermissionNotificationsTitle" to ("3. Status notifications"),
        "onboardingPermissionNotificationsDesc" to ("Shows the persistent notification that protection is running."),
        "onboardingRecoveryCodeUpper" to ("YOUR RECOVERY CODE"),
        "onboardingRecoveryCodeNote" to ("Write down or screenshot this code. Use it to reset your PIN if you ever forget it."),
        "onboardingChildNameLabel" to ("Child's name (e.g. Oliver, Emma)"),
        "onboardingPauseEvery" to ("Pause the screen every: %d minutes"),
        "onboardingDailyCapLabel" to ("Maximum daily screen time (Daily cap): %s (%d minutes)"),
        "pinMinLengthError" to ("PIN must be at least 6 digits."),
        "appLockTitle" to ("App locked"),
        "appLockSubtitle" to ("Enter the parent PIN to open management."),
        "appLockDevSuperPin" to ("Dev Super PIN"),
        "pinPadDefaultTitle" to ("Parent PIN verification"),
        "pinPadDefaultSubtitle" to ("Enter your 6-digit PIN to continue"),
        "pinRecoveryTitle" to ("Recover PIN"),
        "pinLockedOut" to ("Too many incorrect attempts. Temporarily locked.\nPlease wait %d seconds."),
        "pinWrongAttempts" to ("Incorrect PIN. %d attempts remaining."),
        "pinFailLockout" to ("5 incorrect attempts. Locked for 5 minutes!"),
        "pinRecoveryPrompt" to ("Enter the 8-character recovery code created during setup."),
        "pinRecoveryCodeLabel" to ("Recovery code (8 characters)"),
        "pinConfirmRecovery" to ("Confirm Recovery Code"),
        "pinRecoveryInvalid" to ("Invalid recovery code."),
        "pinBackToPin" to ("Back to PIN entry"),
        "pinForgot" to ("Forgot PIN? Use recovery code"),
        "pinLockIconDesc" to ("Lock"),
        "deleteDigitDesc" to ("Delete one digit"),
        "hourRangeLabel" to ("Hour (00 - 23)"),
        "minuteRangeLabel" to ("Minute (00 - 59)"),
        "selectTime" to ("Select %s"),
        "decreaseHour" to ("Decrease 1 hour"),
        "increaseHour" to ("Increase 1 hour"),
        "decreaseMinutes" to ("Decrease 5 minutes"),
        "increaseMinutes" to ("Increase 5 minutes"),
        "tosTitle" to ("Terms & Privacy"),
        "privacyTabTitle" to ("Privacy Policy"),
        "termsTabTitle" to ("Terms of Service"),
        "tosCommitmentSubtitle" to ("Dedicated to absolute safety and privacy for kids"),
        "privacyHeroTitle" to ("100% On-device & Private"),
        "privacyHeroSubtitle" to ("No servers, no personal data collection, no ads."),
        "privacySection1Title" to ("1. Data collection & storage"),
        "privacySection1Body" to ("QuizLock follows an On-device First principle:\n" +
        "• All child profiles, screen time and quiz history are stored locally in an SQLite (Room) database on your device.\n" +
        "• The app does NOT transmit any personally identifying information (name, age, learning results) to third-party servers or the cloud.\n" +
        "• No tracking cookies, Google Analytics or commercial advertising SDKs are used."),
        "privacySection2Title" to ("2. Accessibility transparency"),
        "privacySection2Body" to ("The QuizGateAccessibilityService accessibility service is used for one purpose only:\n" +
        "• Detect the package name of the foreground app to count time spent in entertainment apps (such as YouTube or games).\n" +
        "• The app NEVER reads screen content, NEVER records keystrokes, and NEVER collects passwords, messages or banking information."),
        "privacySection3Title" to ("3. PIN & recovery code security"),
        "privacySection3Body" to ("• The parent PIN is one-way hashed with the industry-standard PBKDF2-HMAC-SHA256 algorithm using 150,000 iterations and a random salt.\n" +
        "• No one, not even the developer, can reverse the PIN from storage.\n" +
        "• An 8-character recovery code is generated locally so parents can reset the PIN without relying on email or cloud services."),
        "privacySection4Title" to ("4. Right to delete data"),
        "privacySection4Body" to ("Parents have full control over their data:\n" +
        "• You can delete a child profile and reset quiz history directly in the app.\n" +
        "• Uninstalling QuizLock permanently removes 100% of the related data from the device."),
        "tosHeroTitle" to ("QuizLock Terms of Use"),
        "tosHeroSubtitle" to ("A companion tool for parents building healthy digital habits"),
        "tosSection1Title" to ("1. Purpose of the app"),
        "tosSection1Body" to ("QuizLock is designed as an educational tool that combines parental control and screen-time gating. Instead of strict prohibition, the app encourages children to practise math, language and logic in exchange for positive entertainment time."),
        "tosSection2Title" to ("2. Parent responsibilities"),
        "tosSection2Body" to ("• Parents are responsible for keeping the 6-digit PIN secure and storing the recovery code safely.\n" +
        "• Parents should discuss viewing limits and quiz strategies with their child to build family consensus.\n" +
        "• Do not share the PIN with children so the screen-time protection works effectively."),
        "tosSection3Title" to ("3. Limitation of liability"),
        "tosSection3Body" to ("• QuizLock is a family assistance tool provided in good faith. It does not fully replace direct parental supervision.\n" +
        "• Question content is compiled to suit Vietnamese primary and preschool curricula. Parents may customise or import additional questions for their own learning needs."),
        "gateQuizWith" to ("Quiz time with"),
        "gateSubmit" to ("Submit Answer"),
        "gateTryAgain" to ("Not quite! Try again"),
        "gateCorrect" to ("Correct! Great job 🎉"),
        "gateHint" to ("Hint"),
        "gateEmergencyPin" to ("Parent Unlock"),
        "gateContinueSubtitle" to ("Answer correctly to keep watching!"),
        "gateFallbackChildName" to ("Child"),
        "gateParentShort" to ("Parent"),
        "gateTopicFallback" to ("Math"),
        "gateHintPrefix" to ("Hint: %s"),
        "gateWrongFirst" to ("Not quite! Try once more."),
        "gateWrongSecond" to ("Read the hint above to find the right answer!"),
        "gateVictoryTitle" to ("Wonderful!"),
        "gateVictorySubtitle" to ("You answered correctly!"),
        "gateVictoryDefaultExplanation" to ("Congratulations, you got it right!"),
        "gateReturning" to ("Returning to your app..."),
        "gateParentModalTitle" to ("Unlock QuizLock"),
        "gateParentModalSubtitle" to ("Enter the PIN to dismiss the gate or start Parent Mode"),
        "gateUnlockDesc" to ("Parent unlock"),
        "gateReadQuestionDesc" to ("Read the question aloud"),
        "gateVictoryDesc" to ("Victory"),
        "gateStarDesc" to ("Star"),
        "hardBlockBedtimeTitle" to ("It's Bedtime!"),
        "hardBlockBedtimeSubtitle" to ("The device is resting until 06:00 tomorrow.\nSleep well and sweet dreams!"),
        "hardBlockDailyCapTitle" to ("Today's Screen Time Is Over!"),
        "hardBlockDailyCapSubtitle" to ("You've reached today's screen time limit.\nGo move around or read a book!"),
        "hardBlockParentButton" to ("Parent (Unlock with PIN)"),
        "hardBlockPinTitle" to ("Confirm Unlock"),
        "hardBlockPinSubtitle" to ("Enter the PIN to start Parent Mode (4 hours) or remove the block"),
        "hardBlockDesc" to ("Blocked"),
        "unlockDesc" to ("Unlock"),
        "questionBankTitle" to ("Current Question Bank"),
        "questionCountSuffix" to ("%d questions"),
        "questionBankStatsDesc" to ("Questions grouped by your child's age band:"),
        "bandGroup4to6" to ("Ages 4–6"),
        "bandGroup7to9" to ("Ages 7–9"),
        "bandGroup10to12" to ("Ages 10–12"),
        "updateFromUrlTitle" to ("Update from URL (JSON)"),
        "updateFromUrlDesc" to ("Enter a direct HTTPS link to a question JSON file. It will be downloaded and merged into the local database without losing progress."),
        "urlFieldLabel" to ("URL of the question JSON file"),
        "fillSampleLink" to ("Fill sample link"),
        "invalidUrlMessage" to ("Please enter a valid HTTPS URL starting with https://."),
        "importSuccess" to ("Loaded %d questions into the bank!"),
        "importFailed" to ("Update failed: %s"),
        "networkError" to ("Network error"),
        "loadingData" to ("Loading data..."),
        "downloadUpdateButton" to ("Download & Update Question Bank"),
        "restoreDataTitle" to ("Restore Default Data"),
        "restoreDataDesc" to ("Restore the built-in 200-question starter pack (Math, Language, Logic)."),
        "restoreSuccess" to ("Restored %d default questions!"),
        "restoreButton" to ("Restore the 200 starter questions"),
        "jsonFormatTitle" to ("Advanced format (JSON / URL)"),
        "collapse" to ("Collapse"),
        "showSample" to ("Show sample"),
        "excelImportTitle" to ("Import questions from Excel"),
        "excelImportDesc" to ("Easiest option: open Excel or Google Sheets, paste the template, then enter one question per row. Question, at least 2 options and the correct answer are required. .xlsx, .csv, .tsv and tab-separated CSV are supported."),
        "excelImportFormatHint" to ("Recommended columns: Question | Answer A | Answer B | Answer C | Answer D | Correct answer | Explanation | Hint. Choose age band and language below; they do not need to be in the file."),
        "excelImportButton" to ("Choose Excel / CSV / TSV file"),
        "excelImportAgeBandLabel" to ("Apply to age band"),
        "excelImportLanguageLabel" to ("Content language"),
        "excelImportTemplateCopy" to ("Copy a template for Excel / Sheets"),
        "excelImportTemplateCopied" to ("Template copied. Paste it into Excel or Google Sheets, then enter your questions."),
        "excelImportSuccess" to ("Imported %d questions (skipped %d invalid rows, %d duplicates)."),
        "excelImportErrorEmpty" to ("The file is empty."),
        "excelImportErrorOldFormat" to ("Legacy .xls files aren't supported. Save as .xlsx or .csv."),
        "excelImportErrorUnreadable" to ("Could not read the file. Check the .xlsx/.csv format."),
        "excelImportErrorNoQuestions" to ("No valid questions found in the file."),
        "deleteAllQuestionsTitle" to ("Delete the entire question bank"),
        "deleteAllQuestionsDesc" to ("Deletes all imported and bundled questions. Quiz gates will use automatically generated questions until you import again."),
        "deleteAllQuestionsButton" to ("Delete all questions"),
        "deleteAllQuestionsConfirmTitle" to ("Delete everything?"),
        "deleteAllQuestionsConfirmMsg" to ("This cannot be undone. The whole question bank and its seen-history will be removed."),
        "deleteAllQuestionsAction" to ("Delete all"),
        "deleteAllQuestionsSuccess" to ("Deleted %d questions. Quiz gates will use generated questions."),
        "deleteAllQuestionsError" to ("Could not delete the question bank."),
        "notifBrokenTitle" to ("Permission needed"),
        "notifBrokenContent" to ("Accessibility is off — open QuizLock to fix it."),
        "notifNoProfile" to ("No child profile yet"),
        "notifWatchingTitle" to ("Monitoring screen time"),
        "notifProtectionActive" to ("Protection active"),
        "notifParentModeRemaining" to ("Parent Mode: %d minutes left"),
        "notifWatchProgress" to ("%s: Watched %d min / %d min"),
        "notifTitle" to ("QuizLock is protecting"),
    )
)

object AppStrings {
    fun of(language: AppLanguage): Strings = when (language) {
        AppLanguage.EN -> EnStrings
        AppLanguage.VI -> ViStrings
    }

    fun fromCode(code: String?): Strings = of(AppLanguage.fromCode(code))
}

fun Strings.ageBandShortLabel(ageBand: String?): String = when (ageBand) {
    "4-6" -> band4to6Short
    "7-9" -> band7to9Short
    else -> band10to12Short
}

val LocalStrings = staticCompositionLocalOf { ViStrings }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.VI }
