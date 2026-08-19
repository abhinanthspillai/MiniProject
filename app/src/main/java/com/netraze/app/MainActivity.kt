package com.netraze.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.netraze.app.data.remote.api.AuthApi
import com.netraze.app.data.repository.AuthRepository
import com.netraze.app.data.repository.HierarchyRepository
import com.netraze.app.data.repository.SurveyRepository
import com.netraze.app.data.security.SecureSessionStore
import com.netraze.app.ui.auth.LoginRoute
import com.netraze.app.ui.auth.LoginViewModel
import com.netraze.app.ui.components.PrimaryButton
import com.netraze.app.ui.hierarchy.BuildingDetailScreen
import com.netraze.app.ui.hierarchy.FloorDetailScreen
import com.netraze.app.ui.hierarchy.HierarchyViewModel
import com.netraze.app.ui.hierarchy.ProjectDetailScreen
import com.netraze.app.ui.hierarchy.ProjectsScreen
import com.netraze.app.ui.survey.SurveyViewModel
import com.netraze.app.ui.survey.SurveysScreen
import com.netraze.app.ui.theme.FormSurfaceBlue
import com.netraze.app.ui.theme.NetrazeTheme
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryBlue
import com.netraze.app.ui.theme.Spacing
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextOnBlue
import com.netraze.app.ui.theme.TextSecondary
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class ScreenState {
    object Dashboard : ScreenState()
    object Projects : ScreenState()
    data class ProjectDetail(val project: ProjectEntity) : ScreenState()
    data class BuildingDetail(val building: BuildingEntity, val project: ProjectEntity) : ScreenState()
    data class FloorDetail(val floor: FloorEntity, val building: BuildingEntity, val project: ProjectEntity) : ScreenState()
    data class Surveys(val surveyArea: SurveyAreaEntity, val floor: FloorEntity, val building: BuildingEntity, val project: ProjectEntity) : ScreenState()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var authApi: AuthApi

    @Inject
    lateinit var secureSessionStore: SecureSessionStore

    @Inject
    lateinit var hierarchyRepository: HierarchyRepository

    @Inject
    lateinit var surveyRepository: SurveyRepository

    private val loginViewModel: LoginViewModel by lazy {
        LoginViewModel(authRepository, authApi).apply {
            setDependencies(authRepository, authApi)
        }
    }

    private val hierarchyViewModel: HierarchyViewModel by lazy {
        HierarchyViewModel(hierarchyRepository).apply {
            setRepository(hierarchyRepository)
        }
    }

    private val surveyViewModel: SurveyViewModel by lazy {
        SurveyViewModel(surveyRepository).apply {
            setRepository(surveyRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NetrazeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceLight
                ) {
                    val authState by loginViewModel.authState.collectAsStateWithLifecycle()
                    var currentScreen by remember { mutableStateOf<ScreenState>(ScreenState.Dashboard) }

                    if (authState.isAuthenticated) {
                        val email = authState.userProfile?.email ?: authState.session?.email ?: "Unknown"
                        val role = authState.userProfile?.role ?: authState.session?.role ?: "Unknown"
                        val userId = (authState.userProfile?.id ?: authState.session?.userId)?.toString() ?: "Unknown"

                        when (val screen = currentScreen) {
                            is ScreenState.Dashboard -> {
                                AuthenticatedDashboardScreen(
                                    email = email,
                                    role = role,
                                    userId = userId,
                                    isKeystoreProtected = secureSessionStore.isKeystoreProtected(),
                                    onBrowseHierarchy = { currentScreen = ScreenState.Projects },
                                    onSignOut = {
                                        currentScreen = ScreenState.Dashboard
                                        loginViewModel.logout()
                                    }
                                )
                            }
                            is ScreenState.Projects -> {
                                ProjectsScreen(
                                    viewModel = hierarchyViewModel,
                                    userRole = role,
                                    currentUserId = userId,
                                    onProjectClick = { project ->
                                        currentScreen = ScreenState.ProjectDetail(project)
                                    },
                                    onBackClick = { currentScreen = ScreenState.Dashboard }
                                )
                            }
                            is ScreenState.ProjectDetail -> {
                                ProjectDetailScreen(
                                    viewModel = hierarchyViewModel,
                                    project = screen.project,
                                    userRole = role,
                                    currentUserId = userId,
                                    onBuildingClick = { building ->
                                        currentScreen = ScreenState.BuildingDetail(building, screen.project)
                                    },
                                    onBackClick = { currentScreen = ScreenState.Projects }
                                )
                            }
                            is ScreenState.BuildingDetail -> {
                                BuildingDetailScreen(
                                    viewModel = hierarchyViewModel,
                                    building = screen.building,
                                    project = screen.project,
                                    userRole = role,
                                    currentUserId = userId,
                                    onFloorClick = { floor ->
                                        currentScreen = ScreenState.FloorDetail(floor, screen.building, screen.project)
                                    },
                                    onBackClick = { currentScreen = ScreenState.ProjectDetail(screen.project) }
                                )
                            }
                            is ScreenState.FloorDetail -> {
                                FloorDetailScreen(
                                    viewModel = hierarchyViewModel,
                                    floor = screen.floor,
                                    project = screen.project,
                                    userRole = role,
                                    currentUserId = userId,
                                    onSurveyAreaClick = { area ->
                                        currentScreen = ScreenState.Surveys(area, screen.floor, screen.building, screen.project)
                                    },
                                    onBackClick = { currentScreen = ScreenState.BuildingDetail(screen.building, screen.project) }
                                )
                            }
                            is ScreenState.Surveys -> {
                                SurveysScreen(
                                    viewModel = surveyViewModel,
                                    surveyArea = screen.surveyArea,
                                    project = screen.project,
                                    userRole = role,
                                    currentUserId = userId,
                                    onSurveyClick = { _ -> },
                                    onBackClick = { currentScreen = ScreenState.FloorDetail(screen.floor, screen.building, screen.project) }
                                )
                            }
                        }
                    } else {
                        LoginRoute(
                            viewModel = loginViewModel,
                            onLoginSubmitted = { _, _ -> }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuthenticatedDashboardScreen(
    email: String,
    role: String,
    userId: String,
    isKeystoreProtected: Boolean,
    onBrowseHierarchy: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Netraze Survey",
            style = NetrazeTypography.headlineMedium,
            color = PrimaryBlue,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = "Authenticated Session Active",
            style = NetrazeTypography.titleMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
        ) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text(text = "Authenticated User", style = NetrazeTypography.labelMedium, color = TextOnBlue)
                Text(text = email, style = NetrazeTypography.titleLarge, color = TextOnBlue, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(text = "Global Role", style = NetrazeTypography.labelMedium, color = TextOnBlue)
                Text(text = role, style = NetrazeTypography.bodyLarge, color = TextOnBlue)

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(text = "Canonical User UUID", style = NetrazeTypography.labelMedium, color = TextOnBlue)
                Text(text = userId, style = NetrazeTypography.bodySmall, color = TextOnBlue)

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(text = "Session Encryption", style = NetrazeTypography.labelMedium, color = TextOnBlue)
                Text(
                    text = if (isKeystoreProtected) "Keystore-protected (AndroidKeyStore)" else "Test Crypto",
                    style = NetrazeTypography.bodyMedium,
                    color = TextOnBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        PrimaryButton(
            text = "Browse Hierarchy Context",
            onClick = onBrowseHierarchy
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        PrimaryButton(
            text = "Sign Out",
            onClick = onSignOut
        )
    }
}
