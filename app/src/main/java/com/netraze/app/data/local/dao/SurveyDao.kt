package com.netraze.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.netraze.app.data.local.entity.SurveyEntity
import java.util.UUID

@Dao
interface SurveyDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSurvey(survey: SurveyEntity)

    @Query("SELECT * FROM surveys WHERE id = :id")
    suspend fun getSurveyById(id: UUID): SurveyEntity?

    @Query("SELECT * FROM surveys WHERE surveyAreaId = :surveyAreaId")
    suspend fun getSurveysForArea(surveyAreaId: UUID): List<SurveyEntity>

    @Query("UPDATE surveys SET status = :status, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSurveyCompletion(id: UUID, status: String, completedAt: Long, updatedAt: Long)

    @Query("UPDATE surveys SET syncState = :syncState WHERE id = :id")
    suspend fun updateSyncState(id: UUID, syncState: String)

    @Query("SELECT * FROM surveys WHERE syncState = 'pending'")
    suspend fun getPendingSurveys(): List<SurveyEntity>
}
