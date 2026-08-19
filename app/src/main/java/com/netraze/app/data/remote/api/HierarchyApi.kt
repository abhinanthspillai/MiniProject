package com.netraze.app.data.remote.api

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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface HierarchyApi {

    @GET("api/v1/projects")
    suspend fun getProjects(): List<ProjectDto>

    @POST("api/v1/projects")
    suspend fun createProject(@Body request: CreateProjectRequestDto): ProjectDto

    @GET("api/v1/projects/{projectId}")
    suspend fun getProject(@Path("projectId") projectId: UUID): ProjectDto

    @PATCH("api/v1/projects/{projectId}")
    suspend fun updateProject(@Path("projectId") projectId: UUID, @Body request: UpdateProjectRequestDto): ProjectDto

    @GET("api/v1/projects/{projectId}/buildings")
    suspend fun getBuildings(@Path("projectId") projectId: UUID): List<BuildingDto>

    @POST("api/v1/projects/{projectId}/buildings")
    suspend fun createBuilding(@Path("projectId") projectId: UUID, @Body request: CreateBuildingRequestDto): BuildingDto

    @GET("api/v1/buildings/{buildingId}")
    suspend fun getBuilding(@Path("buildingId") buildingId: UUID): BuildingDto

    @PATCH("api/v1/buildings/{buildingId}")
    suspend fun updateBuilding(@Path("buildingId") buildingId: UUID, @Body request: UpdateBuildingRequestDto): BuildingDto

    @GET("api/v1/buildings/{buildingId}/floors")
    suspend fun getFloors(@Path("buildingId") buildingId: UUID): List<FloorDto>

    @POST("api/v1/buildings/{buildingId}/floors")
    suspend fun createFloor(@Path("buildingId") buildingId: UUID, @Body request: CreateFloorRequestDto): FloorDto

    @GET("api/v1/floors/{floorId}")
    suspend fun getFloor(@Path("floorId") floorId: UUID): FloorDto

    @PATCH("api/v1/floors/{floorId}")
    suspend fun updateFloor(@Path("floorId") floorId: UUID, @Body request: UpdateFloorRequestDto): FloorDto

    @GET("api/v1/floors/{floorId}/survey-areas")
    suspend fun getSurveyAreas(@Path("floorId") floorId: UUID): List<SurveyAreaDto>

    @POST("api/v1/floors/{floorId}/survey-areas")
    suspend fun createSurveyArea(@Path("floorId") floorId: UUID, @Body request: CreateSurveyAreaRequestDto): SurveyAreaDto

    @GET("api/v1/survey-areas/{surveyAreaId}")
    suspend fun getSurveyArea(@Path("surveyAreaId") surveyAreaId: UUID): SurveyAreaDto

    @PATCH("api/v1/survey-areas/{surveyAreaId}")
    suspend fun updateSurveyArea(@Path("surveyAreaId") surveyAreaId: UUID, @Body request: UpdateSurveyAreaRequestDto): SurveyAreaDto
}
