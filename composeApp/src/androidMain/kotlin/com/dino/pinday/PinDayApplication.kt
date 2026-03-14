package com.dino.pinday

import android.app.Application
import com.dino.pinday.di.appModule
import com.dino.pinday.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PinDayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PinDayApplication)
            modules(appModule, platformModule)
        }
    }
}
