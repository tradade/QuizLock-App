package com.example

import com.example.core.data.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityUtilsTest {

    @Test
    fun `pin hashing and verification works correctly`() {
        val pin = "123456"
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPin(pin, salt)

        assertTrue("Matching PIN must verify true", SecurityUtils.verifyPin(pin, hash, salt))
        assertFalse("Wrong PIN must verify false", SecurityUtils.verifyPin("654321", hash, salt))
        assertFalse("Wrong salt must verify false", SecurityUtils.verifyPin(pin, hash, SecurityUtils.generateSalt()))
    }

    @Test
    fun `recovery code has 8 chars and avoids ambiguous characters`() {
        val code = SecurityUtils.generateRecoveryCode()
        assertEquals(8, code.length)
        assertFalse("Must not contain 0", code.contains("0"))
        assertFalse("Must not contain O", code.contains("O"))
        assertFalse("Must not contain 1", code.contains("1"))
        assertFalse("Must not contain I", code.contains("I"))
    }
}
