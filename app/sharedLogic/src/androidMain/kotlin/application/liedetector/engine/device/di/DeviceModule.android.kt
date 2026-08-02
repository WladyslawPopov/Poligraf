package application.liedetector.engine.device.di

import application.liedetector.engine.device.AndroidDeviceInfoProvider
import application.liedetector.engine.device.DeviceInfoProvider
import org.koin.dsl.module

actual val devicePlatformModule = module {
    single<DeviceInfoProvider> { AndroidDeviceInfoProvider(get()) }
}
