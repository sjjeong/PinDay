package com.dino.pinday.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.pinday.data.repository.AnniversaryRepository
import com.dino.pinday.domain.model.Anniversary
import com.dino.pinday.domain.model.CountingType
import com.dino.pinday.domain.usecase.CalculateDDayUseCase
import com.dino.pinday.domain.usecase.GetMilestonesUseCase
import com.dino.pinday.domain.usecase.Milestone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class DetailUiState(
    val anniversary: Anniversary? = null,
    val dDay: Int = 0,
    val nextDate: LocalDate? = null,
    val milestones: List<Milestone> = emptyList(),
    val isLoading: Boolean = true,
)

class DetailViewModel(
    private val anniversaryId: Long,
    private val repository: AnniversaryRepository,
    private val calculateDDay: CalculateDDayUseCase,
    private val getMilestones: GetMilestonesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val anniversary = repository.getById(anniversaryId) ?: return@launch
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val dDay = calculateDDay.calculate(anniversary, today)
            val nextDate = calculateDDay.getNextOccurrence(anniversary, today)
            val milestones = getMilestones.getMilestones(anniversary, today)

            _uiState.value = DetailUiState(
                anniversary = anniversary,
                dDay = dDay,
                nextDate = nextDate,
                milestones = milestones,
                isLoading = false,
            )
        }
    }

    fun delete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.delete(anniversaryId)
            onSuccess()
        }
    }
}
