package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: UUID,
    val ownerId: UUID,
    val name: String,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)
