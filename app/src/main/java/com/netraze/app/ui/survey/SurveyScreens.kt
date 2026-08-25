package com.netraze.app.ui.survey

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netraze.app.data.local.entity.ProjectEntity
import com.netraze.app.data.local.entity.SurveyAreaEntity
import com.netraze.app.data.local.entity.SurveyEntity
import com.netraze.app.ui.components.AppTextField
import com.netraze.app.ui.components.InfoCard
import com.netraze.app.ui.components.PrimaryButton
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryDark
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextPrimary
import com.netraze.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = surveyArea.name,
                            style = NetrazeTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${project.name} › Surveys",
                            style = NetrazeTypography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
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
        ) {
            PrimaryButton(
                text = "START NEW SURVEY",
                onClick = { showCreateDialog = true },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Rounded.Add,
                containerColor = PrimaryDark,
                contentColor = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryDark)
                }
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = Color.Red,
                    style = NetrazeTypography.bodyMedium
                )
            } else if (uiState.surveys.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No surveys recorded for this area yet.",
                        style = NetrazeTypography.bodyLarge,
                        color = TextSecondary
                    )
                }
            } else {
                InfoCard(isHighEmphasis = false, modifier = Modifier.fillMaxWidth()) {
                    LazyColumn {
                        itemsIndexed(uiState.surveys) { index, survey ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSurveyClick(survey) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (survey.mode == "location_survey") Icons.Rounded.Business else Icons.Rounded.Science,
                                        contentDescription = "Icon",
                                        tint = TextPrimary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = survey.title.ifBlank { "Untitled Survey" },
                                        style = NetrazeTypography.titleMedium,
                                        color = TextPrimary
                                    )
                                    val formattedMode = survey.mode.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                    Text(
                                        text = formattedMode,
                                        style = NetrazeTypography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(survey.updatedAt))
                                    Text(
                                        text = dateStr,
                                        style = NetrazeTypography.bodyMedium,
                                        color = TextSecondary
                                    )
                                    val displayStatus = if (survey.status == "completed" && survey.syncState == "synced") "Synced" else survey.status.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                    Text(
                                        text = displayStatus,
                                        style = NetrazeTypography.labelSmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                            
                            if (index < uiState.surveys.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(TextSecondary.copy(alpha = 0.2f))
                                )
                            }
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
        containerColor = SurfaceLight,
        title = { Text(text = "Start New Survey", style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column {
                AppTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Survey Title",
                    placeholder = "Enter survey title",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Mode:", style = NetrazeTypography.labelMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                
                SurveyModeOption(
                    title = "Location Survey",
                    description = "Dense AP data collection for an area",
                    icon = Icons.Rounded.Place,
                    selected = selectedMode == "location_survey",
                    onClick = { selectedMode = "location_survey" }
                )
                SurveyModeOption(
                    title = "Floor Plan",
                    description = "Collect data pinned to coordinates",
                    icon = Icons.Rounded.Map,
                    selected = selectedMode == "floor_plan",
                    onClick = { selectedMode = "floor_plan" }
                )
                SurveyModeOption(
                    title = "Simple Map",
                    description = "Quick topology map without coordinates",
                    icon = Icons.Rounded.Navigation,
                    selected = selectedMode == "simple_map",
                    onClick = { selectedMode = "simple_map" }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), selectedMode) }
            ) {
                Text("Create", color = PrimaryDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun SurveyModeOption(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) PrimaryDark else TextSecondary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (selected) PrimaryDark.copy(alpha = 0.05f) else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryDark, unselectedColor = TextSecondary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = icon, contentDescription = title, tint = if (selected) PrimaryDark else TextSecondary)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = NetrazeTypography.labelLarge, color = if (selected) PrimaryDark else TextPrimary)
            Text(text = description, style = NetrazeTypography.bodySmall, color = TextSecondary)
        }
    }
}
