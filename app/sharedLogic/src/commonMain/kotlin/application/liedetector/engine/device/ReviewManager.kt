package application.liedetector.engine.device

import application.liedetector.models.KmpResult

interface ReviewManager {
    suspend fun requestReview(): KmpResult<Unit>
}
