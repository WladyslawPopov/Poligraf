package application.poligraf.domain.usecase.recording

import application.poligraf.data.recording.RecordingsRepository

class DeleteRecordingUseCase(
    private val repository: RecordingsRepository
) {
    suspend operator fun invoke(subjectId: String, recordingId: String){
        return repository.deleteRecording(subjectId, recordingId)
    }
}
