package com.netraze.app.ui.hierarchy

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netraze.app.data.local.entity.BuildingEntity
import com.netraze.app.data.local.entity.FloorEntity
import com.netraze.app.data.local.entity.ProjectEntity
import com.netraze.app.data.local.entity.SurveyAreaEntity
import com.netraze.app.ui.components.InfoCard
import com.netraze.app.ui.components.OfflineBanner
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryDark
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextPrimary
import com.netraze.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HierarchyScreenLayout(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    fab: @Composable () -> Unit,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title,
                            style = NetrazeTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = subtitle,
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
        floatingActionButton = fab,
        containerColor = SurfaceLight
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}

@Composable
private fun HierarchyCardContent(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = TextPrimary)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = NetrazeTypography.titleMedium, color = TextPrimary)
            Text(text = subtitle, style = NetrazeTypography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun EmptyStateText(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = NetrazeTypography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(24.dp)
        )
    }
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
        containerColor = SurfaceLight,
        title = { Text(text = title, style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    focusedLabelColor = PrimaryDark,
                    unfocusedTextColor = TextPrimary,
                    focusedTextColor = TextPrimary
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) }
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

    val isAdministrator = (userRole.lowercase() == "administrator")
    val isOffline = !error.isNullOrBlank()

    LaunchedEffect(Unit) {
        viewModel.loadProjects()
    }

    HierarchyScreenLayout(
        title = "Projects",
        subtitle = "Netraze Projects Hierarchy",
        onBackClick = onBackClick,
        fab = {
            if (isAdministrator) {
                FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = PrimaryDark, contentColor = Color.White) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Create Project")
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (isOffline) {
                OfflineBanner(isOffline = isOffline, hasCachedData = projects.isNotEmpty())
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (projects.isEmpty() && !isLoading) {
                EmptyStateText(message = if (isAdministrator) "No projects found. Tap '+' to create." else "No projects available.")
            } else if (projects.isNotEmpty()) {
                InfoCard(isHighEmphasis = false, modifier = Modifier.fillMaxWidth()) {
                    LazyColumn {
                        itemsIndexed(projects) { index, project ->
                            val isOwner = (project.ownerId.toString() == currentUserId)
                            HierarchyCardContent(title = project.name, subtitle = if (isOwner) "Owner Access" else "Member Access", icon = Icons.Rounded.Folder) {
                                onProjectClick(project)
                            }
                            if (index < projects.lastIndex) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondary.copy(alpha = 0.2f)))
                        }
                    }
                }
            }
        }
        if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryDark)
        if (showCreateDialog) {
            CreateEntityDialog(title = "Create New Project", label = "Project Name", onDismiss = { showCreateDialog = false }, onConfirm = { name -> viewModel.createProject(name) { showCreateDialog = false } })
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
    val isAdministrator = (userRole.lowercase() == "administrator")
    val isOffline = !error.isNullOrBlank()

    LaunchedEffect(project.id) { viewModel.loadBuildings(project.id) }

    HierarchyScreenLayout(
        title = project.name,
        subtitle = "Project › Buildings",
        onBackClick = onBackClick,
        fab = {
            if (isAdministrator) {
                FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = PrimaryDark, contentColor = Color.White) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Create Building")
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (isOffline) {
                OfflineBanner(isOffline = isOffline, hasCachedData = buildings.isNotEmpty())
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (buildings.isEmpty() && !isLoading) {
                EmptyStateText(message = if (isAdministrator) "No buildings found. Tap '+' to create." else "No buildings available.")
            } else if (buildings.isNotEmpty()) {
                InfoCard(isHighEmphasis = false, modifier = Modifier.fillMaxWidth()) {
                    LazyColumn {
                        itemsIndexed(buildings) { index, building ->
                            HierarchyCardContent(title = building.name, subtitle = "Building", icon = Icons.Rounded.Apartment) {
                                onBuildingClick(building)
                            }
                            if (index < buildings.lastIndex) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondary.copy(alpha = 0.2f)))
                        }
                    }
                }
            }
        }
        if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryDark)
        if (showCreateDialog) {
            CreateEntityDialog(title = "Add Building", label = "Building Name", onDismiss = { showCreateDialog = false }, onConfirm = { name -> viewModel.createBuilding(project.id, name) { showCreateDialog = false } })
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
    val isAdministrator = (userRole.lowercase() == "administrator")
    val isOffline = !error.isNullOrBlank()

    LaunchedEffect(building.id) { viewModel.loadFloors(building.id) }

    HierarchyScreenLayout(
        title = building.name,
        subtitle = "${project.name} › Floors",
        onBackClick = onBackClick,
        fab = {
            if (isAdministrator) {
                FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = PrimaryDark, contentColor = Color.White) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Create Floor")
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (isOffline) {
                OfflineBanner(isOffline = isOffline, hasCachedData = floors.isNotEmpty())
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (floors.isEmpty() && !isLoading) {
                EmptyStateText(message = if (isAdministrator) "No floors found. Tap '+' to create." else "No floors available.")
            } else if (floors.isNotEmpty()) {
                InfoCard(isHighEmphasis = false, modifier = Modifier.fillMaxWidth()) {
                    LazyColumn {
                        itemsIndexed(floors) { index, floor ->
                            HierarchyCardContent(title = floor.name, subtitle = "Floor", icon = Icons.Rounded.Layers) {
                                onFloorClick(floor)
                            }
                            if (index < floors.lastIndex) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondary.copy(alpha = 0.2f)))
                        }
                    }
                }
            }
        }
        if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryDark)
        if (showCreateDialog) {
            CreateEntityDialog(title = "Add Floor", label = "Floor Name", onDismiss = { showCreateDialog = false }, onConfirm = { name -> viewModel.createFloor(building.id, name) { showCreateDialog = false } })
        }
    }
}

@Composable
fun FloorDetailScreen(
    viewModel: HierarchyViewModel,
    floor: FloorEntity,
    building: BuildingEntity,
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
    val isAdministrator = (userRole.lowercase() == "administrator")
    val isOffline = !error.isNullOrBlank()

    LaunchedEffect(floor.id) { viewModel.loadSurveyAreas(floor.id) }

    HierarchyScreenLayout(
        title = floor.name,
        subtitle = "${project.name} › ${building.name} › Survey Areas",
        onBackClick = onBackClick,
        fab = {
            if (isAdministrator) {
                FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = PrimaryDark, contentColor = Color.White) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Create Survey Area")
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (isOffline) {
                OfflineBanner(isOffline = isOffline, hasCachedData = surveyAreas.isNotEmpty())
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (surveyAreas.isEmpty() && !isLoading) {
                EmptyStateText(message = if (isAdministrator) "No survey areas found. Tap '+' to create." else "No survey areas available.")
            } else if (surveyAreas.isNotEmpty()) {
                InfoCard(isHighEmphasis = false, modifier = Modifier.fillMaxWidth()) {
                    LazyColumn {
                        itemsIndexed(surveyAreas) { index, area ->
                            HierarchyCardContent(title = area.name, subtitle = "Survey Area", icon = Icons.Rounded.LocationOn) {
                                onSurveyAreaClick(area)
                            }
                            if (index < surveyAreas.lastIndex) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondary.copy(alpha = 0.2f)))
                        }
                    }
                }
            }
        }
        if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryDark)
        if (showCreateDialog) {
            CreateEntityDialog(title = "Add Survey Area", label = "Area Name", onDismiss = { showCreateDialog = false }, onConfirm = { name -> viewModel.createSurveyArea(floor.id, name) { showCreateDialog = false } })
        }
    }
}
