package com.dino.pinday.util

import kotlinx.datetime.LocalDate

fun LocalDate.toKoreanString(): String {
    return "${year}년 ${monthNumber}월 ${dayOfMonth}일"
}

fun LocalDate.toShortKoreanString(): String {
    return "${monthNumber}월 ${dayOfMonth}일"
}

fun Int.toDDayString(): String {
    return when {
        this > 0 -> "D-${this}"
        this == 0 -> "D-Day"
        else -> "D+${-this}"
    }
}

fun Int.toDPlusString(): String {
    return "D+${this}"
}
