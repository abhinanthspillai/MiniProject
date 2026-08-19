package com.netraze.app.ui.survey

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
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
            title = "Surveys: ${surveyArea.name}",
            subtitle = "Survey Area Context",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        // + Start New Survey button is available for Survey Technicians and Administrators
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
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
                                text = "Mode: ${survey.mode.replace("_", " ").uppercase()} | Status: ${survey.status}",
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
                    onSuccess = { newSurvey ->
                        onSurveyClick(newSurvey)
                    }
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

                Text(text = "Survey Mode", style = NetrazeTypography.labelLarge, color = TextSecondary)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (selectedMode == "location_survey"),
                        onClick = { selectedMode = "location_survey" }
                    )
                    Text(text = "Location Survey Mode", style = NetrazeTypography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (selectedMode == "simple_map"),
                        onClick = { selectedMode = "simple_map" }
                    )
                    Text(text = "Simple Map Mode", style = NetrazeTypography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (selectedMode == "floor_plan"),
                        onClick = { selectedMode = "floor_plan" }
                    )
                    Text(text = "Floor Plan Mode", style = NetrazeTypography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title.trim(), selectedMode) }
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
