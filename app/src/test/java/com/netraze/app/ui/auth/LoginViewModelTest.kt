package com.netraze.app.ui.auth

import com.netraze.app.data.remote.api.AuthApi
import com.netraze.app.data.remote.dto.CreateUserRequestDto
import com.netraze.app.data.remote.dto.LoginRequestDto
import com.netraze.app.data.remote.dto.LoginResponseDto
import com.netraze.app.data.remote.dto.ResetPasswordRequestDto
import com.netraze.app.data.remote.dto.ResetPasswordResponseDto
import com.netraze.app.data.remote.dto.UserDto
import com.netraze.app.data.repository.AuthRepository
import com.netraze.app.data.security.AuthSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeAuthApi: FakeAuthApi
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        fakeAuthApi = FakeAuthApi()
        viewModel = LoginViewModel(fakeAuthRepository, fakeAuthApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testFormStateValidation() {
        val uiState = viewModel.uiState

        assertFalse(uiState.value.isLoginEnabled)

        viewModel.onIdentityChanged("tech@netraze.app")
        assertFalse(uiState.value.isLoginEnabled)

        viewModel.onPasswordChanged("Password123")
        assertTrue(uiState.value.isLoginEnabled)
    }

    @Test
    fun testSuccessfulLoginFlow() = runTest {
        viewModel.onIdentityChanged("tech@netraze.app")
        viewModel.onPasswordChanged("CorrectPassword")

        var loginSuccessCalled = false
        viewModel.submitLogin {
            loginSuccessCalled = true
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(loginSuccessCalled)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.authState.value.isAuthenticated)
    }

    @Test
    fun testFailedLoginFlow() = runTest {
        fakeAuthRepository.shouldReturnError = true

        viewModel.onIdentityChanged("tech@netraze.app")
        viewModel.onPasswordChanged("WrongPassword")

        viewModel.submitLogin()

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Invalid email address or password.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.authState.value.isAuthenticated)
    }

    @Test
    fun testVerifyAdminAndCreateUserFlow() = runTest {
        viewModel.updateCreateUserForm(
            adminEmail = "admin@netraze.app",
            adminPassword = "AdminPassword123"
        )

        viewModel.verifyAdminCredentials()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.createUserState.value.isAdminVerified)
        assertNotNull(viewModel.createUserState.value.adminToken)

        viewModel.updateCreateUserForm(
            newUserEmail = "newtech@netraze.app",
            newUserPassword = "Password123!",
            newUserConfirmPassword = "Password123!",
            newUserRole = "survey_technician"
        )

        var createdEmail: String? = null
        viewModel.submitCreateUser { email ->
            createdEmail = email
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("newtech@netraze.app", createdEmail)
        assertEquals("newtech@netraze.app", viewModel.uiState.value.identity)
    }

    @Test
    fun testResetPasswordFlow() = runTest {
        viewModel.updateResetPasswordForm(
            adminEmail = "admin@netraze.app",
            adminPassword = "AdminPassword123"
        )

        viewModel.verifyAdminCredentialsForReset()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.resetPasswordState.value.isAdminVerified)
        assertNotNull(viewModel.resetPasswordState.value.adminToken)

        viewModel.updateResetPasswordForm(
            targetUserEmail = "tech@netraze.app",
            newPassword = "NewPassword123!",
            confirmNewPassword = "NewPassword123!"
        )

        var resetTargetEmail: String? = null
        viewModel.submitResetPassword { email ->
            resetTargetEmail = email
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("tech@netraze.app", resetTargetEmail)
        assertEquals("tech@netraze.app", viewModel.uiState.value.identity)
    }

    private class FakeAuthRepository : AuthRepository {
        var shouldReturnError = false
        private var currentSession: AuthSession? = null

        override suspend fun login(email: String, password: String): Result<UserDto> {
            return if (shouldReturnError) {
                Result.failure(Exception("Invalid email address or password."))
            } else {
                val userId = UUID.randomUUID()
                currentSession = AuthSession("token_123", userId, email, "survey_technician")
                Result.success(UserDto(id = userId, email = email, role = "survey_technician"))
            }
        }

        override suspend fun logout() {
            currentSession = null
        }

        override suspend fun hasActiveSession(): Boolean = currentSession != null
        override suspend fun getCurrentSession(): AuthSession? = currentSession

        override suspend fun verifyAdmin(email: String, password: String): Result<String> {
            return if (shouldReturnError) {
                Result.failure(Exception("Administrator verification failed."))
            } else {
                Result.success("admin_token_xyz")
            }
        }

        override suspend fun createUser(
            adminToken: String,
            email: String,
            password: String,
            role: String
        ): Result<UserDto> {
            return Result.success(UserDto(id = UUID.randomUUID(), email = email, role = role))
        }

        override suspend fun resetPassword(
            adminToken: String,
            targetEmail: String,
            newPassword: String
        ): Result<String> {
            return Result.success("Password reset successfully.")
        }
    }

    private class FakeAuthApi : AuthApi {
        override suspend fun login(request: LoginRequestDto): LoginResponseDto {
            val userId = UUID.randomUUID()
            return LoginResponseDto("token_123", "bearer", UserDto(userId, request.email, "survey_technician"))
        }

        override suspend fun getMe(): UserDto {
            return UserDto(UUID.randomUUID(), "tech@netraze.app", "survey_technician")
        }

        override suspend fun createUser(
            authorizationToken: String,
            request: CreateUserRequestDto
        ): UserDto {
            return UserDto(UUID.randomUUID(), request.email, request.role)
        }

        override suspend fun resetPassword(
            authorizationToken: String,
            request: ResetPasswordRequestDto
        ): ResetPasswordResponseDto {
            return ResetPasswordResponseDto("Password reset successfully.", request.targetEmail)
        }
    }
}
