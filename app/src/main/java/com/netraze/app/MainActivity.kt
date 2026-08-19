package com.netraze.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netraze.app.data.remote.api.AuthApi
import com.netraze.app.data.repository.AuthRepository
import com.netraze.app.data.security.SecureSessionStore
import com.netraze.app.ui.auth.LoginRoute
import com.netraze.app.ui.auth.LoginViewModel
import com.netraze.app.ui.components.PrimaryButton
import com.netraze.app.ui.theme.FormSurfaceBlue
import com.netraze.app.ui.theme.NetrazeTheme
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryBlue
import com.netraze.app.ui.theme.Spacing
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextOnBlue
import com.netraze.app.ui.theme.TextSecondary
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var authApi: AuthApi

    @Inject
    lateinit var secureSessionStore: SecureSessionStore

    private val loginViewModel: LoginViewModel by lazy {
        LoginViewModel(authRepository, authApi).apply {
            setDependencies(authRepository, authApi)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NetrazeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceLight
                ) {
                    val authState by loginViewModel.authState.collectAsStateWithLifecycle()

                    if (authState.isAuthenticated) {
                        AuthenticatedDashboardScreen(
                            email = authState.userProfile?.email ?: authState.session?.email ?: "Unknown",
                            role = authState.userProfile?.role ?: authState.session?.role ?: "Unknown",
                            userId = (authState.userProfile?.id ?: authState.session?.userId)?.toString() ?: "Unknown",
                            isKeystoreProtected = secureSessionStore.isKeystoreProtected(),
                            onSignOut = { loginViewModel.logout() }
                        )
                    } else {
                        LoginRoute(
                            viewModel = loginViewModel,
                            onLoginSubmitted = { _, _ -> }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuthenticatedDashboardScreen(
    email: String,
    role: String,
    userId: String,
    isKeystoreProtected: Boolean,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Netraze Survey",
            style = NetrazeTypography.headlineMedium,
            color = PrimaryBlue,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = "Authenticated Session Active",
            style = NetrazeTypography.titleMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
        ) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text(text = "Authenticated User", style = NetrazeTypography.labelMedium, color = TextOnBlue)
                Text(text = email, style = NetrazeTypography.titleLarge, color = TextOnBlue, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(text = "Global Role", style = NetrazeTypography.labelMedium, color = TextOnBlue)
                Text(text = role, style = NetrazeTypography.bodyLarge, color = TextOnBlue)

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(text = "Canonical User UUID", style = NetrazeTypography.labelMedium, color = TextOnBlue)
                Text(text = userId, style = NetrazeTypography.bodySmall, color = TextOnBlue)

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(text = "Session Encryption", style = NetrazeTypography.labelMedium, color = TextOnBlue)
                Text(
                    text = if (isKeystoreProtected) "Keystore-protected (AndroidKeyStore)" else "Test Crypto",
                    style = NetrazeTypography.bodyMedium,
                    color = TextOnBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xxl))

        PrimaryButton(
            text = "Sign Out",
            onClick = onSignOut
        )
    }
}
