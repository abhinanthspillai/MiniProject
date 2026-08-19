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

data class CreateUserUiState(
    val adminEmail: String = "",
    val adminPassword: String = "",
    val isAdminVerified: Boolean = false,
    val adminToken: String? = null,
    val newUserEmail: String = "",
    val newUserPassword: String = "",
    val newUserConfirmPassword: String = "",
    val newUserRole: String = "survey_technician",
    val isVerifyingAdmin: Boolean = false,
    val isCreatingUser: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
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

    private val _createUserState = MutableStateFlow(CreateUserUiState())
    val createUserState: StateFlow<CreateUserUiState> = _createUserState.asStateFlow()

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

        val identity = _uiState.value.identity.trim()
        val password = _uiState.value.password

        if (identity.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter your email address.") }
            return
        }

        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter your password.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.login(identity, password)
            result.onSuccess { user ->
                val session = repository.getCurrentSession()
                _uiState.update { state -> state.copy(isLoading = false, errorMessage = null) }
                _authState.update {
                    it.copy(isAuthenticated = true, session = session, userProfile = user)
                }
                onLoginSuccess()
            }.onFailure { exception ->
                _uiState.update { state ->
                    state.copy(isLoading = false, errorMessage = exception.message ?: "Invalid email address or password.")
                }
            }
        }
    }

    fun verifyAdminCredentials(onSuccess: () -> Unit = {}) {
        val repository = authRepository ?: return
        val adminEmail = _createUserState.value.adminEmail.trim()
        val adminPass = _createUserState.value.adminPassword

        if (adminEmail.isBlank() || adminPass.isBlank()) {
            _createUserState.update { it.copy(error = "Enter Administrator email and password.") }
            return
        }

        _createUserState.update { it.copy(isVerifyingAdmin = true, error = null) }

        viewModelScope.launch {
            val result = repository.verifyAdmin(adminEmail, adminPass)
            result.onSuccess { token ->
                _createUserState.update {
                    it.copy(
                        isVerifyingAdmin = false,
                        isAdminVerified = true,
                        adminToken = token,
                        error = null
                    )
                }
                onSuccess()
            }.onFailure { exception ->
                _createUserState.update {
                    it.copy(
                        isVerifyingAdmin = false,
                        isAdminVerified = false,
                        adminToken = null,
                        error = exception.message ?: "Administrator verification failed."
                    )
                }
            }
        }
    }

    fun submitCreateUser(onSuccess: (String) -> Unit = {}) {
        val repository = authRepository ?: return
        val state = _createUserState.value
        val adminToken = state.adminToken

        if (!state.isAdminVerified || adminToken.isNullOrBlank()) {
            _createUserState.update { it.copy(error = "Administrator verification required before account creation.") }
            return
        }

        val email = state.newUserEmail.trim()
        val password = state.newUserPassword
        val confirmPassword = state.newUserConfirmPassword
        val role = state.newUserRole

        if (email.isBlank()) {
            _createUserState.update { it.copy(error = "Enter new user email address.") }
            return
        }

        if (password.isBlank()) {
            _createUserState.update { it.copy(error = "Enter password for new user.") }
            return
        }

        if (password != confirmPassword) {
            _createUserState.update { it.copy(error = "Passwords do not match.") }
            return
        }

        _createUserState.update { it.copy(isCreatingUser = true, error = null) }

        viewModelScope.launch {
            val result = repository.createUser(adminToken, email, password, role)
            result.onSuccess { createdUser ->
                _createUserState.update {
                    CreateUserUiState(
                        successMessage = "Account created. ${createdUser.email} can now sign in to Netraze."
                    )
                }
                // Pre-fill email in login identity field safely
                _uiState.update { it.copy(identity = createdUser.email, password = "") }
                onSuccess(createdUser.email)
            }.onFailure { exception ->
                _createUserState.update {
                    it.copy(
                        isCreatingUser = false,
                        error = exception.message ?: "Failed to create user account."
                    )
                }
            }
        }
    }

    fun updateCreateUserForm(
        adminEmail: String = _createUserState.value.adminEmail,
        adminPassword: String = _createUserState.value.adminPassword,
        newUserEmail: String = _createUserState.value.newUserEmail,
        newUserPassword: String = _createUserState.value.newUserPassword,
        newUserConfirmPassword: String = _createUserState.value.newUserConfirmPassword,
        newUserRole: String = _createUserState.value.newUserRole
    ) {
        _createUserState.update {
            it.copy(
                adminEmail = adminEmail,
                adminPassword = adminPassword,
                newUserEmail = newUserEmail,
                newUserPassword = newUserPassword,
                newUserConfirmPassword = newUserConfirmPassword,
                newUserRole = newUserRole,
                error = null
            )
        }
    }

    fun resetCreateUserState() {
        _createUserState.update { CreateUserUiState() }
    }

    fun logout() {
        val repository = authRepository
        viewModelScope.launch {
            repository?.logout()
            _authState.update { AuthenticatedState(isAuthenticated = false) }
            _uiState.update { LoginUiState() }
            _createUserState.update { CreateUserUiState() }
        }
    }
}
