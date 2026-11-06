package com.example

import android.app.Application
import com.example.core.data.QuizGateDatabase
import com.example.core.data.QuizGateRepository
import com.example.core.data.StarterPackImporter
import com.example.service.WatchdogForegroundService
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
            // Seed starter pack if fresh database
            StarterPackImporter.importIfNeeded(this@QuizGateApplication, database)

            // Seed default exclusion for Quiz Gate itself
            repository.addExclusion(packageName, "Quiz Gate")

            // Start watchdog service
            WatchdogForegroundService.start(this@QuizGateApplication)
        }
    }
}
