package com.dino.pinday.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dino.pinday.data.repository.AnniversaryRepository
import com.dino.pinday.domain.model.Anniversary
import com.dino.pinday.domain.model.Category
import com.dino.pinday.domain.model.CountingType
import com.dino.pinday.domain.usecase.LunarSolarConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class AddEditUiState(
    val title: String = "",
    val isLunar: Boolean = false,
    val solarDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val lunarMonth: Int = 1,
    val lunarDay: Int = 1,
    val isLeapMonth: Boolean = false,
    val countingType: CountingType = CountingType.D_MINUS,
    val isRecurring: Boolean = true,
    val category: Category = Category.OTHER,
    val memo: String = "",
    val isEditMode: Boolean = false,
    val convertedSolarDate: LocalDate? = null,
    val isSaving: Boolean = false,
)

class AddEditViewModel(
    private val anniversaryId: Long?,
    private val repository: AnniversaryRepository,
    private val converter: LunarSolarConverter,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        if (anniversaryId != null) {
            viewModelScope.launch {
                val anniversary = repository.getById(anniversaryId) ?: return@launch
                _uiState.value = AddEditUiState(
                    title = anniversary.title,
                    isLunar = anniversary.isLunar,
                    solarDate = anniversary.date,
                    lunarMonth = anniversary.lunarMonth ?: 1,
                    lunarDay = anniversary.lunarDay ?: 1,
                    isLeapMonth = anniversary.isLeapMonth,
                    countingType = anniversary.countingType,
                    isRecurring = anniversary.isRecurring,
                    category = anniversary.category,
                    memo = anniversary.memo ?: "",
                    isEditMode = true,
                )
                if (anniversary.isLunar) {
                    updateConvertedDate()
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateIsLunar(isLunar: Boolean) {
        _uiState.update { it.copy(isLunar = isLunar) }
        if (isLunar) updateConvertedDate()
    }

    fun updateSolarDate(date: LocalDate) {
        _uiState.update { it.copy(solarDate = date) }
    }

    fun updateLunarMonth(month: Int) {
        _uiState.update { it.copy(lunarMonth = month) }
        updateConvertedDate()
    }

    fun updateLunarDay(day: Int) {
        _uiState.update { it.copy(lunarDay = day) }
        updateConvertedDate()
    }

    fun updateIsLeapMonth(isLeap: Boolean) {
        _uiState.update { it.copy(isLeapMonth = isLeap) }
        updateConvertedDate()
    }

    fun updateCountingType(type: CountingType) {
        _uiState.update {
            it.copy(
                countingType = type,
                isRecurring = if (type == CountingType.D_PLUS) false else it.isRecurring,
            )
        }
    }

    fun updateIsRecurring(recurring: Boolean) {
        _uiState.update { it.copy(isRecurring = recurring) }
    }

    fun updateCategory(category: Category) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateMemo(memo: String) {
        _uiState.update { it.copy(memo = memo) }
    }

    private fun updateConvertedDate() {
        val state = _uiState.value
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val converted = try {
            converter.lunarToSolarForYear(today.year, state.lunarMonth, state.lunarDay, state.isLeapMonth)
        } catch (_: Exception) {
            null
        }
        _uiState.update { it.copy(convertedSolarDate = converted) }
    }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val date = if (state.isLunar) {
                try {
                    converter.lunarToSolar(today.year, state.lunarMonth, state.lunarDay, state.isLeapMonth)
                } catch (_: Exception) {
                    state.solarDate
                }
            } else {
                state.solarDate
            }

            val anniversary = Anniversary(
                id = anniversaryId ?: 0,
                title = state.title,
                date = date,
                isLunar = state.isLunar,
                lunarMonth = if (state.isLunar) state.lunarMonth else null,
                lunarDay = if (state.isLunar) state.lunarDay else null,
                isLeapMonth = state.isLeapMonth,
                countingType = state.countingType,
                isRecurring = state.isRecurring,
                category = state.category,
                memo = state.memo.ifBlank { null },
                createdAt = today,
                updatedAt = today,
            )

            if (state.isEditMode && anniversaryId != null) {
                repository.update(anniversary)
            } else {
                repository.insert(anniversary)
            }
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }

    fun delete(onSuccess: () -> Unit) {
        if (anniversaryId == null) return
        viewModelScope.launch {
            repository.delete(anniversaryId)
            onSuccess()
        }
    }
}
