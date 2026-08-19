package com.netraze.app.ui.hierarchy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.netraze.app.ui.theme.FormSurfaceBlue
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryBlue
import com.netraze.app.ui.theme.Spacing
import com.netraze.app.ui.theme.TextOnBlue
import com.netraze.app.ui.theme.TextSecondary

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

    LaunchedEffect(Unit) {
        viewModel.loadProjects()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Select Survey Location",
                    style = NetrazeTypography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                val breadcrumb = when {
                    selectedFloor != null -> "${selectedProject?.name} › ${selectedBuilding?.name} › ${selectedFloor?.name}"
                    selectedBuilding != null -> "${selectedProject?.name} › ${selectedBuilding?.name}"
                    selectedProject != null -> "${selectedProject?.name}"
                    else -> "Select Project"
                }
                Text(
                    text = breadcrumb,
                    style = NetrazeTypography.bodySmall,
                    color = PrimaryBlue
                )
            }
        },
        text = {
            Column(modifier = Modifier.height(280.dp)) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else if (selectedProject == null) {
                    Text(text = "Step 1: Select Project", style = NetrazeTypography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    LazyColumn {
                        items(projects) { project ->
                            LocationItemCard(title = project.name, onClick = {
                                selectedProject = project
                                viewModel.loadBuildings(project.id)
                            })
                        }
                    }
                } else if (selectedBuilding == null) {
                    Text(text = "Step 2: Select Building", style = NetrazeTypography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    LazyColumn {
                        items(buildings) { building ->
                            LocationItemCard(title = building.name, onClick = {
                                selectedBuilding = building
                                viewModel.loadFloors(building.id)
                            })
                        }
                    }
                } else if (selectedFloor == null) {
                    Text(text = "Step 3: Select Floor", style = NetrazeTypography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    LazyColumn {
                        items(floors) { floor ->
                            LocationItemCard(title = floor.name, onClick = {
                                selectedFloor = floor
                                viewModel.loadSurveyAreas(floor.id)
                            })
                        }
                    }
                } else {
                    Text(text = "Step 4: Select Survey Area", style = NetrazeTypography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    LazyColumn {
                        items(surveyAreas) { area ->
                            LocationItemCard(title = area.name, onClick = {
                                onLocationSelected(area, selectedFloor!!, selectedBuilding!!, selectedProject!!)
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedProject != null) {
                TextButton(
                    onClick = {
                        selectedFloor = null
                        selectedBuilding = null
                        selectedProject = null
                        viewModel.loadProjects()
                    }
                ) {
                    Text("Reset Selection", color = PrimaryBlue)
                }
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
private fun LocationItemCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
    ) {
        Text(
            text = title,
            style = NetrazeTypography.bodyMedium,
            color = TextOnBlue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(Spacing.md)
        )
    }
}
