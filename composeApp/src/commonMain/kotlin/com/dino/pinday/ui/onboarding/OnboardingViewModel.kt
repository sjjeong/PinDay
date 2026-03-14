package com.dino.pinday.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.pinday.data.repository.AnniversaryRepository
import com.dino.pinday.domain.model.Anniversary
import com.dino.pinday.domain.model.OnboardingSuggestion
import com.dino.pinday.domain.usecase.LunarSolarConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class SelectedSuggestion(
    val suggestion: OnboardingSuggestion,
    val solarDate: LocalDate? = null,
    val lunarMonth: Int? = null,
    val lunarDay: Int? = null,
)

data class OnboardingUiState(
    val groups: List<Pair<String, List<OnboardingSuggestion>>> = OnboardingSuggestion.groups,
    val selected: Map<String, SelectedSuggestion> = emptyMap(),
    val isSaving: Boolean = false,
)

class OnboardingViewModel(
    private val repository: AnniversaryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun toggleSuggestion(suggestion: OnboardingSuggestion) {
        _uiState.update { state ->
            val key = suggestion.title
            val newSelected = state.selected.toMutableMap()
            if (newSelected.containsKey(key)) {
                newSelected.remove(key)
            } else {
                newSelected[key] = SelectedSuggestion(suggestion)
            }
            state.copy(selected = newSelected)
        }
    }

    fun updateDate(title: String, date: LocalDate) {
        _uiState.update { state ->
            val newSelected = state.selected.toMutableMap()
            newSelected[title]?.let { sel ->
                newSelected[title] = sel.copy(solarDate = date)
            }
            state.copy(selected = newSelected)
        }
    }

    fun updateLunarDate(title: String, month: Int, day: Int) {
        _uiState.update { state ->
            val newSelected = state.selected.toMutableMap()
            newSelected[title]?.let { sel ->
                newSelected[title] = sel.copy(lunarMonth = month, lunarDay = day)
            }
            state.copy(selected = newSelected)
        }
    }

    fun save(onComplete: () -> Unit) {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            _uiState.value.selected.values.forEach { sel ->
                val suggestion = sel.suggestion
                val date = if (suggestion.isLunar && sel.lunarMonth != null && sel.lunarDay != null) {
                    try {
                        LunarSolarConverter.lunarToSolar(today.year, sel.lunarMonth, sel.lunarDay)
                    } catch (_: Exception) {
                        today
                    }
                } else {
                    sel.solarDate ?: today
                }

                val anniversary = Anniversary(
                    title = suggestion.title,
                    date = date,
                    isLunar = suggestion.isLunar,
                    lunarMonth = if (suggestion.isLunar) sel.lunarMonth else null,
                    lunarDay = if (suggestion.isLunar) sel.lunarDay else null,
                    countingType = suggestion.countingType,
                    isRecurring = suggestion.isRecurring,
                    category = suggestion.category,
                    createdAt = today,
                    updatedAt = today,
                )
                repository.insert(anniversary)
            }
            _uiState.update { it.copy(isSaving = false) }
            onComplete()
        }
    }
}
