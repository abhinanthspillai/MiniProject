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
    val newUserEmail: String = "",
    val newUserPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class ResetPasswordUiState(
    val adminEmail: String = "",
    val adminPassword: String = "",
    val isAdminVerified: Boolean = false,
    val adminToken: String? = null,
    val targetUserEmail: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isVerifyingAdmin: Boolean = false,
    val isResettingPassword: Boolean = false,
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

    private val _resetPasswordState = MutableStateFlow(ResetPasswordUiState())
    val resetPasswordState: StateFlow<ResetPasswordUiState> = _resetPasswordState.asStateFlow()

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
                    it.copy(
                        userProfile = profile,
                        isFetchingProfile = false
                    )
                }
            } catch (e: Exception) {
                _authState.update {
                    it.copy(
                        isFetchingProfile = false,
                        profileError = e.message ?: "Failed to fetch user profile"
                    )
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

    fun submitLogin(onSuccess: () -> Unit = {}) {
        val repository = authRepository
        if (repository == null) {
            _uiState.update { it.copy(errorMessage = "Auth repository not initialized") }
            return
        }

        val currentState = _uiState.value
        if (!currentState.isLoginEnabled) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.login(currentState.identity, currentState.password)
            if (result.isSuccess) {
                val user = result.getOrNull()
                if (user != null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                    _authState.update {
                        it.copy(
                            isAuthenticated = true,
                            userProfile = user
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Login failed: missing user profile data") }
                }
            } else {
                val exception = result.exceptionOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception?.message ?: "Login failed due to an unknown error."
                    )
                }
            }
        }
    }

    fun logout() {
        val repository = authRepository ?: return
        viewModelScope.launch {
            repository.logout()
            _authState.update {
                AuthenticatedState(isAuthenticated = false)
            }
            _uiState.update {
                LoginUiState()
            }
        }
    }

    // --- Create User Flow ---

    fun updateCreateUserForm(
        newUserEmail: String = _createUserState.value.newUserEmail,
        newUserPassword: String = _createUserState.value.newUserPassword,
        confirmPassword: String = _createUserState.value.confirmPassword
    ) {
        _createUserState.update {
            it.copy(
                newUserEmail = newUserEmail,
                newUserPassword = newUserPassword,
                confirmPassword = confirmPassword,
                errorMessage = null
            )
        }
    }

    fun submitCreateUser() {
        val api = authApi ?: return
        val state = _createUserState.value

        if (state.newUserPassword != state.confirmPassword) {
            _createUserState.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }
        if (state.newUserEmail.isBlank() || state.newUserPassword.isBlank()) {
             _createUserState.update { it.copy(errorMessage = "Email and Password are required") }
            return
        }

        _createUserState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // Call standard public register
                api.registerUser(
                    com.netraze.app.data.remote.dto.RegisterRequestDto(
                        email = state.newUserEmail,
                        password = state.newUserPassword,
                        confirm_password = state.confirmPassword
                    )
                )
                _createUserState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _createUserState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to create account"
                    )
                }
            }
        }
    }

    fun resetCreateUserForm() {
        _createUserState.update { CreateUserUiState() }
    }

    // --- Reset Password Flow ---

    fun updateResetPasswordAdminForm(
        adminEmail: String = _resetPasswordState.value.adminEmail,
        adminPassword: String = _resetPasswordState.value.adminPassword
    ) {
        _resetPasswordState.update {
            it.copy(
                adminEmail = adminEmail,
                adminPassword = adminPassword,
                error = null
            )
        }
    }

    fun verifyAdminForResetPassword() {
        val api = authApi ?: return
        val state = _resetPasswordState.value

        _resetPasswordState.update { it.copy(isVerifyingAdmin = true, error = null) }
        viewModelScope.launch {
            try {
                val tokenResponse = api.login(
                    com.netraze.app.data.remote.dto.LoginRequestDto(
                        email = state.adminEmail,
                        password = state.adminPassword
                    )
                )
                
                if (tokenResponse.user.role.lowercase() != "administrator") {
                    _resetPasswordState.update {
                        it.copy(
                            isVerifyingAdmin = false,
                            error = "Account is not an administrator."
                        )
                    }
                    return@launch
                }
                
                _resetPasswordState.update {
                    it.copy(
                        isVerifyingAdmin = false,
                        isAdminVerified = true,
                        adminToken = tokenResponse.accessToken
                    )
                }
            } catch (e: Exception) {
                _resetPasswordState.update {
                    it.copy(
                        isVerifyingAdmin = false,
                        error = "Failed to verify admin credentials. ${e.message}"
                    )
                }
            }
        }
    }

    fun updateResetPasswordForm(
        targetUserEmail: String = _resetPasswordState.value.targetUserEmail,
        newPassword: String = _resetPasswordState.value.newPassword,
        confirmNewPassword: String = _resetPasswordState.value.confirmNewPassword
    ) {
        _resetPasswordState.update {
            it.copy(
                targetUserEmail = targetUserEmail,
                newPassword = newPassword,
                confirmNewPassword = confirmNewPassword,
                error = null
            )
        }
    }

    fun submitResetPassword() {
        val api = authApi ?: return
        val state = _resetPasswordState.value

        if (!state.isAdminVerified || state.adminToken == null) {
            _resetPasswordState.update { it.copy(error = "Admin not verified.") }
            return
        }

        if (state.newPassword != state.confirmNewPassword) {
            _resetPasswordState.update { it.copy(error = "New passwords do not match.") }
            return
        }

        _resetPasswordState.update { it.copy(isResettingPassword = true, error = null) }
        viewModelScope.launch {
            try {
                val response = api.resetPassword(
                    authorizationToken = "Bearer ${state.adminToken}",
                    request = com.netraze.app.data.remote.dto.ResetPasswordRequestDto(
                        targetEmail = state.targetUserEmail,
                        newPassword = state.newPassword
                    )
                )
                _resetPasswordState.update {
                    it.copy(
                        isResettingPassword = false,
                        successMessage = response.message
                    )
                }
            } catch (e: Exception) {
                _resetPasswordState.update {
                    it.copy(
                        isResettingPassword = false,
                        error = "Failed to reset password. ${e.message}"
                    )
                }
            }
        }
    }

    fun resetResetPasswordForm() {
        _resetPasswordState.update { ResetPasswordUiState() }
    }
}
