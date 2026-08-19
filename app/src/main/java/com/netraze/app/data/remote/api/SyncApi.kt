package com.netraze.app.data.remote.api

import com.netraze.app.data.remote.dto.SurveySyncPayloadDto
import com.netraze.app.data.remote.dto.SurveySyncResultDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface SyncApi {

    @POST("api/v1/surveys/{surveyId}/sync")
    suspend fun syncSurvey(
        @Path("surveyId") surveyId: UUID,
        @Body payload: SurveySyncPayloadDto
    ): SurveySyncResultDto
}
