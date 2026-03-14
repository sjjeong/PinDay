package com.dino.pinday.ui.add

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.dino.pinday.domain.model.Category
import com.dino.pinday.domain.model.CountingType
import com.dino.pinday.domain.usecase.LunarSolarConverter
import com.dino.pinday.util.toKoreanString
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditScreen(
    anniversaryId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditViewModel = koinViewModel { parametersOf(anniversaryId) },
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "기념일 수정" else "기념일 추가") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    if (uiState.isEditMode) {
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Text("삭제", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(
                        onClick = { viewModel.save(onSaved) },
                        enabled = uiState.title.isNotBlank() && !uiState.isSaving,
                    ) {
                        Text("저장")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("기념일 이름") },
                placeholder = { Text("예: 엄마 생신") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // 날짜 유형
            Text("날짜 유형", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !uiState.isLunar,
                    onClick = { viewModel.updateIsLunar(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) {
                    Text("양력")
                }
                SegmentedButton(
                    selected = uiState.isLunar,
                    onClick = { viewModel.updateIsLunar(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) {
                    Text("음력")
                }
            }

            // 날짜 선택
            if (uiState.isLunar) {
                LunarDateSelector(
                    month = uiState.lunarMonth,
                    day = uiState.lunarDay,
                    isLeapMonth = uiState.isLeapMonth,
                    onMonthChange = viewModel::updateLunarMonth,
                    onDayChange = viewModel::updateLunarDay,
                    onLeapMonthChange = viewModel::updateIsLeapMonth,
                )
                uiState.convertedSolarDate?.let { converted ->
                    Text(
                        text = "양력 변환: ${converted.toKoreanString()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                TextButton(onClick = { showDatePicker = true }) {
                    Text("날짜: ${uiState.solarDate.toKoreanString()}")
                }
            }

            // 카운팅 방식
            Text("카운팅 방식", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = uiState.countingType == CountingType.D_MINUS,
                    onClick = { viewModel.updateCountingType(CountingType.D_MINUS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) {
                    Text("D- 다가오는 날")
                }
                SegmentedButton(
                    selected = uiState.countingType == CountingType.D_PLUS,
                    onClick = { viewModel.updateCountingType(CountingType.D_PLUS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) {
                    Text("D+ 경과일")
                }
            }

            // 매년 반복
            if (uiState.countingType == CountingType.D_MINUS) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("매년 반복")
                    Switch(
                        checked = uiState.isRecurring,
                        onCheckedChange = viewModel::updateIsRecurring,
                    )
                }
            }

            // 카테고리
            Text("카테고리", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Category.entries.forEach { category ->
                    FilterChip(
                        selected = uiState.category == category,
                        onClick = { viewModel.updateCategory(category) },
                        label = { Text(category.displayName) },
                    )
                }
            }

            // 메모
            OutlinedTextField(
                value = uiState.memo,
                onValueChange = viewModel::updateMemo,
                label = { Text("메모 (선택)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.solarDate
                .atStartOfDayIn(TimeZone.UTC)
                .toEpochMilliseconds(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val ld = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        viewModel.updateSolarDate(ld)
                    }
                    showDatePicker = false
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("삭제") },
            text = { Text("이 기념일을 삭제할까요?") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(onBack) }) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
private fun LunarDateSelector(
    month: Int,
    day: Int,
    isLeapMonth: Boolean,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    onLeapMonthChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = month.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { m ->
                        if (m in 1..12) onMonthChange(m)
                    }
                },
                label = { Text("월") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            OutlinedTextField(
                value = day.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { d ->
                        if (d in 1..30) onDayChange(d)
                    }
                },
                label = { Text("일") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("윤달")
            Switch(
                checked = isLeapMonth,
                onCheckedChange = onLeapMonthChange,
            )
        }
    }
}
