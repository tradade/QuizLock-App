# QuizLock

[Tiếng Việt](README.vi.md) · [English](README.md)

![CI](https://github.com/tradade/QuizLock/actions/workflows/ci.yml/badge.svg)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
![Min SDK](https://img.shields.io/badge/minSdk-24-green)
![Release](https://img.shields.io/badge/release-1.0.0-orange)

> **Quiz for screen time — turn device time into a little learning-and-play moment.**

QuizLock is an Android app for parents who want to manage their child's TV / tablet / smartphone time **without banning the device**. Instead of hard-locking the screen, it turns each viewing block into a small “gate”: the child answers one Math, Language or Logic question correctly to unlock and keep watching.

Everything runs **fully on-device** — no backend, no accounts, no ads. The goal is a lightweight, private tool that works on both affordable TV boxes and everyday Android phones.

---

## 1. The idea

Children often get upset when a device is suddenly taken away. QuizLock takes a different route:

- **No ban** — the child can keep watching, but pays with a correct answer.
- **No heavy extra schoolwork** — questions follow the child's age band, with hints and short explanations.
- **No privacy trade-off** — child data stays on the device, never sent to the cloud.
- **No bricked device** — there is always an escape hatch via the parent PIN and an automatic fallback question generator.

---

## 2. How it works

```
┌─────────────────────┐
│ Child opens YouTube │
│ or a game           │
└──────────┬──────────┘
           │  AccessibilityService tracks the foreground app
           ▼
┌──────────────────────┐        below threshold
│ Accumulate watch time│ ─────────────────────────┐
└──────────┬───────────┘                          │
           │ X minutes / daily cap / outside hours │
           ▼                                       │
┌──────────────────────┐                          │
│   Open QuizLock     │ ◄────────────────────────┘
│ (question + 4 answers│
└──────────┬───────────┘
           │ correct answer
           ▼
┌──────────────────────┐
│ Unlock + record      │ ──► child returns to the app
│ progress, close ~2s  │
└──────────────────────┘
```

1. The parent installs the app, grants permissions, creates a PIN and a child profile.
2. A background service tracks the foreground app and accumulates screen time (excluding approved apps).
3. When the threshold is reached, a full-screen quiz appears on top of the current app.
4. Correct answer → the gate closes and the child continues. Wrong answer → retry; after 2 misses a hint appears; after 3 misses a new question is loaded.
5. The parent can always unlock with the PIN or enable **Parent Mode** when the device is needed.

---

## 3. Features

### Quiz gate
- Four-answer multiple-choice questions selected by the child's age band.
- Repetition guard: a question is not repeated within **14 days** for that profile.
- Hint after 2 wrong attempts; automatically swaps to a fresh question after the 3rd wrong attempt.
- Short victory screen with the explanation, auto-closing after ~2 seconds.
- TV remote / D-pad support: number keys **1–4** select answers, and all controls are focusable.
- Text-to-speech button reads the question; the voice follows the app language.

### Three control strategies
| Strategy | Behaviour |
|---|---|
| **Daily cap** (default) | Sets a total daily viewing limit. Once reached, the device locks; only the PIN can unlock. |
| **Infinite** | Every X minutes the child must solve one question to continue; no total limit. |
| **Escalating** | Each subsequent gate requires more correct answers (1 → 2 → 3…). |

### Child profiles
- Multiple profiles; each child has a name, age, age band and individual limits.
- **Tap a profile** in the Profile Manager to open that child's **own Settings screen**: inherit global rules or enable custom ones, choose strategy, gate interval, daily cap and allowed hours.
- The pencil icon on a card edits the child's name and age.
- Each profile can have its own allowed viewing hours; outside them the “bedtime” screen is shown.

### Time management
- Gate interval X (minutes), daily cap and allowed start/end times.
- Quick presets: all day, after school, relaxing evening, weekend.
- **Excluded apps**: learning, reading or online-class apps are never counted or blocked.
- **Parent Mode**: pause gates for 1 / 2 / 4 / 8 hours without counting toward the child's time.

### Background reliability
- **Event-driven watchdog**: re-evaluates on app changes, screen on/off, gate close or config changes, and only wakes at the next gate/cap/allowed-hours boundary instead of polling every 2 seconds.
- Watched seconds are buffered in memory and flushed to the database every 30 seconds.
- Restarts after reboot / app update; shows a broken-state warning when Accessibility is lost.
- Best-effort restart when the task is swiped away (fallback AlarmManager), still subject to OEM behaviour and force-stop.
- Android 15/16 may reject a `specialUse` FGS started from `BOOT_COMPLETED`; the watchdog keeps running in the AccessibilityService and tries to start the FGS from there.

### Security & anti-bypass
- 6-digit parent PIN stored as a **PBKDF2-HMAC-SHA256 hash with 150,000 iterations and a random salt**.
- 5 wrong attempts → 5-minute lockout.
- 8-character recovery code shown once at setup, used to reset the PIN.
- **App lock**: once a PIN is configured, the whole management UI is locked on launch and whenever the app returns from the background; unlock with the PIN or recovery code.
- **Super PIN (debug builds only)**: enter `999999` on the lock screen to open management without the configured PIN, bypassing lockout as well. Release builds ignore it entirely.
- **Broken-state warning**: the background service checks permissions every hour; if Accessibility is off, the notification switches to “Permission needed” with a button that opens Accessibility settings.
- Device Admin receiver + Boot receiver so protection comes back after a restart.
- Persistent foreground notification showing protection status.

### Question bank
- **200 Vietnamese questions** bundled in the APK across 3 age bands.
- Additional questions can be imported from a direct HTTPS JSON URL or an Excel/CSV/TSV file in Settings.
- Content-based duplicate prevention: the same question + answer + age band + language is skipped on import.
- A **delete the entire question bank** action (with confirmation) is available in Settings.
- When the bank is empty or exhausted, a **fallback generator** creates age-appropriate Math / Logic questions — the gate never dies.

### Vietnamese – English support
- The entire UI, notifications, PIN dialogs, block screens and legal content support **Tiếng Việt / English**.
- Language can be switched from the Dashboard, Onboarding or Settings; the choice is stored in the local database.
- System resources (Accessibility description, notification channel, Device Admin toasts) are translated in `values-en/`.

---

## 4. Architecture & tech stack

The project is currently a **single `:app` module** (not split into the multi-module design from the original plan), prioritising simplicity and a small APK.

> Application ID: `com.cloudon.quizlock` · Kotlin namespace: `com.quizlock`.

| Component | Choice |
|---|---|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 |
| Storage | Room (SQLite), no DataStore |
| Foreground tracking | `AccessibilityService` |
| Background service | Foreground Service + persistent notification |
| Anti-uninstall / restart | Device Admin receiver + Boot receiver |
| Network | OkHttp (only when the parent explicitly imports a question URL) |
| i18n | `Strings` + `LocalStrings` / `LocalAppLanguage` in Compose |
| Build | AGP 9.1.1 · Gradle 9.3.1 · minSdk 24 · targetSdk 36 |

### Directory structure

```
app/src/main/java/com/quizlock/
├── MainActivity.kt                 # Main navigation + bottom nav
├── QuizGateApplication.kt         # DB init, seeding, service start
├── core/
│   ├── data/                      # Room entities, DAOs, repository, fallback generator
│   ├── i18n/                      # VI/EN strings and CompositionLocal
│   └── policy/                    # PolicyEngine: gate / hard-block decisions
├── service/                       # AccessibilityService + ForegroundService
├── receiver/                      # BootReceiver, DeviceAdminReceiver
└── ui/
    ├── parent/                    # Dashboard, Profiles, Settings, Onboarding, TOS
    ├── gate/                      # QuizGateActivity, HardBlockActivity
    ├── components/                # PIN pad, Time picker
    └── theme/                     # Colors, typography
```

### Data flow

`AccessibilityService` → `QuizGateRepository` (profiles, config, daily ledger) → `PolicyEngine` (pure decision) → launches `QuizGateActivity` / `HardBlockActivity` → writes `attempt_log`, `day_ledger`, `question_seen`.

---

## 5. Installation & setup

### 5.1 Requirements

| Component | Version / note |
|---|---|
| JDK | 17 or 21 |
| Android SDK Platform | **36.1** (`compileSdk`), build-tools 36.1.0 |
| Gradle | **9.3.1** (AGP 9.1.1 requires at least this version) |
| Android Studio | Recommended, a build that supports AGP 9.x |
| Device / emulator | Android 7.0 (API 24) or newer |

> No backend or Firebase is required — the project has no Firebase dependencies or configuration.

### 5.2 Prepare the project

```bash
git clone https://github.com/tradade/QuizLock.git QuizLock
cd QuizLock
```

Create `local.properties` if Android Studio has not done so:

```properties
sdk.dir=/path/to/Android/Sdk
```

Or export `ANDROID_HOME`.

The Gradle wrapper is included (`./gradlew`). Open the project in Android Studio or run the wrapper directly.

### 5.3 Release signing (optional)

Debug builds use the standard Android debug keystore, so `assembleDebug` works on a fresh clone. Release builds use environment variables:

```bash
export KEYSTORE_PATH=/path/to/upload-key.jks
export STORE_PASSWORD=keystore-password
export KEY_PASSWORD=key-password
```

### 5.4 Build & install

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `./gradlew :app:assembleRelease` → `app/build/outputs/apk/release/`
- If installing over an existing build fails with a signature error: `adb uninstall com.cloudon.quizlock`, then install again.
- **Android TV / TV box** (enable ADB debugging in Developer options):

```bash
adb connect <tv-ip>:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 5.5 First-run setup inside the app

1. Open the app → choose **Tiếng Việt / English**.
2. Grant **Accessibility Service** (required) — the main mechanism for detecting the foreground app.
3. Grant **Ignore battery optimization** and **Notifications** (Android 13+) so the protection status is visible.
4. (Recommended) Enable **Device Admin** so uninstalling requires an extra warning step.
5. Create a **6-digit parent PIN** and write down the **8-character recovery code**. From the next launch, the management UI asks for this PIN.
6. Create the first child profile: name, age, strategy, gate interval X and daily cap.
7. Go to **Settings → Exclusions** and add learning / reading apps that must not be counted.
8. Open **Child Profiles**, tap a profile to open its **own Settings**, and enable “Custom rules for this child”.
9. Use **Test QuizLock** on the Dashboard for a quick check.

> **Developer Super PIN:** in **debug** builds, enter `999999` on the lock screen to open management without the parent PIN (even during lockout). Release builds ignore this value. Change it via `QuizGateRepository.SUPER_PIN`.

#### Device-specific notes

- **Xiaomi / Redmi (MIUI)**: enable Autostart and set the battery profile to “No restrictions” for QuizLock.
- **Samsung / One UI**: add the app to “Never sleeping apps” if the service is stopped.
- **Android TV / TV box**: some devices hide permission screens; use the remote or ADB to open Settings. Re-check Accessibility after a reboot.
- **Android 13+**: grant notification permission to see the protection status.

### 5.6 Tests

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

### 5.7 Troubleshooting

| Error | Fix |
|---|---|
| `Minimum supported Gradle version is 9.3.1` | Use the bundled wrapper (`./gradlew`) or Android Studio. |
| Missing `SDK Platform 36.1` | Install it via SDK Manager or let Gradle download it. |
| Service stops after reboot | Check Accessibility, battery optimization and OEM autostart settings. |
| Screen time is not counted | Confirm Accessibility is enabled, the app is not excluded, and the current time is inside the allowed window. |
| Forgot PIN / need quick dev access | Use the **recovery code** on the lock screen; debug builds can use Super PIN `999999`. |

### 5.8 Real-device verification checklist

- [ ] Reboot → Accessibility stays enabled → watchdog is active again within 60 seconds.
- [ ] Swipe the app from recents → the service restarts (fallback AlarmManager, ~5 seconds or when the app is reopened).
- [ ] **MIUI/Redmi**: enable Autostart + “No restrictions”; lock the screen for 10 minutes, then open YouTube → the gate appears at the configured X threshold.
- [ ] **One UI/Samsung**: add the app to “Never sleeping apps”; leave it overnight and confirm the service survives.
- [ ] **Android 15/16**: after reboot check the notification; if the `specialUse` FGS is rejected, the watchdog still runs via Accessibility — open the app to restart the notification.
- [ ] **Force-stop**: confirm the Android limitation — the app does not restart until the user opens it again.

---

## 6. Required permissions

| Permission | Purpose | Where to grant |
|---|---|---|
| Accessibility Service | **Required** — detects the foreground app to count time and open gates | Settings → Accessibility |
| Ignore battery optimization | Keeps the service from being killed by the OS | Settings → Battery |
| Device Admin | Adds a warning step before uninstalling | Settings → Device admin apps |
| Notifications (Android 13+) | Shows protection status | Runtime dialog |

The onboarding wizard walks through each step and provides buttons that open the relevant settings screen.

---

## 7. Privacy & data

- Every profile, screen-time record and quiz history lives in Room/SQLite **on the device**.
- No server, no analytics, no crash reporter, no advertising SDK.
- The app **never reads screen content**, never records keystrokes, and never collects passwords or messages. Accessibility is used only to detect the foreground package.
- Network access happens only when the parent explicitly enters an HTTPS JSON URL to update questions.
- Uninstalling the app deletes all local data.

Repository copies: [Privacy policy](docs/PRIVACY.md) · [Play compliance notes](docs/PLAY_COMPLIANCE.md).

---

## 8. Question format

Questions are JSON and can be loaded from an asset or an HTTPS URL:

```json
[
  {
    "id": "q_math_101",
    "type": "choice",
    "topic": "math",
    "age_band": "7-9",
    "difficulty": 1,
    "lang": "en",
    "question": "What is 8 × 7?",
    "options": ["54", "56", "58", "64"],
    "answer": "56",
    "hint": "8 × 5 = 40, then add 16",
    "explanation": "8 × 7 = 56",
    "active": 1
  }
]
```

- `age_band`: `4-6`, `7-9`, `10-12`.
- `lang`: `vi` or `en`.
- The default bank is at `app/src/main/assets/questions-starter.json`.

### Excel / CSV import

In **Settings → Questions**, choose **Import questions from Excel**. For parents, the easiest format is **one `.xlsx` spreadsheet** (Excel or Google Sheets), with one question per row. `.csv`/`.tsv` is also supported as a lightweight fallback.

Recommended template:

| Question | Answer A | Answer B | Answer C | Answer D | Correct answer | Explanation | Hint |
|---|---|---|---|---|---|---|---|
| What is 2 + 3? | 4 | 5 | 6 | 7 | B | 2 + 3 = 5. | Count on from 2. |

- **Required:** `Question`, at least 2 option cells, and `Correct answer`.
- `Correct answer` should normally be `A`, `B`, `C`, or `D`; `1`–`4`, the exact option text, and labels such as `Answer B` are also accepted.
- **Not required:** `STT`, `Age Band`, `Lang`, `Topic`, or `Difficulty` when the whole file uses one age band/language. Choose the age band and language in the import screen.
- Optional columns: `Explanation`/`Description`, `Hint`, `Topic`, `Age Band`, `Difficulty`, and `Lang`.
- Both Vietnamese headers (`Câu hỏi`, `Đáp án A`...) and English headers (`Question`, `Answer A`...) are accepted.
- Supported files: `.xlsx`, `.csv`, `.tsv`, and comma-, semicolon-, or tab-separated text. Legacy `.xls` files are not supported.
- The app includes **Copy a template for Excel / Sheets**, so parents do not need to remember the column names.
- **Duplicate prevention:** the same *question + answer + age band + language* is skipped; re-importing the same file updates rows instead of duplicating them. JSON without an `id` also receives a deterministic content-based ID.
- Repository template: [`docs/question-bank-template.csv`](docs/question-bank-template.csv).

### Delete the entire bank

In **Settings → Questions**, **Delete all questions** removes the whole `questions` table (and its `question_seen` history) after confirmation. Quiz gates keep working with the automatic question generator until you import again.

---

## 9. Project status

This is an **in-progress MVP**. The code compiles and has unit tests for policy, PIN security and the fallback generator. Remaining work before release:

- [ ] Validate on the real device matrix (Android TV, AOSP TV boxes, tablets, phones).
- [ ] Measure background-service RAM/CPU/battery on low-end TV boxes.
- [ ] Add English content to the starter pack (the bundled pack is Vietnamese).
- [ ] Add a content pipeline with manifest + sha256 verification.
- [ ] Grow the bank to the planned 1,450 questions.

---

## 10. Design philosophy

> “Don't take time away from the child — turn that time into a small learning opportunity.”

QuizLock is built around three principles:

1. **Child safety first** — no state can leave a child permanently stuck.
2. **Parents keep control** — every limit can be unlocked with the PIN.
3. **Absolute privacy** — family data never leaves the device.

---

## 11. Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for build/test instructions and [SECURITY.md](SECURITY.md) for private vulnerability reports.

## 12. License

Licensed under the [Apache License 2.0](LICENSE). See [NOTICE](NOTICE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

---

*QuizLock — learn a little, play a little, grow a little.*
