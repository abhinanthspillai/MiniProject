package com.netraze.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.netraze.app.ui.theme.Spacing

@Composable
fun LoginForm(
    identity: String,
    onIdentityChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    isLoginEnabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AppTextField(
            value = identity,
            onValueChange = onIdentityChange,
            label = "Email Address or Username",
            placeholder = "Enter your email or username",
            leadingIcon = Icons.Rounded.Email,
            enabled = !isLoading,
            isError = !errorMessage.isNullOrBlank(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        PasswordField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            placeholder = "Enter your password",
            isPasswordVisible = isPasswordVisible,
            onTogglePasswordVisibility = onTogglePasswordVisibility,
            enabled = !isLoading,
            isError = !errorMessage.isNullOrBlank(),
            imeAction = ImeAction.Done,
            onImeAction = {
                if (isLoginEnabled && !isLoading) {
                    onSubmit()
                }
            }
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(Spacing.md))
            ErrorMessage(message = errorMessage)
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        PrimaryButton(
            text = "Sign In",
            onClick = onSubmit,
            enabled = isLoginEnabled,
            isLoading = isLoading
        )
    }
}
