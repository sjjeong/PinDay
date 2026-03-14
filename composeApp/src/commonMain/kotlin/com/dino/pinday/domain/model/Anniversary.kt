package com.dino.pinday.domain.model

import kotlinx.datetime.LocalDate

data class Anniversary(
    val id: Long = 0,
    val title: String,
    val date: LocalDate,
    val isLunar: Boolean = false,
    val lunarMonth: Int? = null,
    val lunarDay: Int? = null,
    val isLeapMonth: Boolean = false,
    val countingType: CountingType = CountingType.D_MINUS,
    val isRecurring: Boolean = true,
    val category: Category = Category.OTHER,
    val memo: String? = null,
    val createdAt: LocalDate,
    val updatedAt: LocalDate,
)
