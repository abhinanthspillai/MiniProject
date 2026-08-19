package com.netraze.app.ui.hierarchy

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
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
private fun HierarchyCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = TextOnBlue)
            Spacer(modifier = Modifier.width(Spacing.md))
            Column {
                Text(text = title, style = NetrazeTypography.titleMedium, color = TextOnBlue, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = NetrazeTypography.bodySmall, color = TextOnBlue)
            }
        }
    }
}

@Composable
private fun EmptyStateText(message: String) {
    Text(
        text = message,
        style = NetrazeTypography.bodyMedium,
        color = TextSecondary,
        modifier = Modifier.padding(vertical = Spacing.xl)
    )
}

@Composable
private fun ErrorMessage(message: String) {
    Text(
        text = message,
        style = NetrazeTypography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(vertical = Spacing.sm)
    )
}

@Composable
private fun CreateEntityDialog(
    title: String,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    focusedLabelColor = PrimaryBlue
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) onConfirm(name.trim())
                }
            ) {
                Text("Create", color = PrimaryBlue, fontWeight = FontWeight.Bold)
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
fun ProjectsScreen(
    viewModel: HierarchyViewModel,
    userRole: String,
    currentUserId: String,
    onProjectClick: (ProjectEntity) -> Unit,
    onBackClick: () -> Unit
) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    val isAdministrator = (userRole == "administrator")

    LaunchedEffect(Unit) {
        viewModel.loadProjects()
    }

    Scaffold(
        topBar = {
            HeaderBar(title = "Projects", subtitle = "Netraze Hierarchy Root", onBackClick = onBackClick)
        },
        floatingActionButton = {
            if (isAdministrator) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = PrimaryBlue
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Create Project", tint = TextOnBlue)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(Spacing.lg)) {
                if (!error.isNullOrBlank()) {
                    ErrorMessage(message = error ?: "")
                    Spacer(modifier = Modifier.height(Spacing.md))
                }

                if (projects.isEmpty() && !isLoading) {
                    EmptyStateText(message = "No projects found. Tap '+' to create a project.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        items(projects) { project ->
                            val isOwner = (project.ownerId.toString() == currentUserId)
                            val subtitle = if (isOwner) "Owner (Full Admin Access)" else "Member Access"
                            HierarchyCard(
                                title = project.name,
                                subtitle = subtitle,
                                icon = Icons.Rounded.Folder,
                                onClick = { onProjectClick(project) }
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
            }

            if (showCreateDialog) {
                CreateEntityDialog(
                    title = "Create New Project",
                    label = "Project Name",
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { name ->
                        viewModel.createProject(name) { showCreateDialog = false }
                    }
                )
            }
        }
    }
}

@Composable
fun ProjectDetailScreen(
    viewModel: HierarchyViewModel,
    project: ProjectEntity,
    userRole: String,
    currentUserId: String,
    onBuildingClick: (BuildingEntity) -> Unit,
    onBackClick: () -> Unit
) {
    val buildings by viewModel.buildings.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    val isOwnerAdmin = (userRole == "administrator" && project.ownerId.toString() == currentUserId)

    LaunchedEffect(project.id) {
        viewModel.loadBuildings(project.id)
    }

    Scaffold(
        topBar = {
            HeaderBar(title = project.name, subtitle = "Buildings Hierarchy", onBackClick = onBackClick)
        },
        floatingActionButton = {
            if (isOwnerAdmin) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = PrimaryBlue
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Create Building", tint = TextOnBlue)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(Spacing.lg)) {
                if (!error.isNullOrBlank()) {
                    ErrorMessage(message = error ?: "")
                    Spacer(modifier = Modifier.height(Spacing.md))
                }

                if (buildings.isEmpty() && !isLoading) {
                    EmptyStateText(message = "No buildings found in this project.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        items(buildings) { building ->
                            HierarchyCard(
                                title = building.name,
                                subtitle = "Building Entity",
                                icon = Icons.Rounded.Apartment,
                                onClick = { onBuildingClick(building) }
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
            }

            if (showCreateDialog) {
                CreateEntityDialog(
                    title = "Create New Building",
                    label = "Building Name",
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { name ->
                        viewModel.createBuilding(project.id, name) { showCreateDialog = false }
                    }
                )
            }
        }
    }
}

@Composable
fun BuildingDetailScreen(
    viewModel: HierarchyViewModel,
    building: BuildingEntity,
    project: ProjectEntity,
    userRole: String,
    currentUserId: String,
    onFloorClick: (FloorEntity) -> Unit,
    onBackClick: () -> Unit
) {
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    val isOwnerAdmin = (userRole == "administrator" && project.ownerId.toString() == currentUserId)

    LaunchedEffect(building.id) {
        viewModel.loadFloors(building.id)
    }

    Scaffold(
        topBar = {
            HeaderBar(title = building.name, subtitle = "Floors Hierarchy", onBackClick = onBackClick)
        },
        floatingActionButton = {
            if (isOwnerAdmin) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = PrimaryBlue
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Create Floor", tint = TextOnBlue)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(Spacing.lg)) {
                if (!error.isNullOrBlank()) {
                    ErrorMessage(message = error ?: "")
                    Spacer(modifier = Modifier.height(Spacing.md))
                }

                if (floors.isEmpty() && !isLoading) {
                    EmptyStateText(message = "No floors found in this building.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        items(floors) { floor ->
                            HierarchyCard(
                                title = floor.name,
                                subtitle = "Floor Entity",
                                icon = Icons.Rounded.Layers,
                                onClick = { onFloorClick(floor) }
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
            }

            if (showCreateDialog) {
                CreateEntityDialog(
                    title = "Create New Floor",
                    label = "Floor Name (e.g. Ground Floor, Lab Floor)",
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { name ->
                        viewModel.createFloor(building.id, name) { showCreateDialog = false }
                    }
                )
            }
        }
    }
}

@Composable
fun FloorDetailScreen(
    viewModel: HierarchyViewModel,
    floor: FloorEntity,
    project: ProjectEntity,
    userRole: String,
    currentUserId: String,
    onSurveyAreaClick: (SurveyAreaEntity) -> Unit,
    onBackClick: () -> Unit
) {
    val surveyAreas by viewModel.surveyAreas.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    val isOwnerAdmin = (userRole == "administrator" && project.ownerId.toString() == currentUserId)

    LaunchedEffect(floor.id) {
        viewModel.loadSurveyAreas(floor.id)
    }

    Scaffold(
        topBar = {
            HeaderBar(title = floor.name, subtitle = "Survey Areas", onBackClick = onBackClick)
        },
        floatingActionButton = {
            if (isOwnerAdmin) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = PrimaryBlue
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Create Survey Area", tint = TextOnBlue)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(Spacing.lg)) {
                if (!error.isNullOrBlank()) {
                    ErrorMessage(message = error ?: "")
                    Spacer(modifier = Modifier.height(Spacing.md))
                }

                if (surveyAreas.isEmpty() && !isLoading) {
                    EmptyStateText(message = "No survey areas found on this floor.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        items(surveyAreas) { area ->
                            HierarchyCard(
                                title = area.name,
                                subtitle = "Survey Area Entity",
                                icon = Icons.Rounded.LocationOn,
                                onClick = { onSurveyAreaClick(area) }
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
            }

            if (showCreateDialog) {
                CreateEntityDialog(
                    title = "Create New Survey Area",
                    label = "Area Name (e.g. Lab 305, East Wing)",
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { name ->
                        viewModel.createSurveyArea(floor.id, name) { showCreateDialog = false }
                    }
                )
            }
        }
    }
}
