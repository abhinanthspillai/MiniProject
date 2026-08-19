package com.netraze.app.data.sync

import com.netraze.app.data.local.dao.ScanCycleDao
import com.netraze.app.data.local.dao.SpatialPositionDao
import com.netraze.app.data.local.dao.SurveyDao
import com.netraze.app.data.local.dao.WifiObservationDao
import com.netraze.app.data.remote.api.SyncApi
import com.netraze.app.data.remote.dto.CreateSurveyRequestDto
import com.netraze.app.data.remote.dto.ScanCycleSyncDto
import com.netraze.app.data.remote.dto.SpatialPositionSyncDto
import com.netraze.app.data.remote.dto.SurveySyncPayloadDto
import com.netraze.app.data.remote.dto.SurveySyncResultDto
import com.netraze.app.data.remote.dto.WifiObservationSyncDto
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val syncApi: SyncApi,
    private val surveyDao: SurveyDao,
    private val spatialPositionDao: SpatialPositionDao,
    private val scanCycleDao: ScanCycleDao,
    private val wifiObservationDao: WifiObservationDao
) {

    suspend fun drainPendingSync(surveyId: UUID): Result<SurveySyncResultDto> {
        val survey = surveyDao.getSurveyById(surveyId) ?: return Result.failure(IllegalStateException("Survey not found"))

        // 1. Gather pending survey root if pending
        val rootDto = if (survey.syncState == "pending") {
            CreateSurveyRequestDto(
                id = survey.id,
                title = survey.title,
                mode = survey.mode,
                floorPlanId = survey.floorPlanId,
                simpleMapId = survey.simpleMapId
            )
        } else null

        // 2. Gather pending spatial positions
        val positions = spatialPositionDao.getSpatialPositionsForSurvey(surveyId).filter { it.syncState == "pending" }
        val positionDtos = positions.map { pos ->
            SpatialPositionSyncDto(
                id = pos.id,
                label = pos.label,
                floorPlanX = pos.floorPlanX,
                floorPlanY = pos.floorPlanY,
                simpleMapX = pos.simpleMapX,
                simpleMapY = pos.simpleMapY,
                latitude = pos.latitude,
                longitude = pos.longitude,
                accuracyMeters = pos.accuracyMeters,
                capturedAt = pos.capturedAt?.let { Instant.ofEpochMilli(it).toString() },
                createdAt = Instant.ofEpochMilli(pos.createdAt).toString()
            )
        }

        // 3. Gather pending scan cycles & wifi observations
        val cycles = scanCycleDao.getScanCyclesForSurvey(surveyId).filter { it.syncState == "pending" }
        val cycleDtos = cycles.map { cycle ->
            val observations = wifiObservationDao.getObservationsForCycle(cycle.id)
            val obsDtos = observations.map { obs ->
                WifiObservationSyncDto(
                    id = obs.id,
                    scanCycleId = obs.scanCycleId,
                    ssid = obs.ssid,
                    bssid = obs.bssid,
                    rssiDbm = obs.rssiDbm,
                    frequencyMhz = obs.frequencyMhz,
                    channel = obs.channel,
                    channelSource = obs.channelSource,
                    capabilities = obs.capabilities
                )
            }
            ScanCycleSyncDto(
                id = cycle.id,
                spatialPositionId = cycle.spatialPositionId,
                capturedAtWallclock = Instant.ofEpochMilli(cycle.capturedAtWallclock).toString(),
                androidScanTimestampRaw = cycle.androidScanTimestampRaw,
                freshResults = cycle.freshResults,
                createdAt = Instant.ofEpochMilli(cycle.createdAt).toString(),
                observations = obsDtos
            )
        }

        val payload = SurveySyncPayloadDto(
            survey = rootDto,
            spatialPositions = positionDtos,
            scanCycles = cycleDtos
        )

        return try {
            val result = syncApi.syncSurvey(surveyId, payload)

            // Update Room sync states on success
            if (survey.syncState == "pending") {
                surveyDao.updateSyncState(surveyId, "synced")
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
