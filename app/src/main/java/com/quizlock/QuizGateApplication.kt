package com.quizlock

import android.app.Application
import android.util.Log
import com.quizlock.core.data.QuizGateDatabase
import com.quizlock.core.data.QuizGateRepository
import com.quizlock.core.data.StarterPackImporter
import com.quizlock.service.WatchdogForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuizGateApplication : Application() {

    lateinit var database: QuizGateDatabase
        private set

    lateinit var repository: QuizGateRepository
        private set

    private val appScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        database = QuizGateDatabase.getInstance(this)
        repository = QuizGateRepository(database)

        appScope.launch {
            try {
                // Seed starter pack if fresh database
                StarterPackImporter.importIfNeeded(this@QuizGateApplication, database)

                // Seed default exclusion for QuizLock itself
                repository.addExclusion(packageName, "QuizLock")

                // Start the watchdog only after setup. Onboarding starts it when finished.
                if (repository.isPinConfigured()) {
                    WatchdogForegroundService.start(this@QuizGateApplication)
                }
            } catch (e: Exception) {
                Log.e("QuizGateApplication", "Startup initialization failed", e)
            }
        }
    }
}
