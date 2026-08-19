package com.netraze.app.data.remote.api

import com.netraze.app.data.remote.dto.CreateSurveyRequestDto
import com.netraze.app.data.remote.dto.SurveyDto
import com.netraze.app.data.remote.dto.UpdateSurveyRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface SurveyApi {

    @POST("api/v1/survey-areas/{surveyAreaId}/surveys")
    suspend fun createSurvey(
        @Path("surveyAreaId") surveyAreaId: UUID,
        @Body request: CreateSurveyRequestDto
    ): SurveyDto

    @GET("api/v1/survey-areas/{surveyAreaId}/surveys")
    suspend fun getSurveysForArea(
        @Path("surveyAreaId") surveyAreaId: UUID
    ): List<SurveyDto>

    @GET("api/v1/surveys/{surveyId}")
    suspend fun getSurvey(
        @Path("surveyId") surveyId: UUID
    ): SurveyDto

    @PATCH("api/v1/surveys/{surveyId}")
    suspend fun updateSurvey(
        @Path("surveyId") surveyId: UUID,
        @Body request: UpdateSurveyRequestDto
    ): SurveyDto
}
