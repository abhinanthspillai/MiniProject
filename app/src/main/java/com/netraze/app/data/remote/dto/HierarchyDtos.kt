package com.netraze.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class ProjectDto(
    val id: UUID,
    @SerializedName("owner_id") val ownerId: UUID,
    val name: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class CreateProjectRequestDto(
    val name: String
)

data class UpdateProjectRequestDto(
    val name: String
)

data class BuildingDto(
    val id: UUID,
    @SerializedName("project_id") val projectId: UUID,
    val name: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class CreateBuildingRequestDto(
    val name: String
)

data class UpdateBuildingRequestDto(
    val name: String
)

data class FloorDto(
    val id: UUID,
    @SerializedName("building_id") val buildingId: UUID,
    val name: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class CreateFloorRequestDto(
    val name: String
)

data class UpdateFloorRequestDto(
    val name: String
)

data class SurveyAreaDto(
    val id: UUID,
    @SerializedName("floor_id") val floorId: UUID,
    val name: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class CreateSurveyAreaRequestDto(
    val name: String
)

data class UpdateSurveyAreaRequestDto(
    val name: String
)
