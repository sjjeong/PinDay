package com.dino.pinday.data.db

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = NativeSqliteDriver(PinDayDatabase.Schema, "pinday.db")
        driver.execute(null, "CREATE TABLE IF NOT EXISTS SettingsEntity (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL)", 0)
        return driver
    }
}
