package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "location_fixes",
    foreignKeys = [
        ForeignKey(
            entity = SpatialPositionEntity::class,
            parentColumns = ["id"],
            childColumns = ["spatialPositionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LocationFixEntity(
    @PrimaryKey val spatialPositionId: UUID,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Double,
    val capturedAt: Long
)
