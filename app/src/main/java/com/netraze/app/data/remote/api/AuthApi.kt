package com.netraze.app.data.remote.api

import com.netraze.app.data.remote.dto.CreateUserRequestDto
import com.netraze.app.data.remote.dto.LoginRequestDto
import com.netraze.app.data.remote.dto.LoginResponseDto
import com.netraze.app.data.remote.dto.ResetPasswordRequestDto
import com.netraze.app.data.remote.dto.ResetPasswordResponseDto
import com.netraze.app.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): LoginResponseDto

    @GET("api/v1/auth/me")
    suspend fun getMe(): UserDto

    @POST("api/v1/users")
    suspend fun createUser(
        @Header("Authorization") authorizationToken: String,
        @Body request: CreateUserRequestDto
    ): UserDto

    @POST("api/v1/auth/register")
    suspend fun registerUser(
        @Body request: com.netraze.app.data.remote.dto.RegisterRequestDto
    ): UserDto

    @POST("api/v1/users/reset-password")
    suspend fun resetPassword(
        @Header("Authorization") authorizationToken: String,
        @Body request: ResetPasswordRequestDto
    ): ResetPasswordResponseDto
}
