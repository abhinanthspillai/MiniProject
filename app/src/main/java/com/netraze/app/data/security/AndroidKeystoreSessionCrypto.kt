package com.netraze.app.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.KeyStoreException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production Keystore-protected implementation of SessionCrypto (D090).
 * Uses exclusively AndroidKeyStore provider with AES-256 GCM.
 * Contains ZERO JVM / in-memory testing fallbacks.
 */
@Singleton
class AndroidKeystoreSessionCrypto @Inject constructor() : SessionCrypto {

    private val transformation = "AES/GCM/NoPadding"

    override fun isKeystoreProtected(): Boolean = true

    private fun getSecretKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }
            val existingEntry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            existingEntry?.secretKey ?: createAndroidKeyStoreKey()
        } catch (e: KeyStoreException) {
            throw SessionCryptoException("AndroidKeyStore provider unavailable", e)
        } catch (e: GeneralSecurityException) {
            throw SessionCryptoException("Keystore key retrieval failed", e)
        } catch (e: IOException) {
            throw SessionCryptoException("Keystore stream load failed", e)
        }
    }

    private fun createAndroidKeyStoreKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    override fun encrypt(plaintext: ByteArray): Pair<ByteArray, ByteArray> {
        return try {
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(plaintext)
            Pair(iv, ciphertext)
        } catch (e: GeneralSecurityException) {
            throw SessionCryptoException("Session encryption failed", e)
        }
    }

    override fun decrypt(iv: ByteArray, ciphertext: ByteArray): ByteArray {
        return try {
            val cipher = Cipher.getInstance(transformation)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            cipher.doFinal(ciphertext)
        } catch (e: GeneralSecurityException) {
            throw SessionCryptoException("Session decryption failed", e)
        }
    }

    companion object {
        private const val KEY_ALIAS = "NetrazeSessionKey"
        private const val GCM_TAG_LENGTH = 128
    }
}
