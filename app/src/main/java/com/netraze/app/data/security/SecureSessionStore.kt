package com.netraze.app.data.security

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.UUID

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "netraze_secure_session")

data class AuthSession(
    val accessToken: String,
    val userId: UUID,
    val email: String,
    val role: String
)

/**
 * Dedicated Android SecureSessionStore (D090).
 * Session payload is encrypted via SessionCrypto interface (AndroidKeystoreSessionCrypto in production)
 * and persisted in Jetpack DataStore.
 * Completely separate from Room survey evidence.
 */
class SecureSessionStore(
    private val dataStore: DataStore<Preferences>,
    private val sessionCrypto: SessionCrypto,
    private val gson: Gson = Gson()
) {

    suspend fun saveSession(session: AuthSession) {
        val jsonPayload = gson.toJson(session)
        val (iv, ciphertext) = sessionCrypto.encrypt(jsonPayload.toByteArray(Charsets.UTF_8))

        val base64Iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        val base64Ciphertext = Base64.encodeToString(ciphertext, Base64.NO_WRAP)

        dataStore.edit { preferences ->
            preferences[KEY_SESSION_IV] = base64Iv
            preferences[KEY_SESSION_DATA] = base64Ciphertext
        }
    }

    suspend fun getSession(): AuthSession? {
        val preferences = dataStore.data.first()
        val base64Iv = preferences[KEY_SESSION_IV] ?: return null
        val base64Ciphertext = preferences[KEY_SESSION_DATA] ?: return null

        return try {
            val iv = Base64.decode(base64Iv, Base64.NO_WRAP)
            val ciphertext = Base64.decode(base64Ciphertext, Base64.NO_WRAP)

            val decryptedBytes = sessionCrypto.decrypt(iv, ciphertext)
            val jsonString = String(decryptedBytes, Charsets.UTF_8)
            gson.fromJson(jsonString, AuthSession::class.java)
        } catch (e: SessionCryptoException) {
            // Decryption key / payload failure -> safely clear unusable auth session ciphertext only.
            // Room survey evidence remains 100% untouched and preserved.
            clearSession()
            null
        } catch (e: GeneralSecurityException) {
            clearSession()
            null
        } catch (e: JsonParseException) {
            clearSession()
            null
        } catch (e: IllegalArgumentException) {
            clearSession()
            null
        } catch (e: IOException) {
            null
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_SESSION_IV)
            preferences.remove(KEY_SESSION_DATA)
        }
    }

    suspend fun hasActiveSession(): Boolean {
        val session = getSession()
        return session != null && session.accessToken.isNotBlank()
    }

    fun isKeystoreProtected(): Boolean {
        return sessionCrypto.isKeystoreProtected()
    }

    companion object {
        private val KEY_SESSION_IV = stringPreferencesKey("session_iv")
        private val KEY_SESSION_DATA = stringPreferencesKey("session_data")
    }
}
