package com.netraze.app.ui.survey

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netraze.app.data.local.entity.ProjectEntity
import com.netraze.app.data.local.entity.SurveyAreaEntity
import com.netraze.app.data.local.entity.SurveyEntity
import com.netraze.app.ui.components.AppTextField
import com.netraze.app.ui.components.PrimaryButton
import com.netraze.app.ui.theme.FormSurfaceBlue
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryBlue
import com.netraze.app.ui.theme.Spacing
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextOnBlue
import com.netraze.app.ui.theme.TextSecondary

@Composable
private fun HeaderBar(title: String, subtitle: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(text = title, style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = NetrazeTypography.bodySmall, color = TextSecondary)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
    )
}

@Composable
fun SurveysScreen(
    viewModel: SurveyViewModel,
    surveyArea: SurveyAreaEntity,
    project: ProjectEntity,
    userRole: String,
    currentUserId: String,
    onSurveyClick: (SurveyEntity) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(surveyArea.id) {
        viewModel.loadSurveys(surveyArea.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg)
    ) {
        HeaderBar(
            title = surveyArea.name,
            subtitle = "${project.name} › ${surveyArea.name} › Surveys",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        PrimaryButton(
            text = "+ Start New Survey",
            onClick = { showCreateDialog = true }
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (uiState.error != null) {
            Text(
                text = uiState.error ?: "",
                color = androidx.compose.ui.graphics.Color.Red,
                style = NetrazeTypography.bodyMedium
            )
        } else if (uiState.surveys.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No surveys recorded for this area yet.\nTap '+ Start New Survey' to begin.",
                    style = NetrazeTypography.bodyLarge,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                items(uiState.surveys) { survey ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSurveyClick(survey) },
                        colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text(
                                text = if (survey.title.isNotBlank()) survey.title else "Untitled Survey",
                                style = NetrazeTypography.titleMedium,
                                color = TextOnBlue,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = "Mode: ${survey.mode.replace("_", " ").uppercase()} | Status: ${survey.status.uppercase()}",
                                style = NetrazeTypography.bodySmall,
                                color = TextOnBlue
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = "Sync State: ${survey.syncState.uppercase()}",
                                style = NetrazeTypography.labelSmall,
                                color = TextOnBlue
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateSurveyDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, mode ->
                showCreateDialog = false
                viewModel.createSurvey(
                    surveyAreaId = surveyArea.id,
                    title = title,
                    mode = mode,
                    onSuccess = { newSurvey -> onSurveyClick(newSurvey) }
                )
            }
        )
    }
}

@Composable
fun CreateSurveyDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, mode: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("location_survey") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Start New Survey", style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                AppTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Survey Title",
                    placeholder = "e.g. Q3 Wi-Fi Audit"
                )

                Spacer(modifier = Modifier.height(Spacing.md))
                Text(text = "Select Survey Mode", style = NetrazeTypography.labelLarge, color = TextSecondary)
                Spacer(modifier = Modifier.height(Spacing.sm))

                SurveyModeCard(
                    title = "Floor Plan Mode",
                    subtitle = "Requires an existing floor-plan artifact for this survey area.",
                    icon = Icons.Rounded.Map,
                    isSelected = selectedMode == "floor_plan",
                    onClick = { selectedMode = "floor_plan" }
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                SurveyModeCard(
                    title = "Simple Map Mode",
                    subtitle = "Requires an existing Simple Map artifact for this survey area.",
                    icon = Icons.Rounded.Navigation,
                    isSelected = selectedMode == "simple_map",
                    onClick = { selectedMode = "simple_map" }
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                SurveyModeCard(
                    title = "Location Survey Mode",
                    subtitle = "Ready for the emulator working model; no map artifact is required.",
                    icon = Icons.Rounded.Place,
                    isSelected = selectedMode == "location_survey",
                    onClick = { selectedMode = "location_survey" }
                )

                if (selectedMode != "location_survey") {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = "This mode cannot be started from the emulator model until its required map artifact is selected. Choose Location Survey Mode for the current working flow.",
                        style = NetrazeTypography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title.trim(), selectedMode) },
                enabled = selectedMode == "location_survey"
            ) {
                Text("Start Survey", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun SurveyModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PrimaryBlue else TextSecondary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Spacer(modifier = Modifier.width(Spacing.xs))
            Icon(imageVector = icon, contentDescription = title, tint = PrimaryBlue)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Column {
                Text(text = title, style = NetrazeTypography.labelLarge, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = NetrazeTypography.bodySmall, color = TextSecondary)
            }
        }
    }
}
