package com.dino.pinday.domain.model

data class OnboardingSuggestion(
    val title: String,
    val category: Category,
    val countingType: CountingType,
    val isLunar: Boolean,
    val isRecurring: Boolean,
) {
    companion object {
        val family = listOf(
            OnboardingSuggestion("엄마 생신", Category.BIRTHDAY, CountingType.D_MINUS, isLunar = true, isRecurring = true),
            OnboardingSuggestion("아빠 생신", Category.BIRTHDAY, CountingType.D_MINUS, isLunar = true, isRecurring = true),
            OnboardingSuggestion("기일", Category.MEMORIAL, CountingType.D_MINUS, isLunar = true, isRecurring = true),
        )

        val couple = listOf(
            OnboardingSuggestion("사귄 날", Category.ANNIVERSARY, CountingType.D_PLUS, isLunar = false, isRecurring = false),
            OnboardingSuggestion("결혼기념일", Category.ANNIVERSARY, CountingType.D_MINUS, isLunar = false, isRecurring = true),
        )

        val personal = listOf(
            OnboardingSuggestion("입사일", Category.ANNIVERSARY, CountingType.D_PLUS, isLunar = false, isRecurring = false),
            OnboardingSuggestion("생일", Category.BIRTHDAY, CountingType.D_MINUS, isLunar = false, isRecurring = true),
        )

        val groups = listOf(
            "가족" to family,
            "연인" to couple,
            "나" to personal,
        )
    }
}
