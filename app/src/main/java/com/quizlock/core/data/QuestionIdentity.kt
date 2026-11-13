package com.quizlock.core.data

import java.security.MessageDigest
import java.util.Locale

/**
 * Identity helpers for question imports.
 *
 * - [stableId] is used when a source has no id: the same question/answer pair
 *   in the same age band/language keeps its primary key, so editing options or
 *   metadata on re-import updates instead of creating a new row.
 * - [contentKey] is used for duplicate detection across different ids. It ignores
 *   topic/difficulty so the same question with changed metadata does not duplicate.
 */
object QuestionIdentity {

    fun contentKey(entity: QuestionEntity): String =
        contentKey(entity.question, entity.answer, entity.ageBand, entity.lang)

    fun contentKey(question: String, answer: String, ageBand: String, lang: String): String =
        listOf(
            normalize(question),
            normalize(answer),
            ageBand.trim(),
            lang.trim().lowercase(Locale.ROOT)
        ).joinToString("|")

    fun stableId(
        question: String,
        optionsJson: String,
        answer: String,
        ageBand: String,
        lang: String
    ): String {
        val payload = listOf(
            question.trim(),
            answer.trim(),
            ageBand.trim(),
            lang.trim().lowercase(Locale.ROOT)
        ).joinToString("\u0002")
        val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
        val hex = digest.take(12).joinToString("") {
            (it.toInt() and 0xFF).toString(16).padStart(2, '0')
        }
        return "auto_$hex"
    }

    private fun normalize(value: String): String =
        value.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")
}

data class QuestionImportPlan(
    val toInsert: List<QuestionEntity>,
    val duplicates: Int
)

data class QuestionImportSummary(
    val imported: Int,
    val duplicates: Int
)

object QuestionDeduplicator {

    fun plan(
        incoming: List<QuestionEntity>,
        existingIds: Set<String>,
        existingKeys: Set<String>,
        existingIdsByKey: Map<String, String> = emptyMap()
    ): QuestionImportPlan {
        val ids = existingIds.toMutableSet()
        val keys = existingKeys.toMutableSet()
        val reusedKeys = mutableSetOf<String>()
        val toInsert = mutableListOf<QuestionEntity>()
        var duplicates = 0

        for (question in incoming) {
            val key = QuestionIdentity.contentKey(question)
            val existingIdForKey = existingIdsByKey[key]
            when {
                // Same primary key: this is an update, always allow it.
                question.id in ids -> {
                    toInsert.add(question)
                    keys.add(key)
                    reusedKeys.add(key)
                }
                // A legacy/imported row may have an id generated with a different
                // schema. Reuse its id so edited options update instead of creating
                // a second row that the content key would otherwise skip.
                existingIdForKey != null &&
                    question.id.startsWith("auto_") &&
                    key in keys &&
                    reusedKeys.add(key) -> {
                    toInsert.add(question.copy(id = existingIdForKey))
                    ids.add(existingIdForKey)
                    keys.add(key)
                }
                // Same question + answer + age band + language already exists: skip.
                !keys.add(key) -> duplicates++
                else -> {
                    toInsert.add(question)
                    ids.add(question.id)
                }
            }
        }

        return QuestionImportPlan(toInsert, duplicates)
    }
}
