package com.netraze.app.data.repository

import com.netraze.app.data.local.dao.HierarchyDao
import com.netraze.app.data.local.entity.BuildingEntity
import com.netraze.app.data.local.entity.FloorEntity
import com.netraze.app.data.local.entity.FloorPlanEntity
import com.netraze.app.data.local.entity.ProjectEntity
import com.netraze.app.data.local.entity.SimpleMapEntity
import com.netraze.app.data.local.entity.SurveyAreaEntity
import com.netraze.app.data.remote.api.HierarchyApi
import com.netraze.app.data.remote.dto.BuildingDto
import com.netraze.app.data.remote.dto.CreateBuildingRequestDto
import com.netraze.app.data.remote.dto.CreateFloorRequestDto
import com.netraze.app.data.remote.dto.CreateProjectRequestDto
import com.netraze.app.data.remote.dto.CreateSurveyAreaRequestDto
import com.netraze.app.data.remote.dto.FloorDto
import com.netraze.app.data.remote.dto.ProjectDto
import com.netraze.app.data.remote.dto.SurveyAreaDto
import com.netraze.app.data.remote.dto.UpdateBuildingRequestDto
import com.netraze.app.data.remote.dto.UpdateFloorRequestDto
import com.netraze.app.data.remote.dto.UpdateProjectRequestDto
import com.netraze.app.data.remote.dto.UpdateSurveyAreaRequestDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class HierarchyRepositoryTest {

    private lateinit var fakeApi: FakeHierarchyApi
    private lateinit var fakeDao: FakeHierarchyDao
    private lateinit var repository: HierarchyRepository

    @Before
    fun setUp() {
        fakeApi = FakeHierarchyApi()
        fakeDao = FakeHierarchyDao()
        repository = HierarchyRepositoryImpl(fakeApi, fakeDao)
    }

    @Test
    fun testGetProjectsOnlineSuccessAndCacheUpdate() = runTest {
        val projectId = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        fakeApi.projectsToReturn = listOf(
            ProjectDto(projectId, ownerId, "Online Campus", true, "2026-08-19", "2026-08-19")
        )

        val result = repository.getProjects()
        assertTrue(result.isSuccess)

        val list = result.getOrNull()!!
        assertEquals(1, list.size)
        assertEquals("Online Campus", list[0].name)
        assertEquals(1, fakeDao.projectsInCache.size)
    }

    @Test
    fun testGetProjectsOfflineFallbackToCache() = runTest {
        val projectId = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        fakeDao.projectsInCache.add(
            ProjectEntity(projectId, ownerId, "Cached Offline Campus", true, 1000L, 1000L)
        )
        fakeApi.shouldThrowError = true

        val result = repository.getProjects()
        assertTrue(result.isSuccess)

        val list = result.getOrNull()!!
        assertEquals(1, list.size)
        assertEquals("Cached Offline Campus", list[0].name)
    }

    @Test
    fun testCreateProjectSuccess() = runTest {
        val result = repository.createProject("New Alpha Project")
        assertTrue(result.isSuccess)

        val entity = result.getOrNull()!!
        assertEquals("New Alpha Project", entity.name)
        assertEquals(1, fakeDao.projectsInCache.size)
    }

    private class FakeHierarchyApi : HierarchyApi {
        var shouldThrowError = false
        var projectsToReturn = listOf<ProjectDto>()

        override suspend fun getProjects(): List<ProjectDto> {
            if (shouldThrowError) throw Exception("Network error")
            return projectsToReturn
        }

        override suspend fun createProject(request: CreateProjectRequestDto): ProjectDto {
            if (shouldThrowError) throw Exception("Network error")
            return ProjectDto(
                id = UUID.randomUUID(),
                ownerId = UUID.randomUUID(),
                name = request.name,
                isActive = true,
                createdAt = "2026-08-19",
                updatedAt = "2026-08-19"
            )
        }

        override suspend fun getProject(projectId: UUID): ProjectDto = TODO()
        override suspend fun updateProject(projectId: UUID, request: UpdateProjectRequestDto): ProjectDto = TODO()
        override suspend fun getBuildings(projectId: UUID): List<BuildingDto> = emptyList()
        override suspend fun createBuilding(projectId: UUID, request: CreateBuildingRequestDto): BuildingDto = TODO()
        override suspend fun getBuilding(buildingId: UUID): BuildingDto = TODO()
        override suspend fun updateBuilding(buildingId: UUID, request: UpdateBuildingRequestDto): BuildingDto = TODO()
        override suspend fun getFloors(buildingId: UUID): List<FloorDto> = emptyList()
        override suspend fun createFloor(buildingId: UUID, request: CreateFloorRequestDto): FloorDto = TODO()
        override suspend fun getFloor(floorId: UUID): FloorDto = TODO()
        override suspend fun updateFloor(floorId: UUID, request: UpdateFloorRequestDto): FloorDto = TODO()
        override suspend fun getSurveyAreas(floorId: UUID): List<SurveyAreaDto> = emptyList()
        override suspend fun createSurveyArea(floorId: UUID, request: CreateSurveyAreaRequestDto): SurveyAreaDto = TODO()
        override suspend fun getSurveyArea(surveyAreaId: UUID): SurveyAreaDto = TODO()
        override suspend fun updateSurveyArea(surveyAreaId: UUID, request: UpdateSurveyAreaRequestDto): SurveyAreaDto = TODO()
    }

    private class FakeHierarchyDao : HierarchyDao {
        val projectsInCache = mutableListOf<ProjectEntity>()
        val buildingsInCache = mutableListOf<BuildingEntity>()

        override suspend fun insertProjects(projects: List<ProjectEntity>) {
            projectsInCache.addAll(projects)
        }

        override suspend fun getActiveProjects(): List<ProjectEntity> {
            return projectsInCache.filter { it.isActive }
        }

        override suspend fun insertBuildings(buildings: List<BuildingEntity>) {
            buildingsInCache.addAll(buildings)
        }

        override suspend fun getBuildingsForProject(projectId: UUID): List<BuildingEntity> {
            return buildingsInCache.filter { it.projectId == projectId }
        }

        override suspend fun insertFloors(floors: List<FloorEntity>) {}
        override suspend fun getFloorsForBuilding(buildingId: UUID): List<FloorEntity> = emptyList()
        override suspend fun insertSurveyAreas(surveyAreas: List<SurveyAreaEntity>) {}
        override suspend fun getSurveyAreasForFloor(floorId: UUID): List<SurveyAreaEntity> = emptyList()
        override suspend fun insertFloorPlan(floorPlan: FloorPlanEntity) {}
        override suspend fun getFloorPlanForArea(surveyAreaId: UUID): FloorPlanEntity? = null
        override suspend fun insertSimpleMap(simpleMap: SimpleMapEntity) {}
        override suspend fun getSimpleMapForArea(surveyAreaId: UUID): SimpleMapEntity? = null
    }
}
