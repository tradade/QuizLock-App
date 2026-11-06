package com.quizlock

import com.quizlock.core.data.FallbackQuestionGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FallbackQuestionGeneratorTest {

    @Test
    fun `generate band 4-6 produces valid arithmetic and options`() {
        val q = FallbackQuestionGenerator.generate("4-6")
        assertEquals("4-6", q.ageBand)
        assertEquals("toán", q.topic)
        assertNotNull(q.answer)
        assertNotNull(q.hint)
        assertNotNull(q.explanation)
        assertTrue(q.optionsJson.contains(q.answer))
    }

    @Test
    fun `generate band 7-9 produces valid multiplication or division`() {
        val q = FallbackQuestionGenerator.generate("7-9")
        assertEquals("7-9", q.ageBand)
        assertNotNull(q.answer)
        assertTrue(q.optionsJson.contains(q.answer))
    }

    @Test
    fun `generate band 10-12 produces valid advanced math`() {
        val q = FallbackQuestionGenerator.generate("10-12")
        assertEquals("10-12", q.ageBand)
        assertNotNull(q.answer)
        assertTrue(q.optionsJson.contains(q.answer))
    }
}
