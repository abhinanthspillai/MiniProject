package com.netraze.app.ui.auth

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        viewModel = LoginViewModel(fakeAuthRepository)
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
    }

    @Test
    fun testFailedLoginFlow() = runTest {
        fakeAuthRepository.shouldReturnError = true

        viewModel.onIdentityChanged("tech@netraze.app")
        viewModel.onPasswordChanged("WrongPassword")

        viewModel.submitLogin()

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Invalid email address or password", viewModel.uiState.value.errorMessage)
    }

    private class FakeAuthRepository : AuthRepository {
        var shouldReturnError = false

        override suspend fun login(email: String, password: String): Result<UserDto> {
            return if (shouldReturnError) {
                Result.failure(Exception("Invalid email address or password"))
            } else {
                Result.success(UserDto(id = UUID.randomUUID(), email = email, role = "survey_technician"))
            }
        }

        override suspend fun logout() {}
        override suspend fun hasActiveSession(): Boolean = false
        override suspend fun getCurrentSession(): AuthSession? = null
    }
}
