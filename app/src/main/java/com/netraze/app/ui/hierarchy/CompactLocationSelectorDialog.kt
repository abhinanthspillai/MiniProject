package com.netraze.app.ui.hierarchy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.netraze.app.data.local.entity.BuildingEntity
import com.netraze.app.data.local.entity.FloorEntity
import com.netraze.app.data.local.entity.ProjectEntity
import com.netraze.app.data.local.entity.SurveyAreaEntity
import com.netraze.app.ui.components.PrimaryButton
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryDark
import com.netraze.app.ui.theme.Spacing
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextPrimary
import com.netraze.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactLocationSelectorDialog(
    viewModel: HierarchyViewModel,
    onDismiss: () -> Unit,
    onLocationSelected: (SurveyAreaEntity, FloorEntity, BuildingEntity, ProjectEntity) -> Unit
) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val buildings by viewModel.buildings.collectAsStateWithLifecycle()
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val surveyAreas by viewModel.surveyAreas.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var selectedProject by remember { mutableStateOf<ProjectEntity?>(null) }
    var selectedBuilding by remember { mutableStateOf<BuildingEntity?>(null) }
    var selectedFloor by remember { mutableStateOf<FloorEntity?>(null) }
    var selectedSurveyArea by remember { mutableStateOf<SurveyAreaEntity?>(null) }

    var expandedProject by remember { mutableStateOf(false) }
    var expandedBuilding by remember { mutableStateOf(false) }
    var expandedFloor by remember { mutableStateOf(false) }
    var expandedSurveyArea by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProjects()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        title = {
            Text(
                text = "Select Survey Location",
                style = NetrazeTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                if (isLoading && projects.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryDark)
                    }
                } else {
                    // Project Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedProject,
                        onExpandedChange = { expandedProject = !expandedProject },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedProject?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Project") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProject) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedProject,
                            onDismissRequest = { expandedProject = false }
                        ) {
                            projects.forEach { project ->
                                DropdownMenuItem(
                                    text = { Text(project.name) },
                                    onClick = {
                                        selectedProject = project
                                        selectedBuilding = null
                                        selectedFloor = null
                                        selectedSurveyArea = null
                                        expandedProject = false
                                        viewModel.loadBuildings(project.id)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Building Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedBuilding,
                        onExpandedChange = { if (selectedProject != null) expandedBuilding = !expandedBuilding },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedBuilding?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Building") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBuilding) },
                            enabled = selectedProject != null,
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedBuilding,
                            onDismissRequest = { expandedBuilding = false }
                        ) {
                            buildings.forEach { building ->
                                DropdownMenuItem(
                                    text = { Text(building.name) },
                                    onClick = {
                                        selectedBuilding = building
                                        selectedFloor = null
                                        selectedSurveyArea = null
                                        expandedBuilding = false
                                        viewModel.loadFloors(building.id)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Floor Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedFloor,
                        onExpandedChange = { if (selectedBuilding != null) expandedFloor = !expandedFloor },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedFloor?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Floor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFloor) },
                            enabled = selectedBuilding != null,
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFloor,
                            onDismissRequest = { expandedFloor = false }
                        ) {
                            floors.forEach { floor ->
                                DropdownMenuItem(
                                    text = { Text(floor.name) },
                                    onClick = {
                                        selectedFloor = floor
                                        selectedSurveyArea = null
                                        expandedFloor = false
                                        viewModel.loadSurveyAreas(floor.id)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Survey Area Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedSurveyArea,
                        onExpandedChange = { if (selectedFloor != null) expandedSurveyArea = !expandedSurveyArea },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedSurveyArea?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Survey Area") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSurveyArea) },
                            enabled = selectedFloor != null,
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSurveyArea,
                            onDismissRequest = { expandedSurveyArea = false }
                        ) {
                            surveyAreas.forEach { area ->
                                DropdownMenuItem(
                                    text = { Text(area.name) },
                                    onClick = {
                                        selectedSurveyArea = area
                                        expandedSurveyArea = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedProject != null && selectedBuilding != null && selectedFloor != null && selectedSurveyArea != null) {
                        onLocationSelected(selectedSurveyArea!!, selectedFloor!!, selectedBuilding!!, selectedProject!!)
                    }
                },
                enabled = selectedProject != null && selectedBuilding != null && selectedFloor != null && selectedSurveyArea != null
            ) {
                Text("Confirm", color = PrimaryDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
