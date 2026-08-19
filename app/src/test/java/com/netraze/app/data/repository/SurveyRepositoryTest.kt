package com.netraze.app.data.repository

import com.netraze.app.data.local.dao.SurveyDao
import com.netraze.app.data.local.entity.SurveyEntity
import com.netraze.app.data.remote.api.SurveyApi
import com.netraze.app.data.remote.dto.CreateSurveyRequestDto
import com.netraze.app.data.remote.dto.SurveyDto
import com.netraze.app.data.remote.dto.UpdateSurveyRequestDto
import com.netraze.app.data.security.AuthSession
import com.netraze.app.data.security.SecureSessionStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SurveyRepositoryTest {

    private lateinit var fakeApi: FakeSurveyApi
    private lateinit var fakeDao: FakeSurveyDao
    private lateinit var mockSessionStore: SecureSessionStore
    private lateinit var repository: SurveyRepository

    @Before
    fun setUp() {
        fakeApi = FakeSurveyApi()
        fakeDao = FakeSurveyDao()
        mockSessionStore = Mockito.mock(SecureSessionStore::class.java)
        runBlocking {
            Mockito.`when`(mockSessionStore.getSession()).thenReturn(
                AuthSession("fake_token", UUID.randomUUID(), "test@netraze.app", "survey_technician")
            )
        }
        repository = SurveyRepositoryImpl(fakeApi, fakeDao, mockSessionStore)
    }

    @Test
    fun testCreateSurveyGeneratesCanonicalUuidFirstAndPersistsLocally() = runTest {
        val surveyAreaId = UUID.randomUUID()
        val result = repository.createSurvey(
            surveyAreaId = surveyAreaId,
            title = "Test Location Survey",
            mode = "location_survey"
        )

        assertTrue(result.isSuccess)
        val entity = result.getOrThrow()

        // Verify entity persisted in Room cache
        assertEquals(1, fakeDao.surveysInCache.size)
        val cached = fakeDao.surveysInCache[0]
        assertEquals(entity.id, cached.id) // Same canonical UUID
        assertEquals("location_survey", cached.mode)
        assertEquals("Test Location Survey", cached.title)

        // Verify API was called with the SAME canonical Android UUID
        assertEquals(entity.id, fakeApi.lastCreateRequest?.id)
    }

    @Test
    fun testCreateSurveyOfflineFallbackRetainsPendingSyncState() = runTest {
        fakeApi.shouldThrowError = true
        val surveyAreaId = UUID.randomUUID()

        val result = repository.createSurvey(
            surveyAreaId = surveyAreaId,
            title = "Offline Survey",
            mode = "location_survey"
        )

        assertTrue(result.isSuccess)
        val entity = result.getOrThrow()
        assertEquals("pending", entity.syncState)
        assertEquals(1, fakeDao.surveysInCache.size)
    }

    private class FakeSurveyApi : SurveyApi {
        var shouldThrowError = false
        var lastCreateRequest: CreateSurveyRequestDto? = null

        override suspend fun createSurvey(
            surveyAreaId: UUID,
            request: CreateSurveyRequestDto
        ): SurveyDto {
            lastCreateRequest = request
            if (shouldThrowError) throw Exception("Network connection failed")

            val createdId = request.id ?: UUID.randomUUID()
            return SurveyDto(
                id = createdId,
                surveyAreaId = surveyAreaId,
                title = request.title,
                mode = request.mode,
                status = "in_progress",
                floorPlanId = request.floorPlanId,
                simpleMapId = request.simpleMapId,
                createdBy = UUID.randomUUID(),
                startedAt = "2026-08-19T00:00:00Z",
                completedAt = null,
                createdAt = "2026-08-19T00:00:00Z",
                updatedAt = "2026-08-19T00:00:00Z"
            )
        }

        override suspend fun getSurveysForArea(surveyAreaId: UUID): List<SurveyDto> = emptyList()
        override suspend fun getSurvey(surveyId: UUID): SurveyDto = TODO()
        override suspend fun updateSurvey(surveyId: UUID, request: UpdateSurveyRequestDto): SurveyDto = TODO()
        override suspend fun completeSurvey(surveyId: UUID): SurveyDto = TODO()
    }

    private class FakeSurveyDao : SurveyDao {
        val surveysInCache = mutableListOf<SurveyEntity>()

        override suspend fun insertSurvey(survey: SurveyEntity) {
            surveysInCache.add(survey)
        }

        override suspend fun upsertSurvey(survey: SurveyEntity) {
            val index = surveysInCache.indexOfFirst { it.id == survey.id }
            if (index >= 0) surveysInCache[index] = survey else surveysInCache.add(survey)
        }

        override suspend fun upsertSurveys(surveys: List<SurveyEntity>) {
            surveys.forEach { upsertSurvey(it) }
        }

        override suspend fun getSurveyById(id: UUID): SurveyEntity? {
            return surveysInCache.find { it.id == id }
        }

        override suspend fun getSurveysForArea(surveyAreaId: UUID): List<SurveyEntity> {
            return surveysInCache.filter { it.surveyAreaId == surveyAreaId }
        }

        override suspend fun updateSurveyCompletion(
            id: UUID,
            status: String,
            completedAt: Long,
            updatedAt: Long
        ) {
            val index = surveysInCache.indexOfFirst { it.id == id }
            if (index >= 0) {
                val old = surveysInCache[index]
                surveysInCache[index] = old.copy(status = status, completedAt = completedAt, updatedAt = updatedAt)
            }
        }

        override suspend fun updateSyncState(id: UUID, syncState: String) {
            val index = surveysInCache.indexOfFirst { it.id == id }
            if (index >= 0) {
                val old = surveysInCache[index]
                surveysInCache[index] = old.copy(syncState = syncState)
            }
        }

        override suspend fun getPendingSurveys(): List<SurveyEntity> {
            return surveysInCache.filter { it.syncState == "pending" }
        }
    }
}
