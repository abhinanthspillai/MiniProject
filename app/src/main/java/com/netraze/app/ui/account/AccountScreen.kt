package com.netraze.app.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.netraze.app.ui.components.PrimaryButton
import com.netraze.app.ui.theme.FormSurfaceBlue
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryBlue
import com.netraze.app.ui.theme.Spacing
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextOnBlue
import com.netraze.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    email: String,
    role: String,
    onSignOut: () -> Unit
) {
    val displayRole = when (role.lowercase()) {
        "administrator" -> "Administrator"
        "survey_technician" -> "Survey Technician"
        else -> role
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Profile", style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = SurfaceLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = "User Avatar",
                tint = PrimaryBlue,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = email,
                style = NetrazeTypography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = displayRole,
                style = NetrazeTypography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.Email, contentDescription = "Email", tint = TextOnBlue)
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column {
                            Text(text = "Email Address", style = NetrazeTypography.labelSmall, color = TextOnBlue)
                            Text(text = email, style = NetrazeTypography.bodyMedium, color = TextOnBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.Badge, contentDescription = "Role", tint = TextOnBlue)
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column {
                            Text(text = "Global System Role", style = NetrazeTypography.labelSmall, color = TextOnBlue)
                            Text(text = displayRole, style = NetrazeTypography.bodyMedium, color = TextOnBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Sign Out",
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}
