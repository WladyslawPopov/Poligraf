package application.poligraf.di

import application.poligraf.data.di.dataModule
import application.poligraf.engine.database.di.databaseModule
import application.poligraf.engine.device.di.devicePlatformModule
import application.poligraf.engine.network.di.networkModule
import application.poligraf.engine.settings.di.settingsModule
import application.poligraf.domain.usecase.recording.di.recordingUseCaseModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    platformModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {}
): KoinApplication {
    return startKoin {
        appDeclaration()
        modules(sharedModules)
        modules(platformModules)
    }
}

val sharedModules = listOf(
    dataModule,
    networkModule,
    databaseModule,
    settingsModule,
    devicePlatformModule,
    recordingUseCaseModule
)
