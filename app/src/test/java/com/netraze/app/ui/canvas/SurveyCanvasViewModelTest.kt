package com.netraze.app.ui.canvas

import com.netraze.app.data.local.dao.ScanCycleDao
import com.netraze.app.data.local.dao.SpatialPositionDao
import com.netraze.app.data.local.dao.SurveyDao
import com.netraze.app.data.local.dao.WifiObservationDao
import com.netraze.app.data.local.entity.ScanCycleEntity
import com.netraze.app.data.local.entity.SpatialPositionEntity
import com.netraze.app.data.local.entity.SurveyEntity
import com.netraze.app.data.local.entity.WifiObservationEntity
import com.netraze.app.data.wifi.WifiScanCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SurveyCanvasViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSurveyDao: FakeSurveyDao
    private lateinit var fakeSpatialDao: FakeSpatialPositionDao
    private lateinit var fakeScanCycleDao: FakeScanCycleDao
    private lateinit var fakeWifiObsDao: FakeWifiObservationDao
    private lateinit var mockWifiScanCoordinator: WifiScanCoordinator
    private lateinit var viewModel: SurveyCanvasViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSurveyDao = FakeSurveyDao()
        fakeSpatialDao = FakeSpatialPositionDao()
        fakeScanCycleDao = FakeScanCycleDao()
        fakeWifiObsDao = FakeWifiObservationDao()
        mockWifiScanCoordinator = Mockito.mock(WifiScanCoordinator::class.java)

        viewModel = SurveyCanvasViewModel(
            fakeSurveyDao,
            fakeSpatialDao,
            fakeScanCycleDao,
            fakeWifiObsDao,
            mockWifiScanCoordinator
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadSurveyCanvasDataComputesSpatialAnalyticsCorrectly() = runTest {
        val surveyId = UUID.randomUUID()
        val areaId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        fakeSurveyDao.surveys[surveyId] = SurveyEntity(
            id = surveyId,
            surveyAreaId = areaId,
            title = "Canvas Test Survey",
            mode = "location_survey",
            status = "in_progress",
            floorPlanId = null,
            simpleMapId = null,
            createdBy = userId,
            startedAt = System.currentTimeMillis(),
            completedAt = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncState = "synced"
        )

        val posId = UUID.randomUUID()
        fakeSpatialDao.positions.add(
            SpatialPositionEntity(
                id = posId,
                surveyId = surveyId,
                label = "Point A",
                floorPlanX = null,
                floorPlanY = null,
                simpleMapX = null,
                simpleMapY = null,
                latitude = 12.9716,
                longitude = 77.5946,
                accuracyMeters = 2.5,
                capturedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                syncState = "synced"
            )
        )

        val cycleId = UUID.randomUUID()
        fakeScanCycleDao.cycles.add(
            ScanCycleEntity(
                id = cycleId,
                surveyId = surveyId,
                spatialPositionId = posId,
                capturedAtWallclock = System.currentTimeMillis(),
                androidScanTimestampRaw = 123456789L,
                freshResults = true,
                createdAt = System.currentTimeMillis(),
                syncState = "synced"
            )
        )

        fakeWifiObsDao.observations.addAll(
            listOf(
                WifiObservationEntity(
                    id = UUID.randomUUID(),
                    scanCycleId = cycleId,
                    ssid = "AP_5G",
                    bssid = "11:22:33:44:55:66",
                    rssiDbm = -40,
                    frequencyMhz = 5180,
                    channel = 36,
                    channelSource = "frequency_conversion",
                    capabilities = "WPA2"
                ),
                WifiObservationEntity(
                    id = UUID.randomUUID(),
                    scanCycleId = cycleId,
                    ssid = "AP_2G",
                    bssid = "AA:BB:CC:DD:EE:FF",
                    rssiDbm = -60,
                    frequencyMhz = 2412,
                    channel = 1,
                    channelSource = "frequency_conversion",
                    capabilities = "WPA2"
                )
            )
        )

        viewModel.loadSurveyCanvasData(surveyId)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.survey)
        assertEquals("Canvas Test Survey", state.survey?.title)
        assertEquals(1, state.positions.size)
        assertEquals(2, state.totalObservationsCount)
        assertEquals(2, state.uniqueBssidCount)
        assertEquals(-40, state.maxRssi)
        assertEquals(-50.0, state.avgRssi!!, 0.001)
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
        override suspend fun updateSyncState(id: UUID, syncState: String) {}
        override suspend fun getPendingSurveys(): List<SurveyEntity> = emptyList()
    }

    private class FakeSpatialPositionDao : SpatialPositionDao {
        val positions = mutableListOf<SpatialPositionEntity>()

        override suspend fun insertSpatialPosition(spatialPosition: SpatialPositionEntity) { positions.add(spatialPosition) }
        override suspend fun getSpatialPositionById(id: UUID): SpatialPositionEntity? = positions.find { it.id == id }
        override suspend fun getSpatialPositionsForSurvey(surveyId: UUID): List<SpatialPositionEntity> = positions.filter { it.surveyId == surveyId }
    }

    private class FakeScanCycleDao : ScanCycleDao {
        val cycles = mutableListOf<ScanCycleEntity>()

        override suspend fun insertScanCycle(scanCycle: ScanCycleEntity) { cycles.add(scanCycle) }
        override suspend fun insertWifiObservations(observations: List<WifiObservationEntity>) {}
        override suspend fun getScanCycleById(id: UUID): ScanCycleEntity? = cycles.find { it.id == id }
        override suspend fun getScanCyclesForSurvey(surveyId: UUID): List<ScanCycleEntity> = cycles.filter { it.surveyId == surveyId }
        override suspend fun getScanCyclesForPosition(spatialPositionId: UUID): List<ScanCycleEntity> = cycles.filter { it.spatialPositionId == spatialPositionId }
    }

    private class FakeWifiObservationDao : WifiObservationDao {
        val observations = mutableListOf<WifiObservationEntity>()

        override suspend fun insertObservations(observations: List<WifiObservationEntity>) { this.observations.addAll(observations) }
        override suspend fun getObservationsForCycle(scanCycleId: UUID): List<WifiObservationEntity> = observations.filter { it.scanCycleId == scanCycleId }
        override suspend fun getObservationsForBssid(bssid: String): List<WifiObservationEntity> = observations.filter { it.bssid == bssid }
    }
}
