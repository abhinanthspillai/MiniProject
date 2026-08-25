package com.netraze.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.netraze.app.ui.components.LoginForm
import com.netraze.app.ui.components.NetrazeLogo
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryDark
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextPrimary
import com.netraze.app.ui.theme.TextSecondary

@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    onCreateUserClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginSubmitted: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginScreen(
        uiState = uiState,
        onIdentityChanged = { viewModel.onIdentityChanged(it) },
        onPasswordChanged = { viewModel.onPasswordChanged(it) },
        onTogglePasswordVisibility = { viewModel.togglePasswordVisibility() },
        onSubmit = {
            viewModel.submitLogin()
            onLoginSubmitted(uiState.identity, uiState.password)
        },
        onCreateUserClick = onCreateUserClick,
        onForgotPasswordClick = onForgotPasswordClick
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onIdentityChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onCreateUserClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = SurfaceLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            NetrazeLogo(
                size = 64.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome Back",
                style = NetrazeTypography.displaySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in to continue your Wi-Fi survey work.",
                style = NetrazeTypography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            LoginForm(
                identity = uiState.identity,
                onIdentityChange = onIdentityChanged,
                password = uiState.password,
                onPasswordChange = onPasswordChanged,
                isPasswordVisible = uiState.isPasswordVisible,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                onSubmit = onSubmit,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                isLoginEnabled = uiState.isLoginEnabled
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onForgotPasswordClick) {
                    Text(
                        text = "Forgot Password?",
                        style = NetrazeTypography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(onClick = onCreateUserClick) {
                    Text(
                        text = "Create Account",
                        style = NetrazeTypography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
