package com.netraze.app.ui.auth

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netraze.app.ui.components.LoginForm
import com.netraze.app.ui.components.NetrazeLogo
import com.netraze.app.ui.theme.CardShape
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
        onSubmit = { viewModel.submitLogin(onLoginSubmitted) },
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
    Surface(
        modifier = modifier.fillMaxSize(),
        color = SurfaceLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.md))

            // Upper Header & Branding Area
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

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Blue Rounded Form Surface Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.lg),
                shape = CardShape,
                colors = CardDefaults.cardColors(
                    containerColor = FormSurfaceBlue
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = Spacing.xl)
                ) {
                    Text(
                        text = "Welcome Back",
                        style = NetrazeTypography.headlineMedium,
                        color = TextOnBlue,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = "Sign in to your account to access survey system",
                        style = NetrazeTypography.bodyMedium,
                        color = TextOnBlueSecondary
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
                }
            }
        }
    }
}

// ==========================================
// PREVIEWS FOR VARIOUS UI STATES
// ==========================================

@Preview(showBackground = true, name = "Default Empty State")
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

@Preview(showBackground = true, name = "Populated State")
@Composable
fun LoginScreenPopulatedPreview() {
    NetrazeTheme {
        LoginScreen(
            uiState = LoginUiState(
                identity = "technician@wifisurvey.net",
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

@Preview(showBackground = true, name = "Loading State")
@Composable
fun LoginScreenLoadingPreview() {
    NetrazeTheme {
        LoginScreen(
            uiState = LoginUiState(
                identity = "technician@wifisurvey.net",
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

@Preview(showBackground = true, name = "Invalid Credentials Error State")
@Composable
fun LoginScreenErrorPreview() {
    NetrazeTheme {
        LoginScreen(
            uiState = LoginUiState(
                identity = "technician@wifisurvey.net",
                password = "WrongPassword",
                errorMessage = "Invalid email/username or password. Please try again."
            ),
            onIdentityChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true, name = "Network Unavailable Error State")
@Composable
fun LoginScreenNetworkErrorPreview() {
    NetrazeTheme {
        LoginScreen(
            uiState = LoginUiState(
                identity = "technician@wifisurvey.net",
                password = "Password123",
                errorMessage = "Network server unavailable. Please check your connection."
            ),
            onIdentityChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onSubmit = {}
        )
    }
}
