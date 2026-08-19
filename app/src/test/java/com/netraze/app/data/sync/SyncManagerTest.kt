package com.netraze.app.data.sync

import com.netraze.app.data.local.dao.ScanCycleDao
import com.netraze.app.data.local.dao.SpatialPositionDao
import com.netraze.app.data.local.dao.SurveyDao
import com.netraze.app.data.local.dao.WifiObservationDao
import com.netraze.app.data.local.entity.ScanCycleEntity
import com.netraze.app.data.local.entity.SpatialPositionEntity
import com.netraze.app.data.local.entity.SurveyEntity
import com.netraze.app.data.local.entity.WifiObservationEntity
import com.netraze.app.data.remote.api.SyncApi
import com.netraze.app.data.remote.dto.SurveySyncPayloadDto
import com.netraze.app.data.remote.dto.SurveySyncResultDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerTest {

    private lateinit var fakeApi: FakeSyncApi
    private lateinit var fakeSurveyDao: FakeSurveyDao
    private lateinit var fakeSpatialDao: FakeSpatialPositionDao
    private lateinit var fakeScanCycleDao: FakeScanCycleDao
    private lateinit var fakeWifiObsDao: FakeWifiObservationDao
    private lateinit var syncManager: SyncManager

    @Before
    fun setUp() {
        fakeApi = FakeSyncApi()
        fakeSurveyDao = FakeSurveyDao()
        fakeSpatialDao = FakeSpatialPositionDao()
        fakeScanCycleDao = FakeScanCycleDao()
        fakeWifiObsDao = FakeWifiObservationDao()

        syncManager = SyncManager(
            fakeApi,
            fakeSurveyDao,
            fakeSpatialDao,
            fakeScanCycleDao,
            fakeWifiObsDao
        )
    }

    @Test
    fun testDrainPendingSyncGathersPendingItemsAndCallsApi() = runTest {
        val surveyId = UUID.randomUUID()
        val surveyAreaId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        fakeSurveyDao.surveys[surveyId] = SurveyEntity(
            id = surveyId,
            surveyAreaId = surveyAreaId,
            title = "Pending Offline Survey",
            mode = "location_survey",
            status = "in_progress",
            floorPlanId = null,
            simpleMapId = null,
            createdBy = userId,
            startedAt = System.currentTimeMillis(),
            completedAt = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncState = "pending"
        )

        val result = syncManager.drainPendingSync(surveyId)

        assertTrue(result.isSuccess)
        val resDto = result.getOrThrow()
        assertEquals(surveyId, resDto.surveyId)

        // Verify survey root syncState updated to "synced"
        assertEquals("synced", fakeSurveyDao.surveys[surveyId]?.syncState)
    }

    private class FakeSyncApi : SyncApi {
        override suspend fun syncSurvey(
            surveyId: UUID,
            payload: SurveySyncPayloadDto
        ): SurveySyncResultDto {
            return SurveySyncResultDto(
                surveyId = surveyId,
                ingestedSpatialPositions = payload.spatialPositions.size,
                ingestedScanCycles = payload.scanCycles.size,
                ingestedWifiObservations = payload.scanCycles.sumOf { it.observations.size }
            )
        }
    }

    private class FakeSurveyDao : SurveyDao {
        val surveys = mutableMapOf<UUID, SurveyEntity>()

        override suspend fun insertSurvey(survey: SurveyEntity) { surveys[survey.id] = survey }
        override suspend fun upsertSurvey(survey: SurveyEntity) { surveys[survey.id] = survey }
        override suspend fun upsertSurveys(surveys: List<SurveyEntity>) { surveys.forEach { upsertSurvey(it) } }
        override suspend fun getSurveyById(id: UUID): SurveyEntity? = surveys[id]
        override suspend fun getSurveysForArea(surveyAreaId: UUID): List<SurveyEntity> = surveys.values.filter { it.surveyAreaId == surveyAreaId }
        override suspend fun getAllSurveys(): List<SurveyEntity> = surveys.values.toList()
        override suspend fun updateSurveyCompletion(id: UUID, status: String, completedAt: Long, updatedAt: Long) {}
        override suspend fun updateSyncState(id: UUID, syncState: String) {
            surveys[id]?.let { surveys[id] = it.copy(syncState = syncState) }
        }
        override suspend fun getPendingSurveys(): List<SurveyEntity> = surveys.values.filter { it.syncState == "pending" }
    }

    private class FakeSpatialPositionDao : SpatialPositionDao {
        override suspend fun insertSpatialPosition(spatialPosition: SpatialPositionEntity) {}
        override suspend fun getSpatialPositionById(id: UUID): SpatialPositionEntity? = null
        override suspend fun getSpatialPositionsForSurvey(surveyId: UUID): List<SpatialPositionEntity> = emptyList()
    }

    private class FakeScanCycleDao : ScanCycleDao {
        override suspend fun insertScanCycle(scanCycle: ScanCycleEntity) {}
        override suspend fun insertWifiObservations(observations: List<WifiObservationEntity>) {}
        override suspend fun getScanCycleById(id: UUID): ScanCycleEntity? = null
        override suspend fun getScanCyclesForSurvey(surveyId: UUID): List<ScanCycleEntity> = emptyList()
        override suspend fun getScanCyclesForPosition(spatialPositionId: UUID): List<ScanCycleEntity> = emptyList()
    }

    private class FakeWifiObservationDao : WifiObservationDao {
        override suspend fun insertObservations(observations: List<WifiObservationEntity>) {}
        override suspend fun getObservationsForCycle(scanCycleId: UUID): List<WifiObservationEntity> = emptyList()
        override suspend fun getObservationsForBssid(bssid: String): List<WifiObservationEntity> = emptyList()
    }
}
