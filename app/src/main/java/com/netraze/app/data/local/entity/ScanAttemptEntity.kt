package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Android-local only entity tracking scan dispatch context.
 * Binds trustworthy spatialPositionId at dispatch time.
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
        )
    ],
    indices = [Index("surveyId"), Index("spatialPositionId")]
)
data class ScanAttemptEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val surveyId: UUID,
    val spatialPositionId: UUID?,
    val dispatchedAtWallclock: Long,
    val status: String = "dispatched"
)
