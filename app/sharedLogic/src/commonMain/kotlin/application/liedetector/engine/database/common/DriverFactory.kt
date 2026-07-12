package application.liedetector.engine.database.common

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

const val DATABASE_NAME = "LieDetectorDatabase.db"
