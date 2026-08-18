package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "simple_maps",
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
data class SimpleMapEntity(
    @PrimaryKey val id: UUID,
    val surveyAreaId: UUID,
    val artifactReference: String?,
    val createdBy: UUID,
    val createdAt: Long
)
