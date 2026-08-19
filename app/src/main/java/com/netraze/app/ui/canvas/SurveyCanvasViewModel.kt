package com.netraze.app.ui.canvas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netraze.app.data.local.dao.ScanCycleDao
import com.netraze.app.data.local.dao.SpatialPositionDao
import com.netraze.app.data.local.dao.SurveyDao
import com.netraze.app.data.local.dao.WifiObservationDao
import com.netraze.app.data.local.entity.ScanCycleEntity
import com.netraze.app.data.local.entity.SpatialPositionEntity
import com.netraze.app.data.local.entity.SurveyEntity
import com.netraze.app.data.local.entity.WifiObservationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PositionWithObservations(
    val position: SpatialPositionEntity,
    val cycles: List<ScanCycleEntity>,
    val observations: List<WifiObservationEntity>
)

data class SurveyCanvasUiState(
    val survey: SurveyEntity? = null,
    val positions: List<PositionWithObservations> = emptyList(),
    val totalObservationsCount: Int = 0,
    val uniqueBssidCount: Int = 0,
    val maxRssi: Int? = null,
    val avgRssi: Double? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SurveyCanvasViewModel @Inject constructor(
    private val surveyDao: SurveyDao,
    private val spatialPositionDao: SpatialPositionDao,
    private val scanCycleDao: ScanCycleDao,
    private val wifiObservationDao: WifiObservationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SurveyCanvasUiState())
    val uiState: StateFlow<SurveyCanvasUiState> = _uiState.asStateFlow()

    fun loadSurveyCanvasData(surveyId: UUID) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val survey = surveyDao.getSurveyById(surveyId)
                val rawPositions = spatialPositionDao.getSpatialPositionsForSurvey(surveyId)

                val posWithObs = rawPositions.map { pos ->
                    val cycles = scanCycleDao.getScanCyclesForPosition(pos.id)
                    val obsList = cycles.flatMap { cycle ->
                        wifiObservationDao.getObservationsForCycle(cycle.id)
                    }
                    PositionWithObservations(pos, cycles, obsList)
                }

                val allObs = posWithObs.flatMap { it.observations }
                val totalObs = allObs.size
                val uniqueBssids = allObs.map { it.bssid }.distinct().size
                val maxRssiVal = allObs.maxOfOrNull { it.rssiDbm }
                val avgRssiVal = if (allObs.isNotEmpty()) allObs.map { it.rssiDbm }.average() else null

                _uiState.value = SurveyCanvasUiState(
                    survey = survey,
                    positions = posWithObs,
                    totalObservationsCount = totalObs,
                    uniqueBssidCount = uniqueBssids,
                    maxRssi = maxRssiVal,
                    avgRssi = avgRssiVal,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load canvas data"
                )
            }
        }
    }
}
