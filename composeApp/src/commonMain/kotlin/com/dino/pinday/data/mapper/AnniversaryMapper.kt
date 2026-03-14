package com.dino.pinday.data.mapper

import com.dino.pinday.data.db.AnniversaryEntity
import com.dino.pinday.domain.model.Anniversary
import com.dino.pinday.domain.model.Category
import com.dino.pinday.domain.model.CountingType
import kotlinx.datetime.LocalDate

fun AnniversaryEntity.toDomain(): Anniversary {
    return Anniversary(
        id = id,
        title = title,
        date = LocalDate.parse(date),
        isLunar = is_lunar != 0L,
        lunarMonth = lunar_month?.toInt(),
        lunarDay = lunar_day?.toInt(),
        isLeapMonth = is_leap_month != 0L,
        countingType = CountingType.valueOf(counting_type),
        isRecurring = is_recurring != 0L,
        category = Category.valueOf(category),
        memo = memo,
        createdAt = LocalDate.parse(created_at),
        updatedAt = LocalDate.parse(updated_at),
    )
}
