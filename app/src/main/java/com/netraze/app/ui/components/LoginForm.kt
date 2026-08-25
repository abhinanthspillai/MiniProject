package com.netraze.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun LoginForm(
    identity: String,
    onIdentityChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean, // Kept for compatibility but unused
    onTogglePasswordVisibility: () -> Unit, // Kept for compatibility but unused
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
            label = "Email Address",
            placeholder = "Enter your email address",
            leadingIcon = Icons.Rounded.Email,
            enabled = !isLoading,
            isError = !errorMessage.isNullOrBlank(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            enabled = !isLoading,
            isError = !errorMessage.isNullOrBlank(),
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isLoginEnabled && !isLoading) {
                        onSubmit()
                    }
                }
            )
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            ErrorMessage(message = errorMessage)
        }

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "Sign In",
            onClick = onSubmit,
            enabled = isLoginEnabled,
            isLoading = isLoading
        )
    }
}
