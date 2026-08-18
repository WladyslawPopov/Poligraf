package application.poligraf.domain.usecase.recording

import application.poligraf.data.recording.RecordingsRepository
import application.poligraf.models.KmpResult

class DeleteRecordingUseCase(
    private val repository: RecordingsRepository
) {
    suspend operator fun invoke(subjectId: String, recordingId: String): KmpResult<Unit> {
        return repository.deleteRecording(subjectId, recordingId)
    }
}
