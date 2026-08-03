package application.liedetector.engine.device.di

import application.liedetector.engine.device.DeviceInfoProvider
import application.liedetector.engine.device.AndroidDeviceInfoProvider
import org.koin.dsl.module

actual val devicePlatformModule = module {
    single<DeviceInfoProvider> { AndroidDeviceInfoProvider(get()) }
}
