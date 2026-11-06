# Quiz Gate

[Tiếng Việt](README.md) · [English](README.en.md)

> **Câu đố đổi giờ xem — biến thời gian dùng thiết bị thành vừa học vừa chơi.**

Quiz Gate là ứng dụng Android dành cho phụ huynh muốn quản lý thời gian sử dụng TV / tablet / smartphone của con **mà không cần cấm đoán**. Thay vì khóa máy, ứng dụng biến mỗi khoảng thời gian xem thành một “cổng” nhỏ: con trả lời đúng một câu hỏi Toán, Tiếng Việt hoặc Logic để mở khóa và tiếp tục xem.

Ứng dụng chạy hoàn toàn **on-device**, không backend, không tài khoản, không quảng cáo. Mục tiêu là một công cụ nhẹ, riêng tư và đủ đơn giản để phụ huynh Việt Nam dùng được trên cả TV box giá rẻ lẫn điện thoại phổ thông.

---

## 1. Ý tưởng

Trẻ em thường bị cắt ngang cảm xúc khi bị tắt thiết bị đột ngột. Quiz Gate chọn cách khác:

- **Không cấm** — con vẫn được xem, nhưng phải “trả phí” bằng một câu trả lời đúng.
- **Không học thêm nặng nề** — câu hỏi bám theo độ tuổi, có gợi ý và lời giải ngắn.
- **Không xâm phạm quyền riêng tư** — dữ liệu trẻ ở lại trên máy, không gửi lên cloud.
- **Không biến thiết bị thành “cục gạch”** — luôn có lối thoát bằng PIN phụ huynh và câu hỏi dự phòng sinh tự động.

---

## 2. Cách hoạt động

```
┌─────────────────────┐
│ Trẻ mở YouTube/game │
└──────────┬──────────┘
           │  AccessibilityService theo dõi app đang hiển thị
           ▼
┌──────────────────────┐        chưa tới ngưỡng
│ Đếm thời gian đã xem │ ─────────────────────────┐
└──────────┬───────────┘                          │
           │ đủ X phút / hết trần ngày / ngoài giờ │
           ▼                                       │
┌──────────────────────┐                          │
│   Mở Quiz Gate       │ ◄────────────────────────┘
│ (câu hỏi + 4 đáp án) │
└──────────┬───────────┘
           │ trả lời đúng
           ▼
┌──────────────────────┐
│ Mở khóa + ghi nhận   │ ──► trẻ quay lại app đang xem
│ công, đóng sau ~2s   │
└──────────────────────┘
```

1. Phụ huynh cài đặt, cấp quyền, tạo PIN và hồ sơ cho con.
2. Một service nền theo dõi app đang ở foreground và cộng dồn thời gian xem (bỏ qua app nằm trong danh sách loại trừ).
3. Khi chạm ngưỡng, một màn hình câu đố toàn màn hình được mở phía trên app.
4. Trả lời đúng → cổng đóng, con xem tiếp. Trả lời sai → thử lại, sau 2 lần hiện gợi ý, sau 3 lần đổi câu mới.
5. Phụ huynh luôn có thể mở khóa bằng PIN hoặc bật **Parent Mode** khi cần dùng máy.

---

## 3. Tính năng

### Cổng câu đố
- Câu hỏi trắc nghiệm 4 đáp án, lấy ngẫu nhiên theo nhóm tuổi của hồ sơ.
- Chống lặp: không lặp lại câu đã gặp trong **14 ngày**.
- Gợi ý sau 2 lần sai; tự đổi câu mới sau lần sai thứ 3 để trẻ không bị kẹt.
- Màn “chiến thắng” ngắn kèm lời giải, tự đóng sau khoảng 2 giây.
- Hỗ trợ D-pad / remote TV: phím số **1–4** chọn đáp án, các nút đều focusable.
- Nút đọc câu hỏi bằng TTS; giọng đọc đổi theo ngôn ngữ ứng dụng.

### Ba chiến lược kiểm soát
| Chiến lược | Cách hoạt động |
|---|---|
| **Trần ngày** (mặc định) | Đặt tổng thời gian xem/ngày. Hết trần → khóa cứng, chỉ PIN mở. |
| **Vô hạn** | Cứ mỗi X phút phải giải một câu để xem tiếp, không giới hạn tổng. |
| **Tăng dần** | Mỗi cổng tiếp theo cần nhiều câu đúng hơn (1 → 2 → 3…). |

