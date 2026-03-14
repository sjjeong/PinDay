package com.dino.pinday.domain.usecase

import com.dino.pinday.domain.model.Anniversary
import com.dino.pinday.domain.model.CountingType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class Milestone(
    val days: Int,
    val date: LocalDate,
    val label: String,
)

class GetMilestonesUseCase {

    private val milestoneValues = listOf(
        100, 200, 300, 365, 500, 1000, 1500, 2000, 2500, 3000, 5000, 10000,
    )

    fun getMilestones(anniversary: Anniversary, today: LocalDate): List<Milestone> {
        if (anniversary.countingType != CountingType.D_PLUS) return emptyList()

        val startDate = anniversary.date
        return milestoneValues.mapNotNull { days ->
            val milestoneDate = startDate.plus(days - 1, DateTimeUnit.DAY)
            if (milestoneDate >= today) {
                Milestone(
                    days = days,
                    date = milestoneDate,
                    label = "${days}일",
                )
            } else null
        }
    }
}
