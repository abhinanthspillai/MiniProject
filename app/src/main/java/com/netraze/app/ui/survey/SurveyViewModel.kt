package com.netraze.app.ui.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netraze.app.data.local.entity.SurveyEntity
import com.netraze.app.data.repository.SurveyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SurveysUiState(
    val surveys: List<SurveyEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private var surveyRepository: SurveyRepository?
) : ViewModel() {

    fun setRepository(repo: SurveyRepository) {
        this.surveyRepository = repo
    }

    private val _uiState = MutableStateFlow(SurveysUiState())
    val uiState: StateFlow<SurveysUiState> = _uiState.asStateFlow()

    fun loadSurveys(surveyAreaId: UUID) {
        val repo = surveyRepository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repo.getSurveysForArea(surveyAreaId)
            if (result.isSuccess) {
                _uiState.value = SurveysUiState(surveys = result.getOrDefault(emptyList()), isLoading = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load surveys"
                )
            }
        }
    }

    fun createSurvey(
        surveyAreaId: UUID,
        title: String,
        mode: String,
        floorPlanId: UUID? = null,
        simpleMapId: UUID? = null,
        onSuccess: (SurveyEntity) -> Unit
    ) {
        val repo = surveyRepository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repo.createSurvey(surveyAreaId, title, mode, floorPlanId, simpleMapId)
            if (result.isSuccess) {
                val survey = result.getOrThrow()
                loadSurveys(surveyAreaId)
                onSuccess(survey)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to create survey"
                )
            }
        }
    }
}
