package application.poligraf.domain.usecase.recording

import application.poligraf.data.recording.Recording
import application.poligraf.data.recording.RecordingsRepository
import application.poligraf.models.KmpResult

class SaveRecordingUseCase(
    private val repository: RecordingsRepository
) {
    suspend operator fun invoke(subjectId: String, recording: Recording): KmpResult<Recording> {
        return repository.saveRecording(subjectId, recording)
    }
}
