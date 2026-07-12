package application.liedetector.engine.database.common

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import application.liedetector.database.LieDetectorDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(LieDetectorDatabase.Schema, DATABASE_NAME)
    }
}
