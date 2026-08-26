package application.poligraf.engine.device.common

import application.poligraf.engine.device.IosPermissionManager
import application.poligraf.engine.device.PermissionManager

actual fun getPermissionManager(): PermissionManager = IosPermissionManager()
