package com.dino.pinday

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dino.pinday.data.preferences.AppPreferences
import com.dino.pinday.ui.navigation.PinDayNavGraph
import com.dino.pinday.ui.theme.PinDayTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    PinDayTheme {
        val appPreferences = koinInject<AppPreferences>()
        val startOnboarding = remember { !appPreferences.isOnboardingComplete() }
        PinDayNavGraph(startOnboarding = startOnboarding)
    }
}
