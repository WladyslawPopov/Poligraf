package application.poligraf.engine.database.common

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import application.poligraf.database.PoligrafDatabase

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(PoligrafDatabase.Schema, context, DATABASE_NAME)
    }
}
