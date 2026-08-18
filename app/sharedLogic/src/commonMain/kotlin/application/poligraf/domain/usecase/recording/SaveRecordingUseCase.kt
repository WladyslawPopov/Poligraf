package application.poligraf.domain.usecase.recording

import application.poligraf.data.recording.Recording
import application.poligraf.data.recording.RecordingsRepository

class SaveRecordingUseCase(
    private val repository: RecordingsRepository
) {
    suspend operator fun invoke(subjectId: String, recording: Recording): Recording {
        return repository.saveRecording(subjectId, recording)
    }
}
