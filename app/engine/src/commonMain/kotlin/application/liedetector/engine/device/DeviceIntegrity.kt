package application.liedetector.engine.device

interface DeviceIntegrity {
    suspend fun checkIntegrity(): Boolean
}
