package com.netraze.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.netraze.app.data.local.entity.BuildingEntity
import com.netraze.app.data.local.entity.FloorEntity
import com.netraze.app.data.local.entity.FloorPlanEntity
import com.netraze.app.data.local.entity.ProjectEntity
import com.netraze.app.data.local.entity.SimpleMapEntity
import com.netraze.app.data.local.entity.SurveyAreaEntity
import java.util.UUID

@Dao
interface HierarchyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Query("SELECT * FROM projects WHERE isActive = 1")
    suspend fun getActiveProjects(): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildings(buildings: List<BuildingEntity>)

    @Query("SELECT * FROM buildings WHERE projectId = :projectId")
    suspend fun getBuildingsForProject(projectId: UUID): List<BuildingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFloors(floors: List<FloorEntity>)

    @Query("SELECT * FROM floors WHERE buildingId = :buildingId")
    suspend fun getFloorsForBuilding(buildingId: UUID): List<FloorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurveyAreas(surveyAreas: List<SurveyAreaEntity>)

    @Query("SELECT * FROM survey_areas WHERE floorId = :floorId")
    suspend fun getSurveyAreasForFloor(floorId: UUID): List<SurveyAreaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFloorPlan(floorPlan: FloorPlanEntity)

    @Query("SELECT * FROM floor_plans WHERE surveyAreaId = :surveyAreaId LIMIT 1")
    suspend fun getFloorPlanForArea(surveyAreaId: UUID): FloorPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimpleMap(simpleMap: SimpleMapEntity)

    @Query("SELECT * FROM simple_maps WHERE surveyAreaId = :surveyAreaId LIMIT 1")
    suspend fun getSimpleMapForArea(surveyAreaId: UUID): SimpleMapEntity?
}
