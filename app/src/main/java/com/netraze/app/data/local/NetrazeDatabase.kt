package com.netraze.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.netraze.app.data.local.dao.HierarchyDao
import com.netraze.app.data.local.dao.ScanAttemptDao
import com.netraze.app.data.local.dao.ScanCycleDao
import com.netraze.app.data.local.dao.SpatialPositionDao
import com.netraze.app.data.local.dao.SurveyDao
import com.netraze.app.data.local.dao.WifiObservationDao
import com.netraze.app.data.local.entity.BuildingEntity
import com.netraze.app.data.local.entity.FloorEntity
import com.netraze.app.data.local.entity.FloorPlanEntity
import com.netraze.app.data.local.entity.ProjectEntity
import com.netraze.app.data.local.entity.ScanAttemptEntity
import com.netraze.app.data.local.entity.ScanCycleEntity
import com.netraze.app.data.local.entity.SimpleMapEntity
import com.netraze.app.data.local.entity.SpatialPositionEntity
import com.netraze.app.data.local.entity.SurveyAreaEntity
import com.netraze.app.data.local.entity.SurveyEntity
import com.netraze.app.data.local.entity.WifiObservationEntity

@Database(
    entities = [
        ProjectEntity::class,
        BuildingEntity::class,
        FloorEntity::class,
        SurveyAreaEntity::class,
        FloorPlanEntity::class,
        SimpleMapEntity::class,
        SurveyEntity::class,
        SpatialPositionEntity::class,
        ScanAttemptEntity::class,
        ScanCycleEntity::class,
        WifiObservationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NetrazeDatabase : RoomDatabase() {
    abstract fun hierarchyDao(): HierarchyDao
    abstract fun surveyDao(): SurveyDao
    abstract fun spatialPositionDao(): SpatialPositionDao
    abstract fun scanAttemptDao(): ScanAttemptDao
    abstract fun scanCycleDao(): ScanCycleDao
    abstract fun wifiObservationDao(): WifiObservationDao
}
