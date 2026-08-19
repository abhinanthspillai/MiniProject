package com.netraze.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netraze.app.ui.components.LoginForm
import com.netraze.app.ui.components.NetrazeLogo
import com.netraze.app.ui.theme.FormSurfaceBlue
import com.netraze.app.ui.theme.NetrazeTheme
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryBlue
import com.netraze.app.ui.theme.Spacing
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextOnBlue
import com.netraze.app.ui.theme.TextOnBlueSecondary
import com.netraze.app.ui.theme.TextPrimary
import com.netraze.app.ui.theme.TextSecondary
import com.netraze.app.ui.theme.TopSheetShape

@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    modifier: Modifier = Modifier,
    onCreateUserClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onLoginSubmitted: (identity: String, password: String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginScreen(
        uiState = uiState,
        onIdentityChanged = viewModel::onIdentityChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onSubmit = {
            viewModel.submitLogin {
                onLoginSubmitted(uiState.identity, uiState.password)
            }
        },
        onCreateUserClick = onCreateUserClick,
        onForgotPasswordClick = onForgotPasswordClick,
        modifier = modifier
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper Branding Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .padding(top = 28.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NetrazeLogo(
                    size = 64.dp,
                    containerColor = PrimaryBlue,
                    iconColor = Color.White
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = "Netraze",
                    style = NetrazeTypography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "Indoor Wi-Fi Survey & Analysis",
                    style = NetrazeTypography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Lower Curved Blue Surface Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(TopSheetShape)
                    .background(FormSurfaceBlue)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xl, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back",
                        style = NetrazeTypography.headlineSmall,
                        color = TextOnBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = "Sign in to continue your Wi-Fi survey work.",
                        style = NetrazeTypography.bodySmall,
                        color = TextOnBlueSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Spacing.lg))

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

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    TextButton(onClick = onForgotPasswordClick) {
                        Text(
                            text = "Forgot Password?",
                            style = NetrazeTypography.bodyMedium,
                            color = TextOnBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New user? ",
                            style = NetrazeTypography.bodyMedium,
                            color = TextOnBlueSecondary
                        )
                        TextButton(onClick = onCreateUserClick) {
                            Text(
                                text = "CREATE USER",
                                style = NetrazeTypography.labelLarge,
                                color = TextOnBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Default State", heightDp = 800)
@Composable
fun LoginScreenDefaultPreview() {
    NetrazeTheme {
        LoginScreen(
            uiState = LoginUiState(),
            onIdentityChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onSubmit = {},
            onCreateUserClick = {},
            onForgotPasswordClick = {}
        )
    }
}
