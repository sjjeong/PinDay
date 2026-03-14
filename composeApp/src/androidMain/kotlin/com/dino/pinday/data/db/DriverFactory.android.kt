package com.dino.pinday.data.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = PinDayDatabase.Schema,
            context = context,
            name = "pinday.db",
            callback = object : AndroidSqliteDriver.Callback(PinDayDatabase.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS SettingsEntity (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL)"
                    )
                }
            },
        )
    }
}
