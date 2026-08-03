package application.liedetector.engine.database.di

import application.liedetector.database.LieDetectorDatabase
import application.liedetector.engine.database.CacheRepository
import application.liedetector.engine.database.common.DriverFactory
import application.liedetector.engine.database.internal.CacheRepositoryImpl
import org.koin.dsl.module

val databaseModule = module {
    single {
        val driver = get<DriverFactory>().createDriver()
        LieDetectorDatabase(driver)
    }
    single<CacheRepository> { CacheRepositoryImpl(get()) }
}
