package com.netraze.app.data.repository

import com.netraze.app.data.remote.api.AuthApi
import com.netraze.app.data.remote.dto.LoginRequestDto
import com.netraze.app.data.remote.dto.UserDto
import com.netraze.app.data.security.AuthSession
import com.netraze.app.data.security.SecureSessionStore
import retrofit2.HttpException
import java.io.IOException

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserDto>
    suspend fun logout()
    suspend fun hasActiveSession(): Boolean
    suspend fun getCurrentSession(): AuthSession?
}

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val sessionStore: SecureSessionStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserDto> {
        return try {
            val response = authApi.login(LoginRequestDto(email = email.trim(), password = password))
            val session = AuthSession(
                accessToken = response.accessToken,
                userId = response.user.id,
                email = response.user.email,
                role = response.user.role
            )
            sessionStore.saveSession(session)
            Result.success(response.user)
        } catch (e: HttpException) {
            val errorMsg = if (e.code() == 401) {
                "Invalid email address or password"
            } else {
                "Authentication server error (${e.code()})"
            }
            Result.failure(Exception(errorMsg, e))
        } catch (e: IOException) {
            Result.failure(Exception("Backend server unavailable. Please check your connection.", e))
        } catch (e: Exception) {
            Result.failure(Exception("An unexpected authentication error occurred.", e))
        }
    }

    override suspend fun logout() {
        // Clears encrypted session store material only.
        // Room survey evidence remains 100% intact and untampered per D090 §13.
        sessionStore.clearSession()
    }

    override suspend fun hasActiveSession(): Boolean {
        return sessionStore.hasActiveSession()
    }

    override suspend fun getCurrentSession(): AuthSession? {
        return sessionStore.getSession()
    }
}
