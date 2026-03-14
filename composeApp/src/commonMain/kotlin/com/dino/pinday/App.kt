package com.dino.pinday

import androidx.compose.runtime.Composable
import com.dino.pinday.ui.navigation.PinDayNavGraph
import com.dino.pinday.ui.theme.PinDayTheme

@Composable
fun App() {
    PinDayTheme {
        PinDayNavGraph()
    }
}