### Hồ sơ trẻ
- Nhiều hồ sơ, mỗi bé có tên, tuổi, nhóm tuổi và giới hạn riêng.
- **Bấm vào một hồ sơ** trong màn Quản lý để mở màn **Cài đặt riêng của bé**: bật/tắt kế thừa cài đặt chung, chọn chiến lược, khoảng cách cổng, trần ngày và khung giờ.
- Icon bút chì trên card dùng để sửa tên và độ tuổi.
- Khung giờ được phép xem riêng; ngoài khung giờ hiển thị màn “đi ngủ”.

### Quản lý thời gian
- Khoảng cách cổng X (phút), hạn mức ngày, khung giờ bắt đầu/kết thúc.
- Preset nhanh: cả ngày, sau giờ học, tối thư giãn, cuối tuần.
- Danh sách **ứng dụng loại trừ**: app học tập, đọc sách, lớp online… không bị đếm giờ và không bị chặn.
- **Parent Mode**: tạm dừng cổng theo 1 / 2 / 4 / 8 giờ, không đếm vào giờ của trẻ.

### Chạy nền & độ tin cậy
- Watchdog **hướng sự kiện**: đánh giá khi đổi app, bật/tắt màn hình, đóng cổng hoặc đổi cấu hình; chỉ đánh thức tại biên cổng/trần/khung giờ kế tiếp thay vì poll mỗi 2 giây.
- Thời gian xem được gom trong bộ nhớ và ghi DB mỗi 30 giây.
- Tự khởi động lại sau reboot / cập nhật app; có cảnh báo BROKEN khi mất quyền Trợ năng.
- Thử khởi động lại khi người dùng vuốt app khỏi recent (AlarmManager dự phòng), nhưng vẫn phụ thuộc hành vi OEM và thao tác force-stop.
- Android 15/16 có thể chặn FGS `specialUse` khởi động từ `BOOT_COMPLETED`; khi đó watchdog vẫn chạy trong AccessibilityService và thử khởi động FGS từ service này.

### Bảo mật & chống nghịch
- PIN phụ huynh 6 số, lưu dưới dạng hash **PBKDF2-HMAC-SHA256, 150.000 vòng + salt ngẫu nhiên**.
- Sai 5 lần → khóa thử 5 phút.
- Mã khôi phục 8 ký tự, chỉ hiển thị một lần khi tạo, dùng để đặt lại PIN.
- **Khóa ứng dụng**: khi đã thiết lập PIN, toàn bộ màn hình quản lý sẽ khóa lúc mở app và khi quay lại từ nền; mở bằng PIN hoặc mã khôi phục.
- **Super PIN (chỉ bản debug)**: nhập `999999` tại màn khóa để vào phần quản lý mà không cần PIN đã thiết lập, đồng thời bỏ qua lockout. Release build hoàn toàn bỏ qua Super PIN.
- **Cảnh báo BROKEN**: service nền kiểm tra quyền mỗi phút; nếu Trợ năng bị tắt, notification đổi sang trạng thái “Cần kiểm tra quyền” kèm nút mở thẳng cài đặt Trợ năng.
- Device Admin receiver + Boot receiver để dịch vụ sống lại sau khi khởi động máy.
- Thông báo nền thường trực hiển thị trạng thái bảo vệ.

### Ngân hàng câu hỏi
- Đóng gói sẵn **200 câu hỏi** tiếng Việt theo 3 nhóm tuổi trong APK.
- Cập nhật thêm từ URL JSON trực tiếp hoặc nhập từ file Excel/CSV trong Cài đặt.
- Chống trùng theo nội dung: câu hỏi trùng nội dung + đáp án + nhóm tuổi + ngôn ngữ sẽ bị bỏ qua khi nhập.
- Có nút **xóa toàn bộ ngân hàng câu hỏi** (có xác nhận) trong Cài đặt.
- Khi hết câu hoặc mất dữ liệu, **bộ sinh câu hỏi dự phòng** tự tạo phép Toán / Logic theo nhóm tuổi — cổng không bao giờ “chết”.

### Song ngữ Việt – Anh
- Toàn bộ giao diện, thông báo, hộp thoại PIN, màn chặn và nội dung pháp lý hỗ trợ **Tiếng Việt / English**.
- Đổi ngôn ngữ trực tiếp trong Dashboard, Onboarding hoặc Cài đặt; lựa chọn được lưu trong CSDL.
- Tài nguyên hệ thống (mô tả Accessibility, notification channel, toast Device Admin) có bản dịch trong `values-en/`.

---

## 4. Kiến trúc & công nghệ

Dự án hiện là **một module `:app`** (chưa tách multi-module như bản thiết kế ban đầu), ưu tiên độ đơn giản và kích thước APK nhỏ.

