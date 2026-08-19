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
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netraze.app.data.local.entity.ProjectEntity
import com.netraze.app.ui.components.OfflineBanner
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
fun LocationsTabScreen(
    viewModel: HierarchyViewModel,
    userRole: String,
    currentUserId: String,
    onProjectClick: (ProjectEntity) -> Unit,
    onManageLocationsClick: () -> Unit
) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val isAdministrator = (userRole.lowercase() == "administrator")
    val isOffline = !error.isNullOrBlank()

    LaunchedEffect(Unit) {
        viewModel.loadProjects()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Locations", style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = "Survey Location Contexts", style = NetrazeTypography.bodySmall, color = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = SurfaceLight
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg)
            ) {
                OfflineBanner(isOffline = isOffline, hasCachedData = projects.isNotEmpty())

                if (isAdministrator) {
                    PrimaryButton(
                        text = "Manage Locations (Admin Hierarchy)",
                        onClick = onManageLocationsClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))
                }

                Text(
                    text = "Available Projects & Sites",
                    style = NetrazeTypography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                if (projects.isEmpty() && !isLoading) {
                    Text(
                        text = "No location contexts available.",
                        style = NetrazeTypography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = Spacing.xl)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        items(projects) { project ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onProjectClick(project) },
                                colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
                            ) {
                                Row(
                                    modifier = Modifier.padding(Spacing.lg),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Rounded.Folder, contentDescription = project.name, tint = TextOnBlue)
                                    Spacer(modifier = Modifier.width(Spacing.md))
                                    Column {
                                        Text(text = project.name, style = NetrazeTypography.titleMedium, color = TextOnBlue, fontWeight = FontWeight.Bold)
                                        Text(text = "Tap to view buildings & survey areas", style = NetrazeTypography.bodySmall, color = TextOnBlue)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
            }
        }
    }
}
