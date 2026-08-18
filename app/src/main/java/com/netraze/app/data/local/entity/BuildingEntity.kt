package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "buildings",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("projectId")]
)
data class BuildingEntity(
    @PrimaryKey val id: UUID,
    val projectId: UUID,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
)
