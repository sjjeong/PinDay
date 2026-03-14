package com.dino.pinday

import androidx.compose.ui.window.ComposeUIViewController
import com.dino.pinday.di.appModule
import com.dino.pinday.di.platformModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(appModule, platformModule)
    }
}

fun MainViewController() = ComposeUIViewController { App() }
