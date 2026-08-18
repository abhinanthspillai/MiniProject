package com.netraze.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.netraze.app.data.local.entity.FloorPlanPositionEntity
import com.netraze.app.data.local.entity.LocationFixEntity
import com.netraze.app.data.local.entity.SimpleMapPositionEntity
import com.netraze.app.data.local.entity.SpatialPositionEntity
import java.util.UUID

@Dao
interface SpatialPositionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSpatialPosition(spatialPosition: SpatialPositionEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFloorPlanPosition(position: FloorPlanPositionEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSimpleMapPosition(position: SimpleMapPositionEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLocationFix(fix: LocationFixEntity)

    @Query("SELECT * FROM spatial_positions WHERE surveyId = :surveyId")
    suspend fun getSpatialPositionsForSurvey(surveyId: UUID): List<SpatialPositionEntity>

    @Query("SELECT * FROM floor_plan_positions WHERE spatialPositionId = :spatialPositionId")
    suspend fun getFloorPlanPosition(spatialPositionId: UUID): FloorPlanPositionEntity?

    @Query("SELECT * FROM simple_map_positions WHERE spatialPositionId = :spatialPositionId")
    suspend fun getSimpleMapPosition(spatialPositionId: UUID): SimpleMapPositionEntity?

    @Query("SELECT * FROM location_fixes WHERE spatialPositionId = :spatialPositionId")
    suspend fun getLocationFix(spatialPositionId: UUID): LocationFixEntity?

    @Transaction
    suspend fun saveFloorPlanPosition(
        spatialPosition: SpatialPositionEntity,
        floorPlanPosition: FloorPlanPositionEntity
    ) {
        insertSpatialPosition(spatialPosition)
        insertFloorPlanPosition(floorPlanPosition)
    }

    @Transaction
    suspend fun saveLocationFixPosition(
        spatialPosition: SpatialPositionEntity,
        locationFix: LocationFixEntity
    ) {
        insertSpatialPosition(spatialPosition)
        insertLocationFix(locationFix)
    }
}
