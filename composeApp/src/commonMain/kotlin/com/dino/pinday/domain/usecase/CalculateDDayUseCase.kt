package com.dino.pinday.domain.usecase

import com.dino.pinday.domain.model.Anniversary
import com.dino.pinday.domain.model.CountingType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

class CalculateDDayUseCase {

    fun calculate(anniversary: Anniversary, today: LocalDate): Int {
        return when (anniversary.countingType) {
            CountingType.D_PLUS -> calculateDPlus(anniversary, today)
            CountingType.D_MINUS -> calculateDMinus(anniversary, today)
        }
    }

    fun getNextOccurrence(anniversary: Anniversary, today: LocalDate): LocalDate {
        if (anniversary.countingType == CountingType.D_PLUS) return anniversary.date
        if (!anniversary.isRecurring) return anniversary.date

        return if (anniversary.isLunar && anniversary.lunarMonth != null && anniversary.lunarDay != null) {
            LunarSolarConverter.getNextSolarDate(
                anniversary.lunarMonth,
                anniversary.lunarDay,
                anniversary.isLeapMonth,
                today,
            )
        } else {
            getNextSolarOccurrence(anniversary.date, today)
        }
    }

    private fun calculateDPlus(anniversary: Anniversary, today: LocalDate): Int {
        return anniversary.date.daysUntil(today) + 1
    }

    private fun calculateDMinus(anniversary: Anniversary, today: LocalDate): Int {
        val nextDate = getNextOccurrence(anniversary, today)
        return today.daysUntil(nextDate)
    }

    private fun getNextSolarOccurrence(originalDate: LocalDate, today: LocalDate): LocalDate {
        val thisYear = try {
            LocalDate(today.year, originalDate.monthNumber, originalDate.dayOfMonth)
        } catch (_: Exception) {
            // Handle Feb 29 in non-leap years
            LocalDate(today.year, originalDate.monthNumber, 28)
        }
        return if (thisYear >= today) {
            thisYear
        } else {
            try {
                LocalDate(today.year + 1, originalDate.monthNumber, originalDate.dayOfMonth)
            } catch (_: Exception) {
                LocalDate(today.year + 1, originalDate.monthNumber, 28)
            }
        }
    }
}
