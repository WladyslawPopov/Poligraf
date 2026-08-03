package application.liedetector.engine.database.common

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import application.liedetector.database.LieDetectorDatabase

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(LieDetectorDatabase.Schema, context, DATABASE_NAME)
    }
}
