package application.liedetector.di

import application.liedetector.data.di.dataModule
import application.liedetector.engine.database.di.databaseModule
import application.liedetector.engine.device.di.devicePlatformModule
import application.liedetector.engine.network.di.networkModule
import application.liedetector.engine.settings.di.settingsModule
import application.liedetector.domain.usecase.recording.di.recordingUseCaseModule
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
