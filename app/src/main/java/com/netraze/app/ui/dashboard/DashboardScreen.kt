package com.netraze.app.ui.dashboard

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
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
import com.netraze.app.data.local.entity.SurveyEntity
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
    recentSurvey: SurveyEntity?,
    onStartSurveyClick: () -> Unit,
    onContinueSurveyClick: (SurveyEntity) -> Unit,
    onBrowseLocations: () -> Unit
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

            // Continue Active Survey (if present)
            recentSurvey?.let { survey ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onContinueSurveyClick(survey) },
                    colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "Continue", tint = TextOnBlue)
                            Spacer(modifier = Modifier.width(Spacing.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Active Survey", style = NetrazeTypography.labelSmall, color = TextOnBlue)
                                Text(
                                    text = if (survey.title.isNotBlank()) survey.title else "Untitled Survey",
                                    style = NetrazeTypography.titleMedium,
                                    color = TextOnBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Mode: ${survey.mode.replace("_", " ").uppercase()} | Status: ${survey.status.uppercase()}",
                                    style = NetrazeTypography.bodySmall,
                                    color = TextOnBlue
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(Spacing.md))
                        PrimaryButton(
                            text = "CONTINUE SURVEY",
                            onClick = { onContinueSurveyClick(survey) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Primary CTA: START SURVEY
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.AddCircleOutline, contentDescription = "Start Survey", tint = TextOnBlue)
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Launch Wi-Fi Survey", style = NetrazeTypography.titleMedium, color = TextOnBlue, fontWeight = FontWeight.Bold)
                            Text(text = "Select a survey location context and begin collecting Wi-Fi evidence", style = NetrazeTypography.bodySmall, color = TextOnBlue)
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.md))
                    PrimaryButton(
                        text = "START SURVEY",
                        onClick = onStartSurveyClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Locations Quick Entry Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.Wifi, contentDescription = "Locations", tint = TextOnBlue)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "Locations & Sites",
                            style = NetrazeTypography.titleSmall,
                            color = TextOnBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "Browse projects, buildings, floors, and survey areas.",
                        style = NetrazeTypography.bodySmall,
                        color = TextOnBlue
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    PrimaryButton(
                        text = "Browse Locations",
                        onClick = onBrowseLocations,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
