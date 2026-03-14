package com.dino.pinday.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.dino.pinday.domain.model.CountingType
import com.dino.pinday.util.toDDayString
import com.dino.pinday.util.toDPlusString
import com.dino.pinday.util.toKoreanString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    anniversaryId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    viewModel: DetailViewModel = koinViewModel { parametersOf(anniversaryId) },
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("상세") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    TextButton(onClick = { onEdit(anniversaryId) }) {
                        Text("수정")
                    }
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text("삭제", color = MaterialTheme.colorScheme.error)
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            val anniversary = uiState.anniversary ?: return@Scaffold
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // D-Day 헤더
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = anniversary.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(8.dp))
                            val dDayText = when (anniversary.countingType) {
                                CountingType.D_MINUS -> uiState.dDay.toDDayString()
                                CountingType.D_PLUS -> uiState.dDay.toDPlusString()
                            }
                            Text(
                                text = dDayText,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                // 정보
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoRow("카테고리", anniversary.category.displayName)
                        uiState.nextDate?.let { date ->
                            InfoRow(
                                if (anniversary.countingType == CountingType.D_PLUS) "시작일" else "다음 날짜",
                                date.toKoreanString(),
                            )
                        }
                        if (anniversary.isLunar && anniversary.lunarMonth != null && anniversary.lunarDay != null) {
                            InfoRow("음력", "${anniversary.lunarMonth}월 ${anniversary.lunarDay}일${if (anniversary.isLeapMonth) " (윤달)" else ""}")
                        }
                        if (anniversary.isRecurring) {
                            InfoRow("반복", "매년")
                        }
                        anniversary.memo?.let { memo ->
                            InfoRow("메모", memo)
                        }
                    }
                }

                // 마일스톤 (D+ only)
                if (uiState.milestones.isNotEmpty()) {
                    item {
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "다가오는 기념일",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    items(uiState.milestones) { milestone ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(milestone.label, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                milestone.date.toKoreanString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("삭제") },
            text = { Text("이 기념일을 삭제할까요?") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(onDeleted) }) {
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
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
