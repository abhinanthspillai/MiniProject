package com.netraze.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
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
import com.netraze.app.ui.auth.CreateUserScreen
import com.netraze.app.ui.auth.LoginRoute
import com.netraze.app.ui.auth.LoginViewModel
import com.netraze.app.ui.auth.ResetPasswordScreen
import com.netraze.app.ui.canvas.SurveyCanvasScreen
import com.netraze.app.ui.canvas.SurveyCanvasViewModel
import com.netraze.app.ui.dashboard.DashboardHomeScreen
import com.netraze.app.ui.hierarchy.BuildingDetailScreen
import com.netraze.app.ui.hierarchy.CompactLocationSelectorDialog
import com.netraze.app.ui.hierarchy.FloorDetailScreen
import com.netraze.app.ui.hierarchy.HierarchyViewModel
import com.netraze.app.ui.hierarchy.LocationsTabScreen
import com.netraze.app.ui.hierarchy.ProjectDetailScreen
import com.netraze.app.ui.hierarchy.ProjectsScreen
import com.netraze.app.ui.survey.AllSurveysTabScreen
import com.netraze.app.ui.survey.CreateSurveyDialog
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
    object AllSurveys : ScreenState()
    object Locations : ScreenState()
    object Account : ScreenState()
    object Projects : ScreenState()
    data class ProjectDetail(val project: ProjectEntity) : ScreenState()
    data class BuildingDetail(val building: BuildingEntity, val project: ProjectEntity) : ScreenState()
    data class FloorDetail(val floor: FloorEntity, val building: BuildingEntity, val project: ProjectEntity) : ScreenState()
    data class Surveys(val surveyArea: SurveyAreaEntity, val floor: FloorEntity, val building: BuildingEntity, val project: ProjectEntity) : ScreenState()
    data class SurveyCanvas(val survey: SurveyEntity, val surveyArea: SurveyAreaEntity?, val floor: FloorEntity?, val building: BuildingEntity?, val project: ProjectEntity?) : ScreenState()
}

