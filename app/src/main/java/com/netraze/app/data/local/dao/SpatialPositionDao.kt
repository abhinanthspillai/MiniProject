package com.netraze.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.netraze.app.data.local.entity.SpatialPositionEntity
import java.util.UUID

@Dao
interface SpatialPositionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSpatialPosition(spatialPosition: SpatialPositionEntity)

    @Query("SELECT * FROM spatial_positions WHERE id = :id")
    suspend fun getSpatialPositionById(id: UUID): SpatialPositionEntity?

    @Query("SELECT * FROM spatial_positions WHERE surveyId = :surveyId")
    suspend fun getSpatialPositionsForSurvey(surveyId: UUID): List<SpatialPositionEntity>
}
