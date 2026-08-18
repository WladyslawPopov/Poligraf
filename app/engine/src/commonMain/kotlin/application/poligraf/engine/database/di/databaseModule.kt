package application.poligraf.engine.database.di

import application.poligraf.database.PoligrafDatabase
import application.poligraf.engine.database.CacheRepository
import application.poligraf.engine.database.common.DriverFactory
import application.poligraf.engine.database.internal.CacheRepositoryImpl
import org.koin.dsl.module

val databaseModule = module {
    single {
        val driver = get<DriverFactory>().createDriver()
        PoligrafDatabase(driver)
    }
    single<CacheRepository> { CacheRepositoryImpl(get()) }
}
