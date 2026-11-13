package com.quizlock.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.quizlock.BuildConfig

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
    exportSchema = true
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
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    QuizGateDatabase::class.java,
                    "quizgate.db"
                )
                // Destructive fallback is for local development only. Release builds
                // must provide an explicit migration for every schema change.
                if (BuildConfig.DEBUG) {
                    builder.fallbackToDestructiveMigration(dropAllTables = true)
                }
                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}
