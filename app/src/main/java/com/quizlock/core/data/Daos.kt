package com.quizlock.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class QuestionIdentityRow(
    val id: String,
    val question: String,
    val answer: String,
    val ageBand: String,
    val lang: String
)

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE isActive = 1 ORDER BY createdAt ASC")
    fun getAllProfilesFlow(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE isActive = 1 ORDER BY createdAt ASC")
    suspend fun getAllProfiles(): List<ProfileEntity>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    fun getProfileByIdFlow(id: Long): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("UPDATE profiles SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteProfile(id: Long)
}

@Dao
interface QuestionDao {
    @Query("SELECT COUNT(*) FROM questions WHERE active = 1")
    suspend fun getQuestionCount(): Int

    @Query("SELECT COUNT(*) FROM questions WHERE active = 1")
    fun getQuestionCountFlow(): Flow<Int>

    @Query("""
        SELECT * FROM questions 
        WHERE active = 1 
          AND ageBand = :ageBand 
          AND lang = :lang
          AND id NOT IN (
              SELECT questionId FROM question_seen 
              WHERE profileId = :profileId 
                AND seenAt > :seenThresholdMillis
          )
        ORDER BY RANDOM() 
        LIMIT :limit
    """)
    suspend fun getRandomUnseenQuestions(
        ageBand: String,
        profileId: Long,
        seenThresholdMillis: Long,
        lang: String = "vi",
        limit: Int = 1
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE active = 1 AND ageBand = :ageBand ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsByBand(ageBand: String, limit: Int = 1): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE active = 1 AND ageBand = :ageBand AND lang = :lang ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsByBandAndLang(ageBand: String, lang: String, limit: Int = 1): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions WHERE active = 1 AND ageBand = :ageBand")
    suspend fun getQuestionCountByAgeBand(ageBand: String): Int

    @Query("SELECT id, question, answer, ageBand, lang FROM questions")
    suspend fun getQuestionIdentities(): List<QuestionIdentityRow>

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)
}

@Dao
interface QuestionSeenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markSeen(seen: QuestionSeenEntity)

    @Query("DELETE FROM question_seen WHERE seenAt < :expirationMillis")
    suspend fun cleanOldSeen(expirationMillis: Long)
}

@Dao
interface DayLedgerDao {
    @Query("SELECT * FROM day_ledger WHERE profileId = :profileId AND localDate = :date LIMIT 1")
    suspend fun getLedger(profileId: Long, date: String): DayLedgerEntity?

    @Query("SELECT * FROM day_ledger WHERE profileId = :profileId AND localDate = :date LIMIT 1")
    fun getLedgerFlow(profileId: Long, date: String): Flow<DayLedgerEntity?>

    @Query("SELECT * FROM day_ledger WHERE profileId = :profileId ORDER BY localDate DESC LIMIT 7")
    fun getRecentLedgersFlow(profileId: Long): Flow<List<DayLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLedger(ledger: DayLedgerEntity)

    @Query("""
        UPDATE day_ledger 
        SET watchedSeconds = watchedSeconds + :additionalSeconds 
        WHERE profileId = :profileId AND localDate = :date
    """)
    suspend fun incrementWatchedSeconds(profileId: Long, date: String, additionalSeconds: Long): Int

    @Query("""
        UPDATE day_ledger 
        SET gatesPassed = gatesPassed + 1,
            quizSolved = quizSolved + 1,
            gateAnchorSeconds = :newAnchorSeconds
        WHERE profileId = :profileId AND localDate = :date
    """)
    suspend fun recordGateSolved(profileId: Long, date: String, newAnchorSeconds: Long)
}

@Dao
interface AppExclusionDao {
    @Query("SELECT * FROM app_exclusions ORDER BY label ASC")
    fun getAllExclusionsFlow(): Flow<List<AppExclusionEntity>>

    @Query("SELECT * FROM app_exclusions")
    suspend fun getAllExclusions(): List<AppExclusionEntity>

    @Query("SELECT COUNT(*) FROM app_exclusions WHERE packageName = :packageName")
    suspend fun isExcluded(packageName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExclusion(exclusion: AppExclusionEntity)

    @Delete
    suspend fun removeExclusion(exclusion: AppExclusionEntity)

    @Query("DELETE FROM app_exclusions WHERE packageName = :packageName")
    suspend fun removeExclusionByPackage(packageName: String)
}

@Dao
interface ConfigDao {
    @Query("SELECT value FROM config WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): String?

    @Query("SELECT value FROM config WHERE `key` = :key LIMIT 1")
    fun getFlow(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(entry: ConfigEntity)

    @Query("DELETE FROM config WHERE `key` = :key")
    suspend fun delete(key: String)
}

@Dao
interface AttemptLogDao {
    @Insert
    suspend fun logAttempt(log: AttemptLogEntity)

    @Query("SELECT * FROM attempt_log WHERE profileId = :profileId ORDER BY timestamp DESC LIMIT 50")
    fun getRecentAttemptsFlow(profileId: Long): Flow<List<AttemptLogEntity>>
}
