package application.liedetector.engine.device

import application.liedetector.models.KmpResult

interface DeviceIntegrity {
    suspend fun checkIntegrity(): KmpResult<Unit>
}
