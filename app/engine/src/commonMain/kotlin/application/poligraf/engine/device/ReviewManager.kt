package application.poligraf.engine.device

interface ReviewManager {
    suspend fun requestReview(): Boolean
}
