package com.netraze.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netraze.app.data.remote.api.AuthApi
import com.netraze.app.data.remote.dto.UserDto
import com.netraze.app.data.repository.AuthRepository
import com.netraze.app.data.security.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthenticatedState(
    val isAuthenticated: Boolean = false,
    val session: AuthSession? = null,
    val userProfile: UserDto? = null,
    val isFetchingProfile: Boolean = false,
    val profileError: String? = null
)

class LoginViewModel @Inject constructor(
    private var authRepository: AuthRepository?,
    private var authApi: AuthApi?
) : ViewModel() {

    constructor() : this(null, null)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _authState = MutableStateFlow(AuthenticatedState())
    val authState: StateFlow<AuthenticatedState> = _authState.asStateFlow()

    init {
        checkSessionRestoration()
    }

    fun setDependencies(repository: AuthRepository, api: AuthApi) {
        this.authRepository = repository
        this.authApi = api
        checkSessionRestoration()
    }

    fun checkSessionRestoration() {
        val repository = authRepository ?: return
        viewModelScope.launch {
            val session = repository.getCurrentSession()
            if (session != null && session.accessToken.isNotBlank()) {
                _authState.update {
                    it.copy(isAuthenticated = true, session = session)
                }
                fetchUserProfile()
            } else {
                _authState.update { AuthenticatedState(isAuthenticated = false) }
            }
        }
    }

    fun fetchUserProfile() {
        val api = authApi ?: return
        _authState.update { it.copy(isFetchingProfile = true, profileError = null) }
        viewModelScope.launch {
            try {
                val profile = api.getMe()
                _authState.update {
                    it.copy(userProfile = profile, isFetchingProfile = false)
                }
            } catch (e: Exception) {
                _authState.update {
                    it.copy(isFetchingProfile = false, profileError = e.message ?: "Failed to fetch user profile")
                }
            }
        }
    }

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
        val repository = authRepository
        if (repository == null) {
            _uiState.update { it.copy(errorMessage = "Authentication repository uninitialized") }
            return
        }
        val currentState = _uiState.value
        if (!currentState.isLoginEnabled || currentState.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.login(currentState.identity, currentState.password)
            result.onSuccess { user ->
                val session = repository.getCurrentSession()
                _uiState.update { state -> state.copy(isLoading = false, errorMessage = null) }
                _authState.update {
                    it.copy(isAuthenticated = true, session = session, userProfile = user)
                }
                onLoginSuccess()
            }.onFailure { exception ->
                _uiState.update { state ->
                    state.copy(isLoading = false, errorMessage = exception.message ?: "Authentication failed")
                }
            }
        }
    }

    fun logout() {
        val repository = authRepository
        viewModelScope.launch {
            repository?.logout()
            _authState.update { AuthenticatedState(isAuthenticated = false) }
            _uiState.update { LoginUiState() }
        }
    }
}
