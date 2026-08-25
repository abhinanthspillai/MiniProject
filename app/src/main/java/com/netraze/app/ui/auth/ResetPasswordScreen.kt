package com.netraze.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netraze.app.ui.components.AppTextField
import com.netraze.app.ui.components.InfoCard
import com.netraze.app.ui.components.PasswordField
import com.netraze.app.ui.components.PrimaryButton
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryDark
import com.netraze.app.ui.theme.SuccessChipText
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextPrimary
import com.netraze.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    viewModel: LoginViewModel,
    onBackToLogin: () -> Unit
) {
    val state by viewModel.resetPasswordState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reset Password",
                        style = NetrazeTypography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to Login",
                            tint = PrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = SurfaceLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Error Banner
            if (!state.error.isNullOrBlank()) {
                InfoCard(isHighEmphasis = false) {
                    Text(
                        text = state.error ?: "",
                        style = NetrazeTypography.bodySmall,
                        color = Color(0xFF991B1B)
                    )
                }
            }

            // Step 1: Administrator Verification
            InfoCard(isHighEmphasis = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Admin Lock",
                        tint = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Step 1 — Admin Verification",
                        style = NetrazeTypography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Administrator credentials are required to authorize password resets.",
                    style = NetrazeTypography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                AppTextField(
                    value = state.adminEmail,
                    onValueChange = { viewModel.updateResetPasswordAdminForm(adminEmail = it) },
                    label = "Administrator Email",
                    placeholder = "admin@netraze.app",
                    enabled = !state.isAdminVerified
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordField(
                    value = state.adminPassword,
                    onValueChange = { viewModel.updateResetPasswordAdminForm(adminPassword = it) },
                    label = "Administrator Password",
                    enabled = !state.isAdminVerified
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (state.isAdminVerified) {
                    InfoCard(isHighEmphasis = false) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Verified",
                                tint = SuccessChipText
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Administrator Verified",
                                style = NetrazeTypography.labelLarge,
                                color = SuccessChipText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    PrimaryButton(
                        text = if (state.isVerifyingAdmin) "Verifying..." else "Verify Administrator",
                        onClick = { viewModel.verifyAdminForResetPassword() },
                        enabled = !state.isVerifyingAdmin && state.adminEmail.isNotBlank() && state.adminPassword.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Step 2: User Account & New Password (Revealed after Admin verification)
            if (state.isAdminVerified) {
                InfoCard(isHighEmphasis = true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.VpnKey,
                            contentDescription = "Reset Key",
                            tint = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Step 2 — User Account & New Password",
                            style = NetrazeTypography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AppTextField(
                        value = state.targetUserEmail,
                        onValueChange = { viewModel.updateResetPasswordForm(targetUserEmail = it) },
                        label = "User Email Address",
                        placeholder = "user@netraze.app"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PasswordField(
                        value = state.newPassword,
                        onValueChange = { viewModel.updateResetPasswordForm(newPassword = it) },
                        label = "New Password"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PasswordField(
                        value = state.confirmNewPassword,
                        onValueChange = { viewModel.updateResetPasswordForm(confirmNewPassword = it) },
                        label = "Confirm New Password"
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    PrimaryButton(
                        text = if (state.isResettingPassword) "Resetting Password..." else "Reset Password",
                        onClick = { viewModel.submitResetPassword() },
                        enabled = !state.isResettingPassword && state.targetUserEmail.isNotBlank() && state.newPassword.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cancel / Back to Login", color = TextSecondary)
            }
        }
    }

    state.successMessage?.let { successMsg ->
        AlertDialog(
            onDismissRequest = { viewModel.resetResetPasswordForm() },
            title = { Text("Password Reset Complete", style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text(successMsg, style = NetrazeTypography.bodyMedium) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetResetPasswordForm()
                        onBackToLogin()
                    }
                ) {
                    Text("Back to Login", color = PrimaryDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
