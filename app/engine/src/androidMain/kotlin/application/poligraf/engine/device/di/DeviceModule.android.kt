package application.poligraf.engine.device.di

import application.poligraf.engine.device.DeviceInfoProvider
import application.poligraf.engine.device.AndroidDeviceInfoProvider
import org.koin.dsl.module

actual val devicePlatformModule = module {
    single<DeviceInfoProvider> { AndroidDeviceInfoProvider(get()) }
}
