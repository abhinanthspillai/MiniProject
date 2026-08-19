package com.netraze.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class UserDto(
    @SerializedName("id") val id: UUID,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String
)

data class LoginResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("user") val user: UserDto
)

data class CreateUserRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String
)

data class ResetPasswordRequestDto(
    @SerializedName("target_email") val targetEmail: String,
    @SerializedName("new_password") val newPassword: String
)

data class ResetPasswordResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("target_email") val targetEmail: String
)
