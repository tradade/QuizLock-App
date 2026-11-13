package com.quizlock

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.quizlock.core.data.QuestionEntity
import com.quizlock.core.data.QuizGateDatabase
import com.quizlock.core.data.QuizGateRepository
import com.quizlock.core.data.StarterPackImporter
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuizGateRepositoryTest {

    private lateinit var database: QuizGateDatabase
    private lateinit var repository: QuizGateRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, QuizGateDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = QuizGateRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun question(
        id: String,
        text: String,
        answer: String = "4",
        ageBand: String = "7-9",
        lang: String = "vi",
        topic: String = "Math",
        difficulty: Int = 1
    ) = QuestionEntity(
        id = id,
        type = "choice",
        topic = topic,
        ageBand = ageBand,
        difficulty = difficulty,
        lang = lang,
        question = text,
        optionsJson = "[\"3\",\"4\",\"5\"]",
        answer = answer
    )

    @Test
    fun `import skips duplicate content and updates existing ids`() = runBlocking {
        val question = question("q1", "2 + 2 = ?")

        val first = repository.importQuestions(listOf(question))
        assertEquals(1, first.imported)
        assertEquals(0, first.duplicates)

        val second = repository.importQuestions(listOf(question.copy(id = "q2")))
        assertEquals(0, second.imported)
        assertEquals(1, second.duplicates)
        assertEquals(1, repository.getQuestionsCount())

        val third = repository.importQuestions(listOf(question.copy(hint = "Count on fingers")))
        assertEquals(1, third.imported)
        assertEquals(1, repository.getQuestionsCount())
    }

    @Test
    fun `import deduplicates within a single batch`() = runBlocking {
        val first = question("q1", "same question")
        val batch = listOf(first, first.copy(id = "q2"))

        val summary = repository.importQuestions(batch)

        assertEquals(1, summary.imported)
        assertEquals(1, summary.duplicates)
        assertEquals(1, repository.getQuestionsCount())
    }

    @Test
    fun `delete all questions clears the bank and sets the seed guard`() = runBlocking {
        repository.importQuestions(
            listOf(
                question("q1", "Question one"),
                question("q2", "Question two")
            )
        )
        assertEquals(2, repository.getQuestionsCount())

        val deleted = repository.deleteAllQuestions()

        assertEquals(2, deleted)
        assertEquals(0, repository.getQuestionsCount())
        assertEquals(
            "1",
            database.configDao().get(StarterPackImporter.KEY_STARTER_SEEDED)
        )
    }

    @Test
    fun `changing pin preserves the recovery code`() = runBlocking {
        val recoveryCode = repository.setupPin("123456")
        assertTrue(repository.verifyPin("123456"))
        assertTrue(repository.verifyRecoveryCode(recoveryCode))

        repository.changePin("654321")

        assertTrue(repository.verifyPin("654321"))
        assertFalse(repository.verifyPin("123456"))
        assertTrue(repository.verifyRecoveryCode(recoveryCode))
        assertNotNull(database.configDao().get(QuizGateRepository.KEY_RECOVERY_SALT))
    }

    @Test
    fun `empty bank falls back to a generated question`() = runBlocking {
        val generated = repository.getNextQuestion(profileId = 1, ageBand = "7-9")
        assertTrue(generated.id.startsWith("fb_"))
    }
}