| Thành phần | Lựa chọn |
|---|---|
| Ngôn ngữ | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 |
| Lưu trữ | Room (SQLite), không DataStore |
| Theo dõi foreground | `AccessibilityService` |
| Dịch vụ nền | Foreground Service + notification thường trực |
| Chống gỡ / khởi động lại | Device Admin receiver + Boot receiver |
| Mạng | OkHttp (chỉ khi phụ huynh chủ động nhập URL câu hỏi) |
| Đa ngôn ngữ | `Strings` + `LocalStrings` / `LocalAppLanguage` trong Compose |
| Build | AGP 9.1.1 · Gradle 9.3.1 · minSdk 24 · targetSdk 36 |

### Cấu trúc thư mục

```
app/src/main/java/com/quizlock/
├── MainActivity.kt                 # Điều hướng chính + bottom nav
├── QuizGateApplication.kt         # Khởi tạo DB, seed dữ liệu, start service
├── core/
│   ├── data/                      # Room entities, DAO, repository, fallback generator
│   ├── i18n/                      # Bộ chuỗi VI/EN và CompositionLocal
│   └── policy/                    # PolicyEngine: quyết định mở cổng / chặn cứng
├── service/                       # AccessibilityService + ForegroundService
├── receiver/                      # BootReceiver, DeviceAdminReceiver
└── ui/
    ├── parent/                    # Dashboard, Profiles, Settings, Onboarding, TOS
    ├── gate/                      # QuizGateActivity, HardBlockActivity
    ├── components/                # PIN pad, Time picker
    └── theme/                     # Màu, typography
```

### Luồng dữ liệu

`AccessibilityService` → `QuizGateRepository` (đọc hồ sơ, config, sổ ngày) → `PolicyEngine` (quyết định thuần) → mở `QuizGateActivity` / `HardBlockActivity` → ghi `attempt_log`, `day_ledger`, `question_seen`.

---

## 5. Cài đặt & chạy

### 5.1 Yêu cầu hệ thống

| Thành phần | Phiên bản / ghi chú |
|---|---|
| JDK | 17 hoặc 21 |
| Android SDK Platform | **36.1** (`compileSdk`), build-tools 36.1.0 |
| Gradle | **9.3.1** (AGP 9.1.1 yêu cầu tối thiểu phiên bản này) |
| Android Studio | Khuyến nghị, bản hỗ trợ AGP 9.x |
| Thiết bị / emulator | Android 7.0 (API 24) trở lên |

> Ứng dụng không cần backend hay Firebase. Thiếu `google-services.json` chỉ tạo cảnh báo, không chặn build.

### 5.2 Chuẩn bị project

```bash
git clone <repo-url> QuizLock
cd QuizLock
```

Tạo `local.properties` nếu Android Studio chưa tạo:

```properties
sdk.dir=/duong/dan/Android/Sdk
```

Hoặc đặt biến môi trường `ANDROID_HOME`.

Repo có `gradle/wrapper/gradle-wrapper.properties` nhưng có thể chưa kèm script `gradlew`. Nếu thiếu:

```bash
gradle wrapper --gradle-version 9.3.1
```

Hoặc mở project bằng Android Studio và để IDE sync/ tải Gradle.

### 5.3 Tạo debug keystore

Cấu hình build debug trỏ tới `debug.keystore` ở thư mục gốc:

```bash
keytool -genkeypair -v -keystore debug.keystore \
  -storepass android -alias androiddebugkey -keypass android \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Android Debug,O=Android,C=US"
```

Build release dùng biến môi trường:

```bash
export KEYSTORE_PATH=/duong/dan/upload-key.jks
export STORE_PASSWORD=mat-khau-keystore
export KEY_PASSWORD=mat-khau-key
```

### 5.4 Build & cài lên thiết bị

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

- APK debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `./gradlew :app:assembleRelease` → `app/build/outputs/apk/release/`
- Nếu cài đè báo lỗi chữ ký: `adb uninstall com.aistudio.quizgate.app` rồi cài lại.
- **Android TV / TV box** (bật ADB debugging trong Developer options):

