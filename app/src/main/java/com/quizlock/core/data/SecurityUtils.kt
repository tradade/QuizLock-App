package com.quizlock.core.data

import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {

    private const val ITERATIONS = 150_000
    private const val KEY_LENGTH = 256
    private const val SALT_BYTES = 16

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String, saltHex: String): String {
        val salt = hexToBytes(saltHex)
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = skf.generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(enteredPin: String, storedHash: String, storedSaltHex: String): Boolean {
        val computedHash = hashPin(enteredPin, storedSaltHex)
        return slowEquals(computedHash, storedHash)
    }

    /**
     * Generates an 8-character recovery code omitting ambiguous chars: 0, O, 1, I
     */
    fun generateRecoveryCode(): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val random = SecureRandom()
        return (1..8).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    private fun slowEquals(a: String, b: String): Boolean {
        var diff = a.length xor b.length
        var i = 0
        while (i < a.length && i < b.length) {
            diff = diff or (a[i].code xor b[i].code)
            i++
        }
        return diff == 0
    }

    private fun hexToBytes(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in result.indices) {
            val index = i * 2
            val byteVal = hex.substring(index, index + 2).toInt(16)
            result[i] = byteVal.toByte()
        }
        return result
    }
}
