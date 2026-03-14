package com.dino.pinday.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.dino.pinday.data.db.PinDayDatabase
import com.dino.pinday.data.mapper.toDomain
import com.dino.pinday.domain.model.Anniversary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AnniversaryRepository(private val database: PinDayDatabase) {

    private val queries get() = database.anniversaryQueries

    fun getAll(): Flow<List<Anniversary>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun getById(id: Long): Anniversary? = withContext(Dispatchers.IO) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    suspend fun insert(anniversary: Anniversary) = withContext(Dispatchers.IO) {
        queries.insert(
            title = anniversary.title,
            date = anniversary.date.toString(),
            is_lunar = if (anniversary.isLunar) 1L else 0L,
            lunar_month = anniversary.lunarMonth?.toLong(),
            lunar_day = anniversary.lunarDay?.toLong(),
            is_leap_month = if (anniversary.isLeapMonth) 1L else 0L,
            counting_type = anniversary.countingType.name,
            is_recurring = if (anniversary.isRecurring) 1L else 0L,
            category = anniversary.category.name,
            memo = anniversary.memo,
            created_at = anniversary.createdAt.toString(),
            updated_at = anniversary.updatedAt.toString(),
        )
    }

    suspend fun update(anniversary: Anniversary) = withContext(Dispatchers.IO) {
        queries.update(
            title = anniversary.title,
            date = anniversary.date.toString(),
            is_lunar = if (anniversary.isLunar) 1L else 0L,
            lunar_month = anniversary.lunarMonth?.toLong(),
            lunar_day = anniversary.lunarDay?.toLong(),
            is_leap_month = if (anniversary.isLeapMonth) 1L else 0L,
            counting_type = anniversary.countingType.name,
            is_recurring = if (anniversary.isRecurring) 1L else 0L,
            category = anniversary.category.name,
            memo = anniversary.memo,
            updated_at = anniversary.updatedAt.toString(),
            id = anniversary.id,
        )
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteById(id)
    }
}
