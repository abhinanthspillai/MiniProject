package com.netraze.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.netraze.app.data.local.entity.BuildingEntity
import com.netraze.app.data.local.entity.FloorEntity
import com.netraze.app.data.local.entity.ProjectEntity
import com.netraze.app.data.local.entity.ScanAttemptEntity
import com.netraze.app.data.local.entity.ScanCycleEntity
import com.netraze.app.data.local.entity.SpatialPositionEntity
import com.netraze.app.data.local.entity.SurveyAreaEntity
import com.netraze.app.data.local.entity.SurveyEntity
import com.netraze.app.data.local.entity.WifiObservationEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class RoomDatabaseTest {

    private lateinit var db: NetrazeDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NetrazeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertHierarchyAndSurveySession() = runBlocking {
        val projectId = UUID.randomUUID()
        val buildingId = UUID.randomUUID()
        val floorId = UUID.randomUUID()
        val areaId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val surveyId = UUID.randomUUID()

        val project = ProjectEntity(projectId, userId, "HQ Project", true, 1000L, 1000L)
        val building = BuildingEntity(buildingId, projectId, "Building A", 1000L, 1000L)
        val floor = FloorEntity(floorId, buildingId, "Floor 2", 1000L, 1000L)
        val area = SurveyAreaEntity(areaId, floorId, "East Wing", 1000L, 1000L)

        db.hierarchyDao().insertProjects(listOf(project))
        db.hierarchyDao().insertBuildings(listOf(building))
        db.hierarchyDao().insertFloors(listOf(floor))
        db.hierarchyDao().insertSurveyAreas(listOf(area))

        val survey = SurveyEntity(
            id = surveyId,
            surveyAreaId = areaId,
            title = "Survey 1",
            mode = "location_survey",
            status = "in_progress",
            floorPlanId = null,
            simpleMapId = null,
            createdBy = userId,
            startedAt = 2000L,
            completedAt = null,
            createdAt = 2000L,
            updatedAt = 2000L,
            syncState = "pending"
        )
        db.surveyDao().insertSurvey(survey)

        val retrieved = db.surveyDao().getSurveyById(surveyId)
        assertNotNull(retrieved)
        assertEquals("Survey 1", retrieved?.title)
        assertEquals(surveyId, retrieved?.id) // Canonical UUID preserved
    }

    @Test
    fun testScanAttemptAndUnboundScanCycle() = runBlocking {
        val projectId = UUID.randomUUID()
        val buildingId = UUID.randomUUID()
        val floorId = UUID.randomUUID()
        val areaId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val surveyId = UUID.randomUUID()

        db.hierarchyDao().insertProjects(listOf(ProjectEntity(projectId, userId, "P", true, 1L, 1L)))
        db.hierarchyDao().insertBuildings(listOf(BuildingEntity(buildingId, projectId, "B", 1L, 1L)))
        db.hierarchyDao().insertFloors(listOf(FloorEntity(floorId, buildingId, "F", 1L, 1L)))
        db.hierarchyDao().insertSurveyAreas(listOf(SurveyAreaEntity(areaId, floorId, "A", 1L, 1L)))

        val survey = SurveyEntity(
            id = surveyId, surveyAreaId = areaId, title = "Location Survey", mode = "location_survey",
            status = "in_progress", floorPlanId = null, simpleMapId = null, createdBy = userId,
            startedAt = 10L, completedAt = null, createdAt = 10L, updatedAt = 10L
        )
        db.surveyDao().insertSurvey(survey)

        val attemptId = UUID.randomUUID()
        val attempt = ScanAttemptEntity(
            id = attemptId,
            surveyId = surveyId,
            spatialPositionId = null, // Spatially unbound attempt
            dispatchedAtWallclock = 100L,
            status = "dispatched"
        )
        db.scanAttemptDao().insertScanAttempt(attempt)

        val savedAttempt = db.scanAttemptDao().getScanAttemptById(attemptId)
        assertNotNull(savedAttempt)
        assertNull(savedAttempt?.spatialPositionId)

        // Unbound ScanCycle
        val cycleId = UUID.randomUUID()
        val cycle = ScanCycleEntity(
            id = cycleId,
            surveyId = surveyId,
            spatialPositionId = null,
            capturedAtWallclock = 105L,
            androidScanTimestampRaw = 55555L,
            freshResults = true,
            createdAt = 105L
        )

        // Insert cycle with 0 observations (structurally valid)
        db.scanCycleDao().insertScanCycleWithObservations(cycle, emptyList())

        val savedCycle = db.scanCycleDao().getScanCycleById(cycleId)
        assertNotNull(savedCycle)
        assertNull(savedCycle?.spatialPositionId)

        val obs = db.wifiObservationDao().getObservationsForCycle(cycleId)
        assertEquals(0, obs.size)
    }

    @Test
    fun testTransactionalScanCycleWithMultipleSameBssidObservations() = runBlocking {
        val projectId = UUID.randomUUID()
        val buildingId = UUID.randomUUID()
        val floorId = UUID.randomUUID()
        val areaId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val surveyId = UUID.randomUUID()
        val posId = UUID.randomUUID()

        db.hierarchyDao().insertProjects(listOf(ProjectEntity(projectId, userId, "P", true, 1L, 1L)))
        db.hierarchyDao().insertBuildings(listOf(BuildingEntity(buildingId, projectId, "B", 1L, 1L)))
        db.hierarchyDao().insertFloors(listOf(FloorEntity(floorId, buildingId, "F", 1L, 1L)))
        db.hierarchyDao().insertSurveyAreas(listOf(SurveyAreaEntity(areaId, floorId, "A", 1L, 1L)))

        val survey = SurveyEntity(
            id = surveyId, surveyAreaId = areaId, title = "S", mode = "location_survey",
            status = "in_progress", floorPlanId = null, simpleMapId = null, createdBy = userId,
            startedAt = 10L, completedAt = null, createdAt = 10L, updatedAt = 10L
        )
        db.surveyDao().insertSurvey(survey)

        val position = SpatialPositionEntity(posId, surveyId, "Point 1", 12L)
        db.spatialPositionDao().insertSpatialPosition(position)

        val cycleId = UUID.randomUUID()
        val cycle = ScanCycleEntity(
            id = cycleId, surveyId = surveyId, spatialPositionId = posId,
            capturedAtWallclock = 15L, androidScanTimestampRaw = 1000L, freshResults = true, createdAt = 15L
        )

        val bssid = "00:11:22:33:44:55"
        val obs1 = WifiObservationEntity(
            id = UUID.randomUUID(), scanCycleId = cycleId, ssid = "Netraze_WiFi",
            bssid = bssid, rssiDbm = -60, frequencyMhz = 5180, channel = 36, channelSource = "derived", capabilities = "WPA2"
        )
        val obs2 = WifiObservationEntity(
            id = UUID.randomUUID(), scanCycleId = cycleId, ssid = "Netraze_WiFi",
            bssid = bssid, rssiDbm = -58, frequencyMhz = 5180, channel = 36, channelSource = "derived", capabilities = "WPA2"
        )

        // Transactional insert
        db.scanCycleDao().insertScanCycleWithObservations(cycle, listOf(obs1, obs2))

        val obsList = db.wifiObservationDao().getObservationsForCycle(cycleId)
        assertEquals(2, obsList.size)
        assertEquals(bssid, obsList[0].bssid)
        assertEquals(bssid, obsList[1].bssid)
    }
}
