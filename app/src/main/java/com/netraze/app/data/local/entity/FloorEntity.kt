package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "floors",
    foreignKeys = [
        ForeignKey(
            entity = BuildingEntity::class,
            parentColumns = ["id"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("buildingId")]
)
data class FloorEntity(
    @PrimaryKey val id: UUID,
    val buildingId: UUID,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
)
