package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "scan_cycles",
    foreignKeys = [
        ForeignKey(
            entity = SurveyEntity::class,
            parentColumns = ["id"],
            childColumns = ["surveyId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = SpatialPositionEntity::class,
            parentColumns = ["id"],
            childColumns = ["spatialPositionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("surveyId"), Index("spatialPositionId"), Index("capturedAtWallclock")]
)
data class ScanCycleEntity(
    @PrimaryKey val id: UUID, // Canonical UUID generated on Android
    val surveyId: UUID,
    val spatialPositionId: UUID?, // Nullable for unbound / unsolicited cycles
    val capturedAtWallclock: Long,
    val androidScanTimestampRaw: Long?,
    val freshResults: Boolean = true,
    val createdAt: Long,
    val syncState: String = "pending" // "pending", "synced"
)
