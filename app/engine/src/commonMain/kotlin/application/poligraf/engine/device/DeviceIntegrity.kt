package application.poligraf.engine.device

interface DeviceIntegrity {
    suspend fun checkIntegrity(): Boolean
}
