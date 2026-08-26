package application.poligraf.engine.device.common

import application.poligraf.engine.device.AndroidPermissionManager
import application.poligraf.engine.device.PermissionManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun getPermissionManager(): PermissionManager = AndroidPermissionManager(object : KoinComponent {}.get())
