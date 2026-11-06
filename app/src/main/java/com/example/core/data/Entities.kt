package com.example.core.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarId: Int = 1,
    val age: Int,
    val ageBand: String, // "4-6" | "7-9" | "10-12"
    val dailyCapMinutes: Int? = null,
    val gateIntervalMinutes: Int? = null,
    val strategy: String? = null, // "DAILY_CAP" | "INFINITE" | "ESCALATING" | null (inherit global)
    val allowedStartTime: String? = null,
    val allowedEndTime: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey
    val id: String,
    val type: String = "choice", // "choice" | "puzzle"
    val topic: String,           // "toán", "tiếng việt", "luận lý", "khoa học"
    val ageBand: String,         // "4-6" | "7-9" | "10-12"
    val difficulty: Int = 1,
    val lang: String = "vi",
    val question: String,
    val optionsJson: String,     // JSON array of strings e.g. ["A", "B", "C", "D"]
    val answer: String,
    val hint: String? = null,
    val explanation: String? = null,
    val active: Int = 1
)

@Entity(
    tableName = "question_seen",
    primaryKeys = ["profileId", "questionId"],
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["questionId"])]
)
data class QuestionSeenEntity(
    val profileId: Long,
    val questionId: String,
    val seenAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "day_ledger",
    primaryKeys = ["profileId", "localDate"]
)
data class DayLedgerEntity(
    val profileId: Long,
    val localDate: String, // YYYY-MM-DD
    val watchedSeconds: Long = 0L,
    val earnedCycles: Int = 0,
    val quizSolved: Int = 0,
    val quizAttempted: Int = 0,
    val gatesPassed: Int = 0,
    val gateAnchorSeconds: Long = 0L
)

@Entity(tableName = "app_exclusions")
data class AppExclusionEntity(
    @PrimaryKey
    val packageName: String,
    val label: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "config")
data class ConfigEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

@Entity(tableName = "attempt_log")
data class AttemptLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val questionId: String,
    val correct: Boolean,
    val attemptNumber: Int,
    val secondsTaken: Int,
    val timestamp: Long = System.currentTimeMillis()
)
