package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "spatial_positions",
    foreignKeys = [
        ForeignKey(
            entity = SurveyEntity::class,
            parentColumns = ["id"],
            childColumns = ["surveyId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("surveyId")]
)
data class SpatialPositionEntity(
    @PrimaryKey val id: UUID, // Canonical UUID generated on Android
    val surveyId: UUID,
    val label: String?,
    val createdAt: Long,
    val syncState: String = "pending"
)
