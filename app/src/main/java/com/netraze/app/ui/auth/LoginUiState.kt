package com.netraze.app.ui.auth

data class LoginUiState(
    val identity: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isLoginEnabled: Boolean
        get() = identity.isNotBlank() && password.isNotBlank() && !isLoading
}
