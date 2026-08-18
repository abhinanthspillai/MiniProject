package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "survey_areas",
    foreignKeys = [
        ForeignKey(
            entity = FloorEntity::class,
            parentColumns = ["id"],
            childColumns = ["floorId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("floorId")]
)
data class SurveyAreaEntity(
    @PrimaryKey val id: UUID,
    val floorId: UUID,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
)
