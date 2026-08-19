package com.netraze.app.data.repository

import com.netraze.app.data.remote.api.AuthApi
import com.netraze.app.data.remote.dto.CreateUserRequestDto
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
    suspend fun verifyAdmin(email: String, password: String): Result<String>
    suspend fun createUser(adminToken: String, email: String, password: String, role: String): Result<UserDto>
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
            val errorMsg = when (e.code()) {
                401 -> "Invalid email address or password."
                403 -> "Not authorized to perform this operation."
                else -> "Authentication server error (${e.code()})."
            }
            Result.failure(Exception(errorMsg, e))
        } catch (e: IOException) {
            Result.failure(Exception("Unable to connect to Netraze. Check your connection and try again.", e))
        } catch (e: Exception) {
            Result.failure(Exception("An unexpected authentication error occurred.", e))
        }
    }

    override suspend fun logout() {
        sessionStore.clearSession()
    }

    override suspend fun hasActiveSession(): Boolean {
        return sessionStore.hasActiveSession()
    }

    override suspend fun getCurrentSession(): AuthSession? {
        return sessionStore.getSession()
    }

    override suspend fun verifyAdmin(email: String, password: String): Result<String> {
        return try {
            val response = authApi.login(LoginRequestDto(email = email.trim(), password = password))
            if (response.user.role.lowercase() != "administrator") {
                Result.failure(Exception("Administrator authorization required. Provided user is not an administrator."))
            } else {
                Result.success(response.accessToken)
            }
        } catch (e: HttpException) {
            Result.failure(Exception("Administrator verification failed. Invalid administrator email or password.", e))
        } catch (e: IOException) {
            Result.failure(Exception("Unable to connect to Netraze. Check your connection and try again.", e))
        } catch (e: Exception) {
            Result.failure(Exception("Administrator verification failed.", e))
        }
    }

    override suspend fun createUser(
        adminToken: String,
        email: String,
        password: String,
        role: String
    ): Result<UserDto> {
        return try {
            val authHeader = if (adminToken.startsWith("Bearer ")) adminToken else "Bearer $adminToken"
            val dto = authApi.createUser(
                authorizationToken = authHeader,
                request = CreateUserRequestDto(email = email.trim(), password = password, role = role)
            )
            Result.success(dto)
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                403 -> "Administrator authorization required to create accounts."
                409 -> "An account with this email address already exists."
                422 -> "Invalid account details provided."
                else -> "Failed to create account (${e.code()})."
            }
            Result.failure(Exception(errorMsg, e))
        } catch (e: IOException) {
            Result.failure(Exception("Unable to connect to Netraze. Check your connection and try again.", e))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create account.", e))
        }
    }
}
