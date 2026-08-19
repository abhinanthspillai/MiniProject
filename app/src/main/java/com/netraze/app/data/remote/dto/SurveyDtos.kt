package com.netraze.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class CreateSurveyRequestDto(
    @SerializedName("id") val id: UUID? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("mode") val mode: String,
    @SerializedName("floor_plan_id") val floorPlanId: UUID? = null,
    @SerializedName("simple_map_id") val simpleMapId: UUID? = null,
    @SerializedName("started_at") val startedAt: String? = null
)

data class UpdateSurveyRequestDto(
    @SerializedName("title") val title: String? = null
)

data class SurveyDto(
    @SerializedName("id") val id: UUID,
    @SerializedName("survey_area_id") val surveyAreaId: UUID,
    @SerializedName("title") val title: String?,
    @SerializedName("mode") val mode: String,
    @SerializedName("status") val status: String,
    @SerializedName("floor_plan_id") val floorPlanId: UUID?,
    @SerializedName("simple_map_id") val simpleMapId: UUID?,
    @SerializedName("created_by") val createdBy: UUID,
    @SerializedName("started_at") val startedAt: String,
    @SerializedName("completed_at") val completedAt: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
