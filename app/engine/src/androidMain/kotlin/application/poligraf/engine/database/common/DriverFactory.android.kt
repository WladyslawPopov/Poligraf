package application.poligraf.engine.database.common

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import application.poligraf.database.PoligrafDatabase
import org.koin.mp.KoinPlatform.getKoin

actual fun createDriver(): SqlDriver {
    val context : Context = getKoin().get()
    return AndroidSqliteDriver(PoligrafDatabase.Schema, context, DATABASE_NAME)
}
