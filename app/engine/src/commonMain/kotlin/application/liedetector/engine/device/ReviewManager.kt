package application.liedetector.engine.device

interface ReviewManager {
    suspend fun requestReview(): Boolean
}
