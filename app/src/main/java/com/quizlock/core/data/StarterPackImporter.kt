package com.quizlock.core.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

object StarterPackImporter {

    private const val TAG = "StarterPackImporter"
    const val ASSET_FILE = "questions-starter.json"
    const val KEY_STARTER_SEEDED = "starter_pack_seeded"

    fun parseQuestionsFromJson(jsonString: String): List<QuestionEntity> {
        val jsonArray = JSONArray(jsonString.trim())
        val questionList = mutableListOf<QuestionEntity>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val type = obj.optString("type", "choice")
            val topic = obj.optString("topic", "Tổng hợp")
            val ageBand = obj.optString("age_band", "7-9")
            val difficulty = obj.optInt("difficulty", 1)
            val lang = obj.optString("lang", "vi")
            val question = obj.getString("question")

            val optionsArray = if (obj.has("options")) {
                obj.getJSONArray("options")
            } else {
                JSONArray().apply {
                    put("A")
                    put("B")
                    put("C")
                    put("D")
                }
            }
            val optionsJson = optionsArray.toString()
            val answer = obj.getString("answer")
            val hint = if (obj.has("hint") && !obj.isNull("hint")) obj.getString("hint") else null
            val explanation = if (obj.has("explanation") && !obj.isNull("explanation")) obj.getString("explanation") else null
            val active = obj.optInt("active", 1)

            val id = if (obj.has("id") && obj.getString("id").isNotBlank()) {
                obj.getString("id")
            } else {
                QuestionIdentity.stableId(question, optionsJson, answer, ageBand, lang)
            }

            questionList.add(
                QuestionEntity(
                    id = id,
                    type = type,
                    topic = topic,
                    ageBand = ageBand,
                    difficulty = difficulty,
                    lang = lang,
                    question = question,
                    optionsJson = optionsJson,
                    answer = answer,
                    hint = hint,
                    explanation = explanation,
                    active = active
                )
            )
        }
        return questionList
    }

    fun parseQuestionsFromAsset(context: Context): List<QuestionEntity> {
        val jsonString = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        return parseQuestionsFromJson(jsonString)
    }

    suspend fun importFromAsset(context: Context, database: QuizGateDatabase): Int = withContext(Dispatchers.IO) {
        val questions = parseQuestionsFromAsset(context)
        database.questionDao().insertQuestions(questions)
        questions.size
    }

    suspend fun importIfNeeded(context: Context, database: QuizGateDatabase) = withContext(Dispatchers.IO) {
        try {
            // Seed only once. Without this flag, deleting the bank would be undone
            // on the next launch because the room table would look like a fresh install.
            if (database.configDao().get(KEY_STARTER_SEEDED) == "1") {
                return@withContext
            }

            val count = database.questionDao().getQuestionCount()
            if (count == 0) {
                val importedCount = importFromAsset(context, database)
                Log.d(TAG, "Successfully seeded $importedCount starter questions into Room DB.")
            } else {
                Log.d(TAG, "Database already has $count questions. Skipping starter import.")
            }

            database.configDao().set(ConfigEntity(KEY_STARTER_SEEDED, "1"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import starter pack from assets", e)
        }
    }
}
