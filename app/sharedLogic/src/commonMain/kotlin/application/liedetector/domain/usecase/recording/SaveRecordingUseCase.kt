package application.liedetector.domain.usecase.recording

import application.liedetector.data.recording.Recording
import application.liedetector.data.recording.RecordingsRepository
import application.liedetector.models.KmpResult

class SaveRecordingUseCase(
    private val repository: RecordingsRepository
) {
    suspend operator fun invoke(subjectId: String, recording: Recording): KmpResult<Recording> {
        return repository.saveRecording(subjectId, recording)
    }
}
