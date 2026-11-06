package com.quizlock.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProfileEntity::class,
        QuestionEntity::class,
        QuestionSeenEntity::class,
        DayLedgerEntity::class,
        AppExclusionEntity::class,
        ConfigEntity::class,
        AttemptLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QuizGateDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun questionDao(): QuestionDao
    abstract fun questionSeenDao(): QuestionSeenDao
    abstract fun dayLedgerDao(): DayLedgerDao
    abstract fun appExclusionDao(): AppExclusionDao
    abstract fun configDao(): ConfigDao
    abstract fun attemptLogDao(): AttemptLogDao

    companion object {
        @Volatile
        private var INSTANCE: QuizGateDatabase? = null

        fun getInstance(context: Context): QuizGateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizGateDatabase::class.java,
                    "quizgate.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
