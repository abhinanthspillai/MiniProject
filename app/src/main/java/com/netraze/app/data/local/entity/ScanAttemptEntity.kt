package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Android-local only entity tracking scan dispatch context (LOCAL_PERSISTENCE_MODEL).
 * Binds trustworthy spatialPositionId at dispatch time.
 * Points to correlated ScanCycle via nullable FK correlatedScanCycleId (ScanAttempt -> ScanCycle = 0..1).
 */
@Entity(
    tableName = "scan_attempts",
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
        ),
        ForeignKey(
            entity = ScanCycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["correlatedScanCycleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("surveyId"),
        Index("spatialPositionId"),
        Index("correlatedScanCycleId")
    ]
)
data class ScanAttemptEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val surveyId: UUID,
    val spatialPositionId: UUID?, // Captured at dispatch time
    val correlatedScanCycleId: UUID? = null, // FK -> scan_cycles.id (0..1 correlation)
    val dispatchedAtWallclock: Long,
    val status: String = "dispatched" // "dispatched", "completed", "failed"
)
