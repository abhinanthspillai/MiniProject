package com.netraze.app.ui.dashboard

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
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Wifi
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
fun DashboardHomeScreen(
    email: String,
    role: String,
    onBrowseProjects: () -> Unit,
    onStartSurveyShortcut: () -> Unit
) {
    val displayRole = when (role.lowercase()) {
        "administrator" -> "Administrator"
        "survey_technician" -> "Survey Technician"
        else -> role
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Netraze", style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        Text(text = "Indoor Wi-Fi Survey & Analysis", style = NetrazeTypography.bodySmall, color = TextSecondary)
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
                .padding(Spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Welcome & Role Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text(
                        text = "Welcome back,",
                        style = NetrazeTypography.bodyMedium,
                        color = TextOnBlue
                    )
                    Text(
                        text = email,
                        style = NetrazeTypography.titleLarge,
                        color = TextOnBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Role",
                            tint = TextOnBlue,
                            modifier = Modifier.height(16.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = "Role: $displayRole",
                            style = NetrazeTypography.labelMedium,
                            color = TextOnBlue
                        )
                    }
                }
            }

            // Quick Actions Section
            Text(
                text = "Quick Actions",
                style = NetrazeTypography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.AccountTree, contentDescription = "Hierarchy", tint = TextOnBlue)
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Projects & Hierarchy", style = NetrazeTypography.titleMedium, color = TextOnBlue, fontWeight = FontWeight.Bold)
                            Text(text = "Drill down into Projects, Buildings, Floors & Survey Areas", style = NetrazeTypography.bodySmall, color = TextOnBlue)
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.md))
                    PrimaryButton(
                        text = "Browse Hierarchy",
                        onClick = onBrowseProjects,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.AddCircleOutline, contentDescription = "Start Survey", tint = TextOnBlue)
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Start New Survey", style = NetrazeTypography.titleMedium, color = TextOnBlue, fontWeight = FontWeight.Bold)
                            Text(text = "Select a Survey Area context to launch survey mode", style = NetrazeTypography.bodySmall, color = TextOnBlue)
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.md))
                    PrimaryButton(
                        text = "Select Survey Area & Begin",
                        onClick = onStartSurveyShortcut,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // System Capability Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.Wifi, contentDescription = "Wi-Fi Engine", tint = TextOnBlue)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "Wi-Fi Survey System Status",
                            style = NetrazeTypography.titleSmall,
                            color = TextOnBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "• Production Wi-Fi Hardware Scanning Engine Ready\n• Offline Room Persistence Active\n• Mode Support: Floor Plan, Simple Map, Location Survey",
                        style = NetrazeTypography.bodySmall,
                        color = TextOnBlue
                    )
                }
            }
        }
    }
}
