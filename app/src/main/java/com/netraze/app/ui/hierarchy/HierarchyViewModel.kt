package com.netraze.app.ui.hierarchy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netraze.app.data.local.entity.BuildingEntity
import com.netraze.app.data.local.entity.FloorEntity
import com.netraze.app.data.local.entity.ProjectEntity
import com.netraze.app.data.local.entity.SurveyAreaEntity
import com.netraze.app.data.repository.HierarchyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class HierarchyViewModel @Inject constructor(
    private var repository: HierarchyRepository?
) : ViewModel() {

    constructor() : this(null)

    private val _projects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val projects: StateFlow<List<ProjectEntity>> = _projects.asStateFlow()

    private val _buildings = MutableStateFlow<List<BuildingEntity>>(emptyList())
    val buildings: StateFlow<List<BuildingEntity>> = _buildings.asStateFlow()

    private val _floors = MutableStateFlow<List<FloorEntity>>(emptyList())
    val floors: StateFlow<List<FloorEntity>> = _floors.asStateFlow()

    private val _surveyAreas = MutableStateFlow<List<SurveyAreaEntity>>(emptyList())
    val surveyAreas: StateFlow<List<SurveyAreaEntity>> = _surveyAreas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun setRepository(hierarchyRepository: HierarchyRepository) {
        this.repository = hierarchyRepository
    }

    fun clearError() {
        _error.update { null }
    }

    fun loadProjects() {
        val repo = repository ?: return
        _isLoading.update { true }
        _error.update { null }
        viewModelScope.launch {
            val result = repo.getProjects()
            _isLoading.update { false }
            result.onSuccess { list ->
                _projects.update { list }
            }.onFailure { ex ->
                _error.update { ex.message ?: "Failed to load projects" }
            }
        }
    }

    fun createProject(name: String, onSuccess: () -> Unit = {}) {
        val repo = repository ?: return
        if (name.isBlank()) return
        _isLoading.update { true }
        _error.update { null }
        viewModelScope.launch {
            val result = repo.createProject(name)
            _isLoading.update { false }
            result.onSuccess {
                loadProjects()
                onSuccess()
            }.onFailure { ex ->
                _error.update { ex.message ?: "Failed to create project" }
            }
        }
    }

    fun loadBuildings(projectId: UUID) {
        val repo = repository ?: return
        _isLoading.update { true }
        _error.update { null }
        viewModelScope.launch {
            val result = repo.getBuildings(projectId)
            _isLoading.update { false }
            result.onSuccess { list ->
                _buildings.update { list }
            }.onFailure { ex ->
                _error.update { ex.message ?: "Failed to load buildings" }
            }
        }
    }

    fun createBuilding(projectId: UUID, name: String, onSuccess: () -> Unit = {}) {
        val repo = repository ?: return
        if (name.isBlank()) return
        _isLoading.update { true }
        _error.update { null }
        viewModelScope.launch {
            val result = repo.createBuilding(projectId, name)
            _isLoading.update { false }
            result.onSuccess {
                loadBuildings(projectId)
                onSuccess()
            }.onFailure { ex ->
                _error.update { ex.message ?: "Failed to create building" }
            }
        }
    }

    fun loadFloors(buildingId: UUID) {
        val repo = repository ?: return
        _isLoading.update { true }
        _error.update { null }
        viewModelScope.launch {
            val result = repo.getFloors(buildingId)
            _isLoading.update { false }
            result.onSuccess { list ->
                _floors.update { list }
            }.onFailure { ex ->
                _error.update { ex.message ?: "Failed to load floors" }
            }
        }
    }

    fun createFloor(buildingId: UUID, name: String, onSuccess: () -> Unit = {}) {
        val repo = repository ?: return
        if (name.isBlank()) return
        _isLoading.update { true }
        _error.update { null }
        viewModelScope.launch {
            val result = repo.createFloor(buildingId, name)
            _isLoading.update { false }
            result.onSuccess {
                loadFloors(buildingId)
                onSuccess()
            }.onFailure { ex ->
                _error.update { ex.message ?: "Failed to create floor" }
            }
        }
    }

    fun loadSurveyAreas(floorId: UUID) {
        val repo = repository ?: return
        _isLoading.update { true }
        _error.update { null }
        viewModelScope.launch {
            val result = repo.getSurveyAreas(floorId)
            _isLoading.update { false }
            result.onSuccess { list ->
                _surveyAreas.update { list }
            }.onFailure { ex ->
                _error.update { ex.message ?: "Failed to load survey areas" }
            }
        }
    }

    fun createSurveyArea(floorId: UUID, name: String, onSuccess: () -> Unit = {}) {
        val repo = repository ?: return
        if (name.isBlank()) return
        _isLoading.update { true }
        _error.update { null }
        viewModelScope.launch {
            val result = repo.createSurveyArea(floorId, name)
            _isLoading.update { false }
            result.onSuccess {
                loadSurveyAreas(floorId)
                onSuccess()
            }.onFailure { ex ->
                _error.update { ex.message ?: "Failed to create survey area" }
            }
        }
    }
}
