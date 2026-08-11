package application.liedetector.domain.usecase.recording

import application.liedetector.data.recording.RecordingsRepository
import application.liedetector.models.KmpResult

class DeleteRecordingUseCase(
    private val repository: RecordingsRepository
) {
    suspend operator fun invoke(subjectId: String, recordingId: String): KmpResult<Unit> {
        return repository.deleteRecording(subjectId, recordingId)
    }
}
