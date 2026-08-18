package com.netraze.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.netraze.app.ui.theme.ErrorRed
import com.netraze.app.ui.theme.InputBackgroundOnBlue
import com.netraze.app.ui.theme.InputBorderOnBlue
import com.netraze.app.ui.theme.InputFocusBorderOnBlue
import com.netraze.app.ui.theme.InputPlaceholderOnBlue
import com.netraze.app.ui.theme.InputShape
import com.netraze.app.ui.theme.InputTextOnBlue
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.TextOnBlue

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = NetrazeTypography.labelLarge,
            color = TextOnBlue,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            textStyle = NetrazeTypography.bodyLarge.copy(color = InputTextOnBlue),
            placeholder = {
                Text(
                    text = placeholder,
                    style = NetrazeTypography.bodyLarge,
                    color = InputPlaceholderOnBlue
                )
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (isError) ErrorRed else InputPlaceholderOnBlue
                    )
                }
            },
            trailingIcon = trailingIcon,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = InputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InputBackgroundOnBlue,
                unfocusedContainerColor = InputBackgroundOnBlue,
                disabledContainerColor = InputBackgroundOnBlue.copy(alpha = 0.15f),
                errorContainerColor = InputBackgroundOnBlue,
                focusedBorderColor = InputFocusBorderOnBlue,
                unfocusedBorderColor = InputBorderOnBlue,
                disabledBorderColor = InputBorderOnBlue.copy(alpha = 0.2f),
                errorBorderColor = ErrorRed,
                focusedLeadingIconColor = InputFocusBorderOnBlue,
                unfocusedLeadingIconColor = InputPlaceholderOnBlue
            )
        )
    }
}
