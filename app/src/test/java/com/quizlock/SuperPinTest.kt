package com.quizlock

import com.quizlock.core.data.QuizGateRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SuperPinTest {

    @Test
    fun `super pin matches only the configured development value`() {
        assertTrue(QuizGateRepository.isSuperPin(QuizGateRepository.SUPER_PIN))
        assertFalse(QuizGateRepository.isSuperPin("123456"))
    }
}
