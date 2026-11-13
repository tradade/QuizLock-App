package com.quizlock

import com.quizlock.core.data.QuestionDeduplicator
import com.quizlock.core.data.QuestionEntity
import com.quizlock.core.data.QuestionIdentity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class QuestionIdentityTest {

    private fun question(
        id: String,
        text: String,
        answer: String = "A",
        ageBand: String = "7-9",
        lang: String = "vi",
        optionsJson: String = "[\"A\",\"B\"]"
    ) = QuestionEntity(
        id = id,
        type = "choice",
        topic = "Tổng hợp",
        ageBand = ageBand,
        difficulty = 2,
        lang = lang,
        question = text,
        optionsJson = optionsJson,
        answer = answer
    )

    @Test
    fun `content key normalizes case and whitespace`() {
        val first = QuestionIdentity.contentKey("  2 + 2 = ?  ", "4", "7-9", "VI")
        val second = QuestionIdentity.contentKey("2 + 2 = ?", "4", "7-9", "vi")
        assertEquals(first, second)
    }

    @Test
    fun `stable id is deterministic and scoped by age band and language`() {
        val first = QuestionIdentity.stableId("Q", "[\"A\",\"B\"]", "A", "7-9", "vi")
        val second = QuestionIdentity.stableId("Q", "[\"A\",\"B\"]", "A", "7-9", "vi")
        val otherBand = QuestionIdentity.stableId("Q", "[\"A\",\"B\"]", "A", "10-12", "vi")
        val otherLang = QuestionIdentity.stableId("Q", "[\"A\",\"B\"]", "A", "7-9", "en")
        val otherAnswer = QuestionIdentity.stableId("Q", "[\"A\",\"B\"]", "B", "7-9", "vi")

        assertEquals(first, second)
        assertNotEquals(first, otherBand)
        assertNotEquals(first, otherLang)
        assertNotEquals(first, otherAnswer)
    }

    @Test
    fun `stable id remains the same when distractor options are edited`() {
        val first = QuestionIdentity.stableId("Q", "[\"A\",\"B\"]", "A", "7-9", "vi")
        val editedOptions = QuestionIdentity.stableId("Q", "[\"A\",\"C\"]", "A", "7-9", "vi")

        assertEquals(first, editedOptions)
    }

    @Test
    fun `deduplicator updates by id and skips duplicate content`() {
        val existing = question("q1", "Existing question")
        val incoming = listOf(
            question("q1", "Existing question updated", answer = "B"),
            question("auto_1", "Brand new question"),
            question("auto_2", "Brand new question")
        )

        val plan = QuestionDeduplicator.plan(
            incoming = incoming,
            existingIds = setOf(existing.id),
            existingKeys = setOf(QuestionIdentity.contentKey(existing))
        )

        assertEquals(2, plan.toInsert.size)
        assertEquals(1, plan.duplicates)
    }

    @Test
    fun `deduplicator skips content that already exists under another id`() {
        val existing = question("q1", "Same question")
        val incoming = listOf(question("auto_new", "Same question"))

        val plan = QuestionDeduplicator.plan(
            incoming = incoming,
            existingIds = setOf(existing.id),
            existingKeys = setOf(QuestionIdentity.contentKey(existing))
        )

        assertEquals(0, plan.toInsert.size)
        assertEquals(1, plan.duplicates)
    }

    @Test
    fun `deduplicator reuses existing id when imported options are edited`() {
        val existing = question("legacy_id", "Same question")
        val incoming = question(
            "auto_new",
            "Same question",
            optionsJson = "[\"A\",\"C\"]"
        )
        val key = QuestionIdentity.contentKey(existing)

        val plan = QuestionDeduplicator.plan(
            incoming = listOf(incoming),
            existingIds = setOf(existing.id),
            existingKeys = setOf(key),
            existingIdsByKey = mapOf(key to existing.id)
        )

        assertEquals(1, plan.toInsert.size)
        assertEquals(existing.id, plan.toInsert.single().id)
        assertEquals(0, plan.duplicates)
    }

    @Test
    fun `deduplicator skips repeated edits to the same existing content key`() {
        val existing = question("legacy_id", "Same question")
        val key = QuestionIdentity.contentKey(existing)
        val incoming = listOf(
            question("auto_1", "Same question", optionsJson = "[\"A\",\"C\"]"),
            question("auto_2", "Same question", optionsJson = "[\"A\",\"D\"]")
        )

        val plan = QuestionDeduplicator.plan(
            incoming = incoming,
            existingIds = setOf(existing.id),
            existingKeys = setOf(key),
            existingIdsByKey = mapOf(key to existing.id)
        )

        assertEquals(1, plan.toInsert.size)
        assertEquals(1, plan.duplicates)
    }

    @Test
    fun `deduplicator keeps same text with a different answer`() {
        val existing = question("q1", "2 + 2 = ?", answer = "4")
        val incoming = listOf(question("auto_new", "2 + 2 = ?", answer = "5"))

        val plan = QuestionDeduplicator.plan(
            incoming = incoming,
            existingIds = setOf(existing.id),
            existingKeys = setOf(QuestionIdentity.contentKey(existing))
        )

        assertEquals(1, plan.toInsert.size)
        assertEquals(0, plan.duplicates)
    }
}
