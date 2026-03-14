package com.dino.pinday.domain.model

enum class Category(val displayName: String) {
    BIRTHDAY("생일"),
    ANNIVERSARY("기념일"),
    MEMORIAL("기일"),
    HOLIDAY("공휴일"),
    OTHER("기타"),
}
