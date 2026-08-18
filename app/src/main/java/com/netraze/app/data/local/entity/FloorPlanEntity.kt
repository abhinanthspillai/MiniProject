package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "floor_plans",
    foreignKeys = [
        ForeignKey(
            entity = SurveyAreaEntity::class,
            parentColumns = ["id"],
            childColumns = ["surveyAreaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("surveyAreaId")]
)
data class FloorPlanEntity(
    @PrimaryKey val id: UUID,
    val surveyAreaId: UUID,
    val storagePath: String,
    val originalFilename: String,
    val widthPx: Int,
    val heightPx: Int,
    val uploadedBy: UUID,
    val createdAt: Long
)
