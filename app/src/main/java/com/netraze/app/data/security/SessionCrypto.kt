package com.netraze.app.data.security

class SessionCryptoException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Abstraction for session cryptographic operations (D090).
 */
interface SessionCrypto {
    fun isKeystoreProtected(): Boolean
    fun encrypt(plaintext: ByteArray): Pair<ByteArray, ByteArray>
    fun decrypt(iv: ByteArray, ciphertext: ByteArray): ByteArray
}
