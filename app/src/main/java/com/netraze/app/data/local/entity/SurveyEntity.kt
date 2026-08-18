package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "surveys",
    foreignKeys = [
        ForeignKey(
            entity = SurveyAreaEntity::class,
            parentColumns = ["id"],
            childColumns = ["surveyAreaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("surveyAreaId"), Index("status")]
)
data class SurveyEntity(
    @PrimaryKey val id: UUID, // Canonical UUID generated on Android
    val surveyAreaId: UUID,
    val title: String,
    val mode: String, // "floor_plan", "simple_map", "location_survey"
    val status: String = "in_progress", // "in_progress", "completed"
    val floorPlanId: UUID?,
    val simpleMapId: UUID?,
    val createdBy: UUID,
    val startedAt: Long,
    val completedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: String = "pending" // "pending", "synced"
)
