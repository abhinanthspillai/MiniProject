package com.netraze.app.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Hardware-backed direct Android Keystore CryptoManager (D090).
 * Generates and stores AES-256 GCM key inside AndroidKeyStore.
 * Provides platform AES/GCM/NoPadding encryption and decryption.
 */
class CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val transformation = "AES/GCM/NoPadding"

    private fun getSecretKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createSecretKey()
    }

    private fun createSecretKey(): SecretKey {
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    fun encrypt(bytes: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(bytes)
        return Pair(iv, ciphertext)
    }

    fun decrypt(iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(transformation)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        return cipher.doFinal(ciphertext)
    }

    companion object {
        private const val KEY_ALIAS = "NetrazeSessionKey"
        private const val GCM_TAG_LENGTH = 128
    }
}
