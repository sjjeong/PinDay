package com.dino.pinday.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.pinday.data.repository.AnniversaryRepository
import com.dino.pinday.domain.model.Anniversary
import com.dino.pinday.domain.model.CountingType
import com.dino.pinday.domain.usecase.CalculateDDayUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class AnniversaryItem(
    val anniversary: Anniversary,
    val dDay: Int,
    val nextDate: LocalDate,
)

data class HomeUiState(
    val items: List<AnniversaryItem> = emptyList(),
    val isLoading: Boolean = true,
)

class HomeViewModel(
    private val repository: AnniversaryRepository,
    private val calculateDDay: CalculateDDayUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { anniversaries ->
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val items = anniversaries.map { anniversary ->
                    AnniversaryItem(
                        anniversary = anniversary,
                        dDay = calculateDDay.calculate(anniversary, today),
                        nextDate = calculateDDay.getNextOccurrence(anniversary, today),
                    )
                }.sortedWith(
                    compareBy<AnniversaryItem> { it.anniversary.countingType == CountingType.D_PLUS }
                        .thenBy {
                            if (it.anniversary.countingType == CountingType.D_MINUS) it.dDay
                            else -it.dDay
                        },
                )
                _uiState.value = HomeUiState(items = items, isLoading = false)
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
