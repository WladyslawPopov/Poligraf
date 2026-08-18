package application.poligraf.engine.database.common

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import application.poligraf.database.PoligrafDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(PoligrafDatabase.Schema, DATABASE_NAME)
    }
}
