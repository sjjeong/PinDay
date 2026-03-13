package com.dino.pinday

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform