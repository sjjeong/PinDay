package com.dino.pinday

import com.dino.pinday.domain.model.Anniversary
import com.dino.pinday.domain.model.Category
import com.dino.pinday.domain.model.CountingType
import com.dino.pinday.domain.usecase.CalculateDDayUseCase
import com.dino.pinday.domain.usecase.GetMilestonesUseCase
import com.dino.pinday.domain.usecase.LunarSolarConverter
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LunarSolarConverterTest {

    @Test
    fun lunarNewYear2026() {
        // 2026 음력 1월 1일 = 2026년 2월 17일
        val result = LunarSolarConverter.lunarToSolar(2026, 1, 1)
        assertEquals(LocalDate(2026, 2, 17), result)
    }

    @Test
    fun chuseok2025() {
        // 2025 음력 8월 15일 (추석) = 2025년 10월 6일
        val result = LunarSolarConverter.lunarToSolar(2025, 8, 15)
        assertEquals(LocalDate(2025, 10, 6), result)
    }

    @Test
    fun solarToLunarRoundTrip() {
        val solarDate = LocalDate(2026, 2, 17)
        val lunar = LunarSolarConverter.solarToLunar(solarDate)
        assertEquals(2026, lunar.year)
        assertEquals(1, lunar.month)
        assertEquals(1, lunar.day)
    }

    @Test
    fun lunarToSolarForYearReturnsNull_whenInvalidDay() {
        val result = LunarSolarConverter.lunarToSolarForYear(2026, 1, 31)
        // 음력 월은 최대 30일이므로 null 반환 가능
        // (29일 or 30일 월에 31일 요청)
        assertEquals(null, result)
    }
}

class CalculateDDayUseCaseTest {

    private val useCase = CalculateDDayUseCase()

    @Test
    fun dMinusSimple() {
        val today = LocalDate(2026, 3, 1)
        val anniversary = createAnniversary(
            date = LocalDate(2026, 3, 15),
            countingType = CountingType.D_MINUS,
            isRecurring = false,
        )
        assertEquals(14, useCase.calculate(anniversary, today))
    }

    @Test
    fun dMinusDDay() {
        val today = LocalDate(2026, 3, 15)
        val anniversary = createAnniversary(
            date = LocalDate(2026, 3, 15),
            countingType = CountingType.D_MINUS,
            isRecurring = false,
        )
        assertEquals(0, useCase.calculate(anniversary, today))
    }

    @Test
    fun dPlusCount() {
        val today = LocalDate(2026, 3, 10)
        val anniversary = createAnniversary(
            date = LocalDate(2026, 3, 1),
            countingType = CountingType.D_PLUS,
        )
        // D+10 (3/1이 1일째)
        assertEquals(10, useCase.calculate(anniversary, today))
    }

    @Test
    fun dMinusRecurringSolar_pastThisYear() {
        val today = LocalDate(2026, 4, 1)
        val anniversary = createAnniversary(
            date = LocalDate(2020, 3, 15),
            countingType = CountingType.D_MINUS,
            isRecurring = true,
        )
        // 2026-03-15 이미 지남 → 2027-03-15까지 남은 일수
        val result = useCase.calculate(anniversary, today)
        assertEquals(348, result) // 2026-04-01 → 2027-03-15
    }

    private fun createAnniversary(
        date: LocalDate,
        countingType: CountingType,
        isRecurring: Boolean = false,
    ) = Anniversary(
        id = 1,
        title = "Test",
        date = date,
        countingType = countingType,
        isRecurring = isRecurring,
        category = Category.OTHER,
        createdAt = date,
        updatedAt = date,
    )
}

class GetMilestonesUseCaseTest {

    private val useCase = GetMilestonesUseCase()

    @Test
    fun milestonesForDPlus() {
        val anniversary = Anniversary(
            id = 1,
            title = "Test",
            date = LocalDate(2026, 1, 1),
            countingType = CountingType.D_PLUS,
            category = Category.ANNIVERSARY,
            createdAt = LocalDate(2026, 1, 1),
            updatedAt = LocalDate(2026, 1, 1),
        )
        val today = LocalDate(2026, 3, 1)
        val milestones = useCase.getMilestones(anniversary, today)
        assertTrue(milestones.isNotEmpty())
        assertEquals(100, milestones.first().days)
    }

    @Test
    fun noMilestonesForDMinus() {
        val anniversary = Anniversary(
            id = 1,
            title = "Test",
            date = LocalDate(2026, 6, 1),
            countingType = CountingType.D_MINUS,
            category = Category.BIRTHDAY,
            createdAt = LocalDate(2026, 1, 1),
            updatedAt = LocalDate(2026, 1, 1),
        )
        val milestones = useCase.getMilestones(anniversary, LocalDate(2026, 3, 1))
        assertTrue(milestones.isEmpty())
    }
}
