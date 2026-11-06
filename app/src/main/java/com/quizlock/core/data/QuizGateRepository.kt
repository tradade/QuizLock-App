package com.quizlock.core.data

import android.content.Context
import com.quizlock.BuildConfig
import com.quizlock.core.policy.PolicyConfig
import com.quizlock.core.policy.PolicyEngine
import com.quizlock.core.policy.PolicyStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class QuizGateRepository(
    private val database: QuizGateDatabase,
    private val policyEngine: PolicyEngine = PolicyEngine()
) {

    companion object {
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_PIN_SALT = "pin_salt"
        const val KEY_RECOVERY_HASH = "recovery_code_hash"
        const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        const val KEY_PARENT_MODE_UNTIL = "parent_mode_until"

        const val KEY_STRATEGY = "policy_strategy"
        const val KEY_GATE_INTERVAL = "gate_interval_minutes"
        const val KEY_REWARD_MINUTES = "reward_minutes"
        const val KEY_DAILY_CAP = "daily_cap_minutes"
        const val KEY_START_TIME = "allowed_start_time"
        const val KEY_END_TIME = "allowed_end_time"
        const val KEY_MAX_QUESTIONS = "max_questions_per_gate"
        const val KEY_APP_LANGUAGE = "app_language"

        const val SEEN_WINDOW_DAYS_MILLIS = 14L * 24 * 60 * 60 * 1000L

        const val SUPER_PIN = "999999"

        fun isSuperPin(pin: String): Boolean = BuildConfig.DEBUG && pin == SUPER_PIN
    }

    val allProfilesFlow: Flow<List<ProfileEntity>> = database.profileDao().getAllProfilesFlow()
    val allExclusionsFlow: Flow<List<AppExclusionEntity>> = database.appExclusionDao().getAllExclusionsFlow()

    val appLanguageFlow: Flow<com.quizlock.core.i18n.AppLanguage> = database.configDao().getFlow(KEY_APP_LANGUAGE)
        .map { com.quizlock.core.i18n.AppLanguage.fromCode(it) }
        .flowOn(Dispatchers.IO)

    suspend fun getAppLanguage(): com.quizlock.core.i18n.AppLanguage {
        val code = database.configDao().get(KEY_APP_LANGUAGE)
        return com.quizlock.core.i18n.AppLanguage.fromCode(code)
    }

    suspend fun setAppLanguage(lang: com.quizlock.core.i18n.AppLanguage) {
        database.configDao().set(ConfigEntity(KEY_APP_LANGUAGE, lang.code))
    }

    suspend fun getActiveProfileId(): Long {
        val str = database.configDao().get(KEY_ACTIVE_PROFILE_ID)
        return str?.toLongOrNull() ?: 1L
    }

    suspend fun setActiveProfileId(id: Long) {
        database.configDao().set(ConfigEntity(KEY_ACTIVE_PROFILE_ID, id.toString()))
    }

    suspend fun getActiveProfile(): ProfileEntity? {
        val id = getActiveProfileId()
        return database.profileDao().getProfileById(id)
            ?: database.profileDao().getAllProfiles().firstOrNull()
    }

    fun getActiveProfileFlow(): Flow<ProfileEntity?> {
        return database.configDao().getFlow(KEY_ACTIVE_PROFILE_ID)
            .map { it?.toLongOrNull() ?: 1L }
            .combine(allProfilesFlow) { activeId, profiles ->
                profiles.firstOrNull { it.id == activeId } ?: profiles.firstOrNull()
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun addProfile(
        name: String,
        age: Int,
        avatarId: Int,
        dailyCap: Int?,
        gateIntervalMinutes: Int? = null,
        strategy: String? = null,
        allowedStartTime: String? = null,
        allowedEndTime: String? = null
    ): Long {
        val band = when {
            age <= 6 -> "4-6"
            age <= 9 -> "7-9"
            else -> "10-12"
        }
        val entity = ProfileEntity(
            name = name,
            age = age,
            avatarId = avatarId,
            ageBand = band,
            dailyCapMinutes = dailyCap,
            gateIntervalMinutes = gateIntervalMinutes,
            strategy = strategy,
            allowedStartTime = allowedStartTime,
            allowedEndTime = allowedEndTime
        )
        return database.profileDao().insertProfile(entity)
    }

    suspend fun updateProfile(profile: ProfileEntity) {
        database.profileDao().updateProfile(profile)
    }

    suspend fun deleteProfile(id: Long) {
        database.profileDao().softDeleteProfile(id)
        val activeId = getActiveProfileId()
        if (activeId == id) {
            val remaining = database.profileDao().getAllProfiles()
            if (remaining.isNotEmpty()) {
                setActiveProfileId(remaining.first().id)
            }
        }
    }

    suspend fun getEffectivePolicyConfig(profile: ProfileEntity?): PolicyConfig {
        val global = getPolicyConfig()
        if (profile == null) return global
        val strat = profile.strategy?.let {
            try { PolicyStrategy.valueOf(it) } catch (_: Exception) { global.strategy }
        } ?: global.strategy

        return global.copy(
            strategy = strat,
            dailyCapMinutes = profile.dailyCapMinutes ?: global.dailyCapMinutes,
            gateIntervalMinutes = profile.gateIntervalMinutes ?: global.gateIntervalMinutes,
            allowedStartTime = profile.allowedStartTime ?: global.allowedStartTime,
            allowedEndTime = profile.allowedEndTime ?: global.allowedEndTime
        )
    }

    // --- PIN & Security ---

    suspend fun isPinConfigured(): Boolean {
        return database.configDao().get(KEY_PIN_HASH) != null
    }

    suspend fun setupPin(pin: String): String {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPin(pin, salt)
        val recoveryCode = SecurityUtils.generateRecoveryCode()
        val recoveryHash = SecurityUtils.hashPin(recoveryCode, salt)

        database.configDao().set(ConfigEntity(KEY_PIN_HASH, hash))
        database.configDao().set(ConfigEntity(KEY_PIN_SALT, salt))
        database.configDao().set(ConfigEntity(KEY_RECOVERY_HASH, recoveryHash))

        return recoveryCode
    }

    suspend fun verifyPin(enteredPin: String, allowSuperPin: Boolean = false): Boolean {
        if (allowSuperPin && isSuperPin(enteredPin)) return true
        val hash = database.configDao().get(KEY_PIN_HASH) ?: return false
        val salt = database.configDao().get(KEY_PIN_SALT) ?: return false
        return SecurityUtils.verifyPin(enteredPin, hash, salt)
    }

    suspend fun verifyRecoveryCode(enteredCode: String): Boolean {
        val hash = database.configDao().get(KEY_RECOVERY_HASH) ?: return false
        val salt = database.configDao().get(KEY_PIN_SALT) ?: return false
        return SecurityUtils.verifyPin(enteredCode.trim().uppercase(), hash, salt)
    }

    // --- Parent Mode ---

    suspend fun getParentModeUntil(): Long? {
        val str = database.configDao().get(KEY_PARENT_MODE_UNTIL)
        val until = str?.toLongOrNull() ?: return null
        return if (until > System.currentTimeMillis()) until else null
    }

    suspend fun startParentMode(durationHours: Int) {
        val until = System.currentTimeMillis() + (durationHours * 3600_000L)
        database.configDao().set(ConfigEntity(KEY_PARENT_MODE_UNTIL, until.toString()))
    }

    suspend fun stopParentMode() {
        database.configDao().delete(KEY_PARENT_MODE_UNTIL)
    }

    // --- Policy & Settings ---

    suspend fun getPolicyConfig(): PolicyConfig {
        val strategyStr = database.configDao().get(KEY_STRATEGY) ?: PolicyStrategy.DAILY_CAP.name
        val strategy = try { PolicyStrategy.valueOf(strategyStr) } catch (_: Exception) { PolicyStrategy.DAILY_CAP }
        val interval = database.configDao().get(KEY_GATE_INTERVAL)?.toIntOrNull() ?: 30
        val reward = database.configDao().get(KEY_REWARD_MINUTES)?.toIntOrNull() ?: 30
        val cap = database.configDao().get(KEY_DAILY_CAP)?.toIntOrNull() ?: 120
        val start = database.configDao().get(KEY_START_TIME) ?: "06:00"
        val end = database.configDao().get(KEY_END_TIME) ?: "21:00"
        val maxQ = database.configDao().get(KEY_MAX_QUESTIONS)?.toIntOrNull() ?: 5

        return PolicyConfig(
            strategy = strategy,
            gateIntervalMinutes = interval,
            rewardMinutes = reward,
            dailyCapMinutes = cap,
            allowedStartTime = start,
            allowedEndTime = end,
            maxQuestionsPerGate = maxQ
        )
    }

    suspend fun savePolicyConfig(config: PolicyConfig) {
        database.configDao().set(ConfigEntity(KEY_STRATEGY, config.strategy.name))
        database.configDao().set(ConfigEntity(KEY_GATE_INTERVAL, config.gateIntervalMinutes.toString()))
        database.configDao().set(ConfigEntity(KEY_REWARD_MINUTES, config.rewardMinutes.toString()))
        database.configDao().set(ConfigEntity(KEY_DAILY_CAP, config.dailyCapMinutes.toString()))
        database.configDao().set(ConfigEntity(KEY_START_TIME, config.allowedStartTime))
        database.configDao().set(ConfigEntity(KEY_END_TIME, config.allowedEndTime))
        database.configDao().set(ConfigEntity(KEY_MAX_QUESTIONS, config.maxQuestionsPerGate.toString()))
    }

    // --- Screen Time Ledger ---

    suspend fun getTodayLedger(profileId: Long): DayLedgerEntity {
        val date = policyEngine.getLocalDate(System.currentTimeMillis())
        var ledger = database.dayLedgerDao().getLedger(profileId, date)
        if (ledger == null) {
            ledger = DayLedgerEntity(profileId = profileId, localDate = date)
            database.dayLedgerDao().upsertLedger(ledger)
        }
        return ledger
    }

    fun getTodayLedgerFlow(profileId: Long): Flow<DayLedgerEntity?> {
        val date = policyEngine.getLocalDate(System.currentTimeMillis())
        return database.dayLedgerDao().getLedgerFlow(profileId, date)
    }

    suspend fun recordWatchSeconds(profileId: Long, seconds: Long) {
        val date = policyEngine.getLocalDate(System.currentTimeMillis())
        val updated = database.dayLedgerDao().incrementWatchedSeconds(profileId, date, seconds)
        if (updated == 0) {
            database.dayLedgerDao().upsertLedger(
                DayLedgerEntity(profileId = profileId, localDate = date, watchedSeconds = seconds)
            )
        }
    }

    suspend fun recordGateSolved(profileId: Long, currentWatchedSeconds: Long) {
        val date = policyEngine.getLocalDate(System.currentTimeMillis())
        database.dayLedgerDao().recordGateSolved(profileId, date, currentWatchedSeconds)
    }

    // --- Questions & Quiz Gate ---

    suspend fun getNextQuestion(profileId: Long, ageBand: String): QuestionEntity {
        val lang = getAppLanguage().code
        val threshold = System.currentTimeMillis() - SEEN_WINDOW_DAYS_MILLIS
        val unseen = database.questionDao().getRandomUnseenQuestions(
            ageBand = ageBand,
            profileId = profileId,
            seenThresholdMillis = threshold,
            lang = lang,
            limit = 1
        )
        if (unseen.isNotEmpty()) {
            return unseen.first()
        }

        // Try any from band in current language
        val anyInBandLang = database.questionDao().getRandomQuestionsByBandAndLang(ageBand, lang, 1)
        if (anyInBandLang.isNotEmpty()) {
            return anyInBandLang.first()
        }

        // Try any from band across languages if available
        val anyInBand = database.questionDao().getRandomQuestionsByBand(ageBand, 1)
        if (anyInBand.isNotEmpty()) {
            return anyInBand.first()
        }

        // Procedural fallback (guaranteed never empty)
        return FallbackQuestionGenerator.generate(ageBand, lang)
    }

    suspend fun markQuestionSeen(profileId: Long, questionId: String) {
        database.questionSeenDao().markSeen(
            QuestionSeenEntity(profileId = profileId, questionId = questionId)
        )
    }

    suspend fun recordAttempt(profileId: Long, questionId: String, correct: Boolean, attemptNumber: Int, secondsTaken: Int) {
        database.attemptLogDao().logAttempt(
            AttemptLogEntity(
                profileId = profileId,
                questionId = questionId,
                correct = correct,
                attemptNumber = attemptNumber,
                secondsTaken = secondsTaken
            )
        )
    }

    // --- Excluded Apps ---

    suspend fun isAppExcluded(packageName: String): Boolean {
        return database.appExclusionDao().isExcluded(packageName) > 0
    }

    suspend fun addExclusion(packageName: String, label: String) {
        database.appExclusionDao().insertExclusion(AppExclusionEntity(packageName, label))
    }

    suspend fun removeExclusion(packageName: String) {
        database.appExclusionDao().removeExclusionByPackage(packageName)
    }

    // --- Question Management & Remote URL Update ---

    suspend fun getQuestionsCount(): Int = withContext(Dispatchers.IO) {
        database.questionDao().getQuestionCount()
    }

    fun getQuestionsCountFlow(): Flow<Int> = database.questionDao().getQuestionCountFlow()

    suspend fun getQuestionCountByAgeBand(ageBand: String): Int = withContext(Dispatchers.IO) {
        database.questionDao().getQuestionCountByAgeBand(ageBand)
    }

    suspend fun importQuestionsFromJsonString(jsonString: String): Int = withContext(Dispatchers.IO) {
        val questions = StarterPackImporter.parseQuestionsFromJson(jsonString)
        if (questions.isNotEmpty()) {
            database.questionDao().insertQuestions(questions)
        }
        questions.size
    }

    suspend fun importQuestionsFromUrl(url: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val request = okhttp3.Request.Builder()
                .url(url.trim())
                .header("User-Agent", "QuizGate-Android")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
                val bodyStr = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
                val count = importQuestionsFromJsonString(bodyStr)
                Result.success(count)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreDefaultQuestions(context: Context): Int = withContext(Dispatchers.IO) {
        StarterPackImporter.importFromAsset(context, database)
    }
}
