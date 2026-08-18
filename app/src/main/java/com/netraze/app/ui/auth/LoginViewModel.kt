package com.netraze.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netraze.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentityChanged(identity: String) {
        _uiState.update { it.copy(identity = identity, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun setError(message: String?) {
        _uiState.update { it.copy(errorMessage = message, isLoading = false) }
    }

    fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading, errorMessage = if (isLoading) null else it.errorMessage) }
    }

    fun submitLogin(onLoginSuccess: () -> Unit = {}) {
        val currentState = _uiState.value
        if (!currentState.isLoginEnabled || currentState.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.login(currentState.identity, currentState.password)
            result.onSuccess {
                _uiState.update { state -> state.copy(isLoading = false, errorMessage = null) }
                onLoginSuccess()
            }.onFailure { exception ->
                _uiState.update { state -> state.copy(isLoading = false, errorMessage = exception.message ?: "Authentication failed") }
            }
        }
    }
}
