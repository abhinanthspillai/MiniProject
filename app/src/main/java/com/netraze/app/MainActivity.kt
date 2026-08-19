package com.netraze.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netraze.app.data.local.entity.BuildingEntity
import com.netraze.app.data.local.entity.FloorEntity
import com.netraze.app.data.local.entity.ProjectEntity
import com.netraze.app.data.local.entity.SurveyAreaEntity
import com.netraze.app.data.local.entity.SurveyEntity
import com.netraze.app.data.remote.api.AuthApi
import com.netraze.app.data.repository.AuthRepository
import com.netraze.app.data.repository.HierarchyRepository
import com.netraze.app.data.repository.SurveyRepository
import com.netraze.app.data.security.SecureSessionStore
import com.netraze.app.ui.account.AccountScreen
import com.netraze.app.ui.auth.LoginRoute
import com.netraze.app.ui.auth.LoginViewModel
import com.netraze.app.ui.canvas.SurveyCanvasScreen
import com.netraze.app.ui.canvas.SurveyCanvasViewModel
import com.netraze.app.ui.dashboard.DashboardHomeScreen
import com.netraze.app.ui.hierarchy.BuildingDetailScreen
import com.netraze.app.ui.hierarchy.FloorDetailScreen
import com.netraze.app.ui.hierarchy.HierarchyViewModel
import com.netraze.app.ui.hierarchy.ProjectDetailScreen
import com.netraze.app.ui.hierarchy.ProjectsScreen
import com.netraze.app.ui.survey.SurveyViewModel
import com.netraze.app.ui.survey.SurveysScreen
import com.netraze.app.ui.theme.NetrazeTheme
import com.netraze.app.ui.theme.PrimaryBlue
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextSecondary
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class ScreenState {
    object Dashboard : ScreenState()
    object Account : ScreenState()
    object Projects : ScreenState()
    data class ProjectDetail(val project: ProjectEntity) : ScreenState()
    data class BuildingDetail(val building: BuildingEntity, val project: ProjectEntity) : ScreenState()
    data class FloorDetail(val floor: FloorEntity, val building: BuildingEntity, val project: ProjectEntity) : ScreenState()
    data class Surveys(val surveyArea: SurveyAreaEntity, val floor: FloorEntity, val building: BuildingEntity, val project: ProjectEntity) : ScreenState()
    data class SurveyCanvas(val survey: SurveyEntity, val surveyArea: SurveyAreaEntity, val floor: FloorEntity, val building: BuildingEntity, val project: ProjectEntity) : ScreenState()
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

    private val surveyCanvasViewModel: SurveyCanvasViewModel by viewModels()

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

                        Scaffold(
                            bottomBar = {
                                NavigationBar(
                                    containerColor = SurfaceLight
                                ) {
                                    val isHome = currentScreen is ScreenState.Dashboard
                                    val isHierarchy = currentScreen is ScreenState.Projects ||
                                            currentScreen is ScreenState.ProjectDetail ||
                                            currentScreen is ScreenState.BuildingDetail ||
                                            currentScreen is ScreenState.FloorDetail ||
                                            currentScreen is ScreenState.Surveys ||
                                            currentScreen is ScreenState.SurveyCanvas
                                    val isAccount = currentScreen is ScreenState.Account

                                    NavigationBarItem(
                                        selected = isHome,
                                        onClick = { currentScreen = ScreenState.Dashboard },
                                        icon = { Icon(imageVector = Icons.Rounded.Home, contentDescription = "Home") },
                                        label = { Text("Home") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = PrimaryBlue,
                                            selectedTextColor = PrimaryBlue,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = isHierarchy,
                                        onClick = { currentScreen = ScreenState.Projects },
                                        icon = { Icon(imageVector = Icons.Rounded.AccountTree, contentDescription = "Hierarchy") },
                                        label = { Text("Hierarchy") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = PrimaryBlue,
                                            selectedTextColor = PrimaryBlue,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = isAccount,
                                        onClick = { currentScreen = ScreenState.Account },
                                        icon = { Icon(imageVector = Icons.Rounded.Person, contentDescription = "Account") },
                                        label = { Text("Account") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = PrimaryBlue,
                                            selectedTextColor = PrimaryBlue,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        )
                                    )
                                }
                            }
                        ) { paddingValues ->
                            Box(modifier = Modifier.padding(paddingValues)) {
                                when (val screen = currentScreen) {
                                    is ScreenState.Dashboard -> {
                                        DashboardHomeScreen(
                                            email = email,
                                            role = role,
                                            onBrowseProjects = { currentScreen = ScreenState.Projects },
                                            onStartSurveyShortcut = { currentScreen = ScreenState.Projects }
                                        )
                                    }
                                    is ScreenState.Account -> {
                                        AccountScreen(
                                            email = email,
                                            role = role,
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
                                            onSurveyClick = { survey ->
                                                currentScreen = ScreenState.SurveyCanvas(survey, screen.surveyArea, screen.floor, screen.building, screen.project)
                                            },
                                            onBackClick = { currentScreen = ScreenState.FloorDetail(screen.floor, screen.building, screen.project) }
                                        )
                                    }
                                    is ScreenState.SurveyCanvas -> {
                                        SurveyCanvasScreen(
                                            viewModel = surveyCanvasViewModel,
                                            surveyId = screen.survey.id,
                                            onBackClick = { currentScreen = ScreenState.Surveys(screen.surveyArea, screen.floor, screen.building, screen.project) }
                                        )
                                    }
                                }
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
