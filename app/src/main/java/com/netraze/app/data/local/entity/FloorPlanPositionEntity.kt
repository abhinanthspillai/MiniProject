package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "floor_plan_positions",
    foreignKeys = [
        ForeignKey(
            entity = SpatialPositionEntity::class,
            parentColumns = ["id"],
            childColumns = ["spatialPositionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FloorPlanPositionEntity(
    @PrimaryKey val spatialPositionId: UUID,
    val x: Double,
    val y: Double
)
