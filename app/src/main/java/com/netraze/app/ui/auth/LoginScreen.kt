package com.netraze.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    modifier: Modifier = Modifier
) {
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

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
            // Upper Branding Section (~35-40% height)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .padding(top = 40.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NetrazeLogo(
                    size = 72.dp,
                    containerColor = PrimaryBlue,
                    iconColor = Color.White
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = "Netraze",
                    style = NetrazeTypography.displaySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "Indoor Wi-Fi Survey & Analysis",
                    style = NetrazeTypography.titleMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Lower Curved Blue Surface Container (Full width, extends to bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .clip(TopSheetShape)
                    .background(FormSurfaceBlue)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xl, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back!",
                        style = NetrazeTypography.headlineMedium,
                        color = TextOnBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = "Sign in to your account to continue",
                        style = NetrazeTypography.bodyMedium,
                        color = TextOnBlueSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

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

                    Spacer(modifier = Modifier.height(Spacing.md))

                    TextButton(onClick = { showForgotPasswordDialog = true }) {
                        Text(
                            text = "Forgot Password?",
                            style = NetrazeTypography.bodyMedium,
                            color = TextOnBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = { Text("Forgot Password", fontWeight = FontWeight.Bold) },
                text = { Text("Contact your Netraze Administrator to reset your password.") },
                confirmButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

// ==========================================
// PREVIEWS FOR VARIOUS UI STATES
// ==========================================

@Preview(showBackground = true, name = "Default State", heightDp = 800)
@Composable
fun LoginScreenDefaultPreview() {
    NetrazeTheme {
        LoginScreen(
            uiState = LoginUiState(),
            onIdentityChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true, name = "Populated State", heightDp = 800)
@Composable
fun LoginScreenPopulatedPreview() {
    NetrazeTheme {
        LoginScreen(
            uiState = LoginUiState(
                identity = "technician@netraze.app",
                password = "Password123",
                isPasswordVisible = false
            ),
            onIdentityChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State", heightDp = 800)
@Composable
fun LoginScreenLoadingPreview() {
    NetrazeTheme {
        LoginScreen(
            uiState = LoginUiState(
                identity = "technician@netraze.app",
                password = "Password123",
                isLoading = true
            ),
            onIdentityChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true, name = "Invalid Credentials Error State", heightDp = 800)
@Composable
fun LoginScreenErrorPreview() {
    NetrazeTheme {
        LoginScreen(
            uiState = LoginUiState(
                identity = "technician@netraze.app",
                password = "WrongPassword",
                errorMessage = "Invalid email address or password. Please try again."
            ),
            onIdentityChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onSubmit = {}
        )
    }
}
