package com.dino.pinday.ui.onboarding

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
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dino.pinday.domain.model.OnboardingSuggestion
import com.dino.pinday.util.toKoreanString
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var datePickerTarget by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            text = "PinDay",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "어떤 기념일을 추가해볼까요?",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "나중에 추가할 수도 있어요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))

        uiState.groups.forEach { (groupName, suggestions) ->
            Text(
                text = groupName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                suggestions.forEach { suggestion ->
                    val isSelected = uiState.selected.containsKey(suggestion.title)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleSuggestion(suggestion) },
                        label = { Text(suggestion.title) },
                    )
                }
            }

            // 선택된 항목의 날짜 입력
            suggestions.forEach { suggestion ->
                val sel = uiState.selected[suggestion.title] ?: return@forEach
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                ) {
                    Text(
                        text = "${suggestion.title} 날짜",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    if (suggestion.isLunar) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = sel.lunarMonth?.toString() ?: "",
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { m ->
                                        if (m in 1..12) viewModel.updateLunarDate(suggestion.title, m, sel.lunarDay ?: 1)
                                    }
                                },
                                label = { Text("음력 월") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = sel.lunarDay?.toString() ?: "",
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { d ->
                                        if (d in 1..30) viewModel.updateLunarDate(suggestion.title, sel.lunarMonth ?: 1, d)
                                    }
                                },
                                label = { Text("음력 일") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                        }
                    } else {
                        TextButton(onClick = { datePickerTarget = suggestion.title }) {
                            Text(
                                sel.solarDate?.toKoreanString() ?: "날짜를 선택하세요",
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { viewModel.save(onComplete) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
        ) {
            Text(if (uiState.selected.isEmpty()) "건너뛰기" else "시작하기")
        }
        Spacer(Modifier.height(8.dp))
        if (uiState.selected.isNotEmpty()) {
            OutlinedButton(
                onClick = { viewModel.skip(onComplete) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("건너뛰기")
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    datePickerTarget?.let { title ->
        val sel = uiState.selected[title]
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = sel?.solarDate
                ?.atStartOfDayIn(TimeZone.UTC)
                ?.toEpochMilliseconds(),
        )
        DatePickerDialog(
            onDismissRequest = { datePickerTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val ld = instant.toLocalDateTime(TimeZone.UTC).date
                        viewModel.updateDate(title, ld)
                    }
                    datePickerTarget = null
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { datePickerTarget = null }) {
                    Text("취소")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

