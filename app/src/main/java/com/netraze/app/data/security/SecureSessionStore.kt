package com.netraze.app.data.security

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "netraze_secure_session")

data class AuthSession(
    val accessToken: String,
    val userId: UUID,
    val email: String,
    val role: String
)

/**
 * Dedicated Android SecureSessionStore (D090).
 * Key material is hardware-backed inside Android Keystore via CryptoManager.
 * Session payload is encrypted with AES/GCM and stored inside Jetpack DataStore.
 * Completely separate from Room survey evidence.
 */
class SecureSessionStore(
    private val context: Context,
    private val cryptoManager: CryptoManager = CryptoManager(),
    private val gson: Gson = Gson()
) {

    suspend fun saveSession(session: AuthSession) {
        val jsonPayload = gson.toJson(session)
        val (iv, ciphertext) = cryptoManager.encrypt(jsonPayload.toByteArray(Charsets.UTF_8))

        val base64Iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        val base64Ciphertext = Base64.encodeToString(ciphertext, Base64.NO_WRAP)

        context.dataStore.edit { preferences ->
            preferences[KEY_SESSION_IV] = base64Iv
            preferences[KEY_SESSION_DATA] = base64Ciphertext
        }
    }

    suspend fun getSession(): AuthSession? {
        val preferences = context.dataStore.data.first()
        val base64Iv = preferences[KEY_SESSION_IV] ?: return null
        val base64Ciphertext = preferences[KEY_SESSION_DATA] ?: return null

        return try {
            val iv = Base64.decode(base64Iv, Base64.NO_WRAP)
            val ciphertext = Base64.decode(base64Ciphertext, Base64.NO_WRAP)

            val decryptedBytes = cryptoManager.decrypt(iv, ciphertext)
            val jsonString = String(decryptedBytes, Charsets.UTF_8)
            gson.fromJson(jsonString, AuthSession::class.java)
        } catch (e: Exception) {
            // Decryption failure or corrupted store -> clear session safely
            clearSession()
            null
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_SESSION_IV)
            preferences.remove(KEY_SESSION_DATA)
        }
    }

    suspend fun hasActiveSession(): Boolean {
        val session = getSession()
        return session != null && session.accessToken.isNotBlank()
    }

    companion object {
        private val KEY_SESSION_IV = stringPreferencesKey("session_iv")
        private val KEY_SESSION_DATA = stringPreferencesKey("session_data")
    }
}