```bash
adb connect <ip-cua-tv>:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 5.5 Setup lần đầu trong ứng dụng

1. Mở app → chọn ngôn ngữ **Tiếng Việt / English**.
2. Cấp **Accessibility Service** (bắt buộc) — cơ chế chính để biết app nào đang mở.
3. Cấp **Bỏ qua tối ưu hóa pin** và **quyền Thông báo** (Android 13+) để thấy trạng thái bảo vệ.
4. (Khuyến nghị) Bật **Device Admin** để bước gỡ app phải qua cảnh báo.
5. Tạo **PIN phụ huynh 6 số** và lưu lại **mã khôi phục 8 ký tự**. Từ lần mở sau, màn hình quản lý sẽ yêu cầu PIN này.
6. Tạo hồ sơ đầu tiên: tên, tuổi, chiến lược, khoảng cách cổng X và trần ngày.
7. Vào **Cài đặt → Loại trừ** để thêm app học tập / đọc sách không bị đếm giờ.
8. Vào **Hồ sơ bé**, bấm vào một hồ sơ để mở **Cài đặt riêng** và bật “Tùy chỉnh riêng cho bé”.
9. Kiểm tra nhanh bằng nút **Thử nghiệm cổng câu đố** trên Dashboard.

> **Super PIN cho dev:** trong bản **debug**, nhập `999999` ở màn khóa để vào quản lý mà không cần PIN phụ huynh (kể cả khi đang bị lockout). Bản release bỏ qua giá trị này. Đổi giá trị tại `QuizGateRepository.SUPER_PIN`.

#### Ghi chú theo thiết bị

- **Xiaomi / Redmi (MIUI)**: bật Autostart và đặt pin “No restrictions” cho Quiz Gate.
- **Samsung / One UI**: thêm app vào “Never sleeping apps” nếu service bị dừng.
- **Android TV / TV box**: một số máy ẩn màn hình cấp quyền; dùng remote hoặc ADB để mở Settings. Kiểm tra lại Accessibility sau khi reboot.
- **Android 13+**: cấp quyền thông báo để thấy trạng thái bảo vệ.

### 5.6 Kiểm thử

```bash
./gradlew :app:testDebugUnitTest
```

### 5.7 Xử lý sự cố thường gặp

| Lỗi | Cách xử lý |
|---|---|
| `Minimum supported Gradle version is 9.3.1` | Dùng đúng Gradle 9.3.1 (wrapper hoặc Android Studio). |
| `Keystore file ... debug.keystore not found` | Tạo keystore theo mục 5.3. |
| `google-services.json is missing` | Chỉ là cảnh báo; app không cần Firebase. |
| Thiếu `SDK Platform 36.1` | Cài qua SDK Manager hoặc để Gradle tự tải. |
| Service ngừng sau reboot | Kiểm tra Accessibility, battery optimization và autostart của OEM. |
| Không đếm giờ | Kiểm tra Accessibility đang bật, app không nằm trong danh sách loại trừ và đang trong khung giờ cho phép. |
| Quên PIN / cần vào nhanh khi dev | Dùng **mã khôi phục** ở màn khóa; bản debug có thể dùng Super PIN `999999`. |

### 5.8 Checklist kiểm chứng trên thiết bị thật

- [ ] Reboot máy → Accessibility vẫn bật → watchdog hoạt động lại trong ≤60 giây.
- [ ] Vuốt app khỏi recent → service tự khởi động lại (AlarmManager dự phòng, ~5 giây hoặc khi mở lại app).
- [ ] **MIUI/Redmi**: bật Autostart + “No restrictions”; khóa màn hình 10 phút rồi mở YouTube → cổng xuất hiện đúng ngưỡng X.
- [ ] **One UI/Samsung**: thêm vào “Never sleeping apps”; để qua đêm và kiểm tra service còn sống.
- [ ] **Android 15/16**: sau reboot kiểm tra notification; nếu FGS `specialUse` bị chặn, watchdog vẫn chạy nhờ Accessibility — mở app để khởi động lại notification.
- [ ] **Force-stop**: xác nhận giới hạn của Android — app không tự bật lại cho tới khi người dùng mở lại.

---

## 6. Quyền cần cấp

| Quyền | Vai trò | Cách cấp |
|---|---|---|
| Accessibility Service | **Bắt buộc** — biết app nào đang foreground để đếm giờ và mở cổng | Cài đặt → Trợ năng |
| Bỏ qua tối ưu hóa pin | Giữ service không bị hệ điều hành giết | Cài đặt → Pin |
| Device Admin | Bước gỡ ứng dụng phải qua cảnh báo | Cài đặt → Quản trị thiết bị |
| Thông báo (Android 13+) | Hiển thị trạng thái bảo vệ | Hộp thoại runtime |

Wizard onboarding trong app hướng dẫn từng bước và có nút mở thẳng màn hình cài đặt tương ứng.

---

## 7. Quyền riêng tư & dữ liệu

- Mọi hồ sơ, thời gian xem, lịch sử câu hỏi nằm trong Room/SQLite **trên thiết bị**.
- Không có server, không analytics, không crash reporter, không SDK quảng cáo.
- Ứng dụng **không đọc nội dung màn hình**, không ghi phím, không lấy mật khẩu hay tin nhắn. Accessibility chỉ dùng để nhận diện package đang foreground.
- Kết nối mạng chỉ xảy ra khi phụ huynh chủ động nhập URL JSON để cập nhật câu hỏi.
- Gỡ ứng dụng sẽ xóa toàn bộ dữ liệu cục bộ.

---

## 8. Định dạng câu hỏi

Câu hỏi dùng JSON, có thể nạp từ asset hoặc URL:

```json
[
  {
    "id": "q_math_101",
    "type": "choice",
    "topic": "toán",
    "age_band": "7-9",
    "difficulty": 1,
    "lang": "vi",
    "question": "8 × 7 bằng bao nhiêu?",
    "options": ["54", "56", "58", "64"],
    "answer": "56",
    "hint": "8 × 5 = 40, cộng thêm 16",
    "explanation": "8 × 7 = 56",
    "active": 1
  }
]
```

- `age_band`: `4-6`, `7-9`, `10-12`.
- `lang`: `vi` hoặc `en`.
- Bộ câu hỏi mặc định nằm tại `app/src/main/assets/questions-starter.json`.

### Nhập từ Excel / CSV

Trong **Cài đặt → Câu hỏi**, chọn **Nhập câu hỏi từ Excel**. Hỗ trợ `.xlsx` và `.csv` với các cột:

| Cột | Bắt buộc | Ghi chú |
|---|---|---|
| STT | Không | Bỏ qua |
| Question | Có | Nội dung câu hỏi |
| A / B / C / D | Có ít nhất 2 | Các đáp án; ô trống được bỏ qua |
| Correct | Có | Chữ cái A–D, số 1–4, hoặc đúng nội dung đáp án |
| Description | Không | Lời giải, lưu vào `explanation` |
| Topic / Age Band / Difficulty / Hint / Lang | Không | Nếu có sẽ ghi đè giá trị mặc định đang chọn trong app |

- Nhóm tuổi và ngôn ngữ mặc định chọn ngay trong thẻ nhập file; cột `Age Band` / `Lang` (nếu có) được ưu tiên.
- **Chống trùng:** câu hỏi trùng *nội dung + đáp án + nhóm tuổi + ngôn ngữ* sẽ bị bỏ qua; nhập lại cùng file sẽ cập nhật thay vì nhân đôi. JSON thiếu `id` cũng được sinh ID ổn định theo nội dung.
- File mẫu: [Google Sheets](https://docs.google.com/spreadsheets/d/1Z88wm1TV01O3vufHuXGkX0kgsY7ctbVlasEZmIFNa-A/edit?usp=sharing).
- File `.xls` cũ chưa hỗ trợ — lưu sang `.xlsx` hoặc `.csv`.

### Xóa toàn bộ ngân hàng

Trong **Cài đặt → Câu hỏi**, nút **Xóa toàn bộ câu hỏi** sẽ xóa toàn bộ bảng `questions` (và lịch sử `question_seen` đi kèm) sau khi xác nhận. Cổng đố vui vẫn hoạt động bằng bộ sinh câu hỏi tự động cho đến khi bạn nhập lại.

---

## 9. Trạng thái dự án

Đây là **MVP đang phát triển**, code build xanh và đã có unit test cho policy, bảo mật PIN và bộ sinh câu hỏi. Một số điểm cần hoàn thiện trước khi phát hành:

- [ ] Kiểm chứng trên ma trận thiết bị thật (Android TV, TV box AOSP, tablet, điện thoại).
- [ ] Đo RAM/CPU/pin của service nền trên TV box cấu hình thấp.
- [ ] Đồng bộ nội dung câu hỏi tiếng Anh (hiện gói starter pack là tiếng Việt).
- [ ] Pipeline cập nhật nội dung có manifest + kiểm tra sha256.
- [ ] Bổ sung nội dung 1.450 câu theo kế hoạch.

---

## 10. Triết lý thiết kế

> “Không lấy đi thời gian của con — đổi thời gian đó thành một cơ hội học tập nhỏ.”

Quiz Gate được xây dựng quanh ba nguyên tắc:

1. **An toàn cho trẻ trước tiên** — không có trạng thái nào khiến trẻ bị kẹt vĩnh viễn.
2. **Phụ huynh giữ quyền quyết định** — mọi giới hạn đều có thể mở bằng PIN.
3. **Riêng tư tuyệt đối** — dữ liệu của gia đình không rời khỏi thiết bị.

---

*Quiz Gate — vừa học, vừa chơi, vừa lớn.*
