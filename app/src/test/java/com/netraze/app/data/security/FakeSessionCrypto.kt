package com.netraze.app.data.security

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Test-only implementation of SessionCrypto for JVM unit tests (D090).
 * Resides in test source set; NEVER packaged or available in production builds.
 */
class FakeSessionCrypto : SessionCrypto {

    private val secretKey: SecretKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    private val transformation = "AES/GCM/NoPadding"

    override fun isKeystoreProtected(): Boolean = false

    override fun encrypt(plaintext: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        return Pair(iv, ciphertext)
    }

    override fun decrypt(iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(transformation)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(ciphertext)
    }
}