data class SurveyLocationContext(
    val surveyArea: SurveyAreaEntity,
    val floor: FloorEntity,
    val building: BuildingEntity,
    val project: ProjectEntity
)

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
                    val surveysState by surveyViewModel.uiState.collectAsStateWithLifecycle()
                    var currentScreen by remember { mutableStateOf<ScreenState>(ScreenState.Dashboard) }
                    var showLocationSelector by remember { mutableStateOf(false) }
                    var pendingSurveyLocation by remember { mutableStateOf<SurveyLocationContext?>(null) }
                    var showSurveyDetailsDialog by remember { mutableStateOf(false) }
                    var isCreateUserFlowActive by remember { mutableStateOf(false) }
                    var isResetPasswordFlowActive by remember { mutableStateOf(false) }

                    LaunchedEffect(authState.isAuthenticated) {
                        if (authState.isAuthenticated) {
                            surveyViewModel.loadAllSurveys()
                        } else {
                            currentScreen = ScreenState.Dashboard
                            showLocationSelector = false
                            pendingSurveyLocation = null
                            showSurveyDetailsDialog = false
                        }
                    }

                    if (authState.isAuthenticated) {
                        val email = authState.userProfile?.email ?: authState.session?.email ?: "Unknown"
                        val role = authState.userProfile?.role ?: authState.session?.role ?: "Unknown"
                        val userId = (authState.userProfile?.id ?: authState.session?.userId)?.toString() ?: "Unknown"
                        val recentSurvey = surveysState.surveys.firstOrNull { it.status.equals("in_progress", ignoreCase = true) }
                            ?: surveysState.surveys.firstOrNull()
                        val isTopLevelScreen = currentScreen is ScreenState.Dashboard ||
                            currentScreen is ScreenState.AllSurveys ||
                            currentScreen is ScreenState.Locations ||
                            currentScreen is ScreenState.Account

                        Scaffold(
                            bottomBar = {
                                if (isTopLevelScreen) {
                                    NavigationBar(containerColor = SurfaceLight) {
                                        val isHome = currentScreen is ScreenState.Dashboard
                                        val isAllSurveys = currentScreen is ScreenState.AllSurveys
                                        val isLocations = currentScreen is ScreenState.Locations
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
                                            selected = isAllSurveys,
                                            onClick = { currentScreen = ScreenState.AllSurveys },
                                            icon = { Icon(imageVector = Icons.Rounded.Assignment, contentDescription = "Surveys") },
                                            label = { Text("Surveys") },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = PrimaryBlue,
                                                selectedTextColor = PrimaryBlue,
                                                unselectedIconColor = TextSecondary,
                                                unselectedTextColor = TextSecondary
                                            )
                                        )
                                        NavigationBarItem(
                                            selected = isLocations,
                                            onClick = { currentScreen = ScreenState.Locations },
                                            icon = { Icon(imageVector = Icons.Rounded.LocationOn, contentDescription = "Locations") },
                                            label = { Text("Locations") },
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
                            }
                        ) { paddingValues ->
                            Box(modifier = Modifier.padding(paddingValues)) {
                                when (val screen = currentScreen) {
                                    is ScreenState.Dashboard -> {
                                        DashboardHomeScreen(
                                            email = email,
                                            role = role,
                                            recentSurvey = recentSurvey,
                                            onStartSurveyClick = { showLocationSelector = true },
                                            onContinueSurveyClick = { survey ->
                                                currentScreen = ScreenState.SurveyCanvas(survey, null, null, null, null)
                                            },
                                            onBrowseLocations = { currentScreen = ScreenState.Locations }
                                        )
                                    }
                                    is ScreenState.AllSurveys -> {
                                        AllSurveysTabScreen(
                                            viewModel = surveyViewModel,
                                            onSurveyClick = { survey ->
                                                currentScreen = ScreenState.SurveyCanvas(survey, null, null, null, null)
                                            },
                                            onStartSurveyClick = { showLocationSelector = true }
                                        )
                                    }
                                    is ScreenState.Locations -> {
                                        LocationsTabScreen(
                                            viewModel = hierarchyViewModel,
                                            userRole = role,
                                            currentUserId = userId,
                                            onProjectClick = { project -> currentScreen = ScreenState.ProjectDetail(project) },
                                            onManageLocationsClick = { currentScreen = ScreenState.Projects }
                                        )
                                    }
                                    is ScreenState.Account -> {
                                        AccountScreen(
                                            email = email,
                                            role = role,
                                            onSignOut = { loginViewModel.logout() }
                                        )
                                    }
                                    is ScreenState.Projects -> {
                                        ProjectsScreen(
                                            viewModel = hierarchyViewModel,
                                            userRole = role,
                                            currentUserId = userId,
                                            onProjectClick = { project -> currentScreen = ScreenState.ProjectDetail(project) },
                                            onBackClick = { currentScreen = ScreenState.Locations }
                                        )
                                    }
                                    is ScreenState.ProjectDetail -> {
                                        ProjectDetailScreen(
                                            viewModel = hierarchyViewModel,
                                            project = screen.project,
                                            userRole = role,
                                            currentUserId = userId,
                                            onBuildingClick = { building -> currentScreen = ScreenState.BuildingDetail(building, screen.project) },
                                            onBackClick = { currentScreen = ScreenState.Locations }
                                        )
                                    }
                                    is ScreenState.BuildingDetail -> {
                                        BuildingDetailScreen(
                                            viewModel = hierarchyViewModel,
                                            building = screen.building,
                                            project = screen.project,
                                            userRole = role,
                                            currentUserId = userId,
                                            onFloorClick = { floor -> currentScreen = ScreenState.FloorDetail(floor, screen.building, screen.project) },
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
                                            onSurveyAreaClick = { area -> currentScreen = ScreenState.Surveys(area, screen.floor, screen.building, screen.project) },
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
                                            onSurveyClick = { survey -> currentScreen = ScreenState.SurveyCanvas(survey, screen.surveyArea, screen.floor, screen.building, screen.project) },
                                            onBackClick = { currentScreen = ScreenState.FloorDetail(screen.floor, screen.building, screen.project) }
                                        )
                                    }
                                    is ScreenState.SurveyCanvas -> {
                                        SurveyCanvasScreen(
                                            viewModel = surveyCanvasViewModel,
                                            surveyId = screen.survey.id,
                                            onBackClick = { currentScreen = ScreenState.AllSurveys }
                                        )
                                    }
                                }

                                if (showLocationSelector) {
                                    CompactLocationSelectorDialog(
                                        viewModel = hierarchyViewModel,
                                        onDismiss = { showLocationSelector = false },
                                        onLocationSelected = { area, floor, building, project ->
                                            pendingSurveyLocation = SurveyLocationContext(area, floor, building, project)
                                            showLocationSelector = false
                                            showSurveyDetailsDialog = true
                                        }
                                    )
                                }

                                if (showSurveyDetailsDialog) {
                                    val location = pendingSurveyLocation
                                    if (location != null) {
                                        CreateSurveyDialog(
                                            onDismiss = {
                                                showSurveyDetailsDialog = false
                                                pendingSurveyLocation = null
                                            },
                                            onConfirm = { title, mode ->
                                                showSurveyDetailsDialog = false
                                                surveyViewModel.createSurvey(
                                                    surveyAreaId = location.surveyArea.id,
                                                    title = title,
                                                    mode = mode,
                                                    onSuccess = { newSurvey ->
                                                        pendingSurveyLocation = null
                                                        currentScreen = ScreenState.SurveyCanvas(
                                                            survey = newSurvey,
                                                            surveyArea = location.surveyArea,
                                                            floor = location.floor,
                                                            building = location.building,
                                                            project = location.project
                                                        )
                                                    }
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else if (isCreateUserFlowActive) {
                        CreateUserScreen(
                            viewModel = loginViewModel,
                            onBackToLogin = { isCreateUserFlowActive = false }
                        )
                    } else if (isResetPasswordFlowActive) {
                        ResetPasswordScreen(
                            viewModel = loginViewModel,
                            onBackToLogin = { isResetPasswordFlowActive = false }
                        )
                    } else {
                        LoginRoute(
                            viewModel = loginViewModel,
                            onCreateUserClick = { isCreateUserFlowActive = true },
                            onForgotPasswordClick = { isResetPasswordFlowActive = true },
                            onLoginSubmitted = { _, _ -> }
                        )
                    }
                }
            }
        }
    }
}
