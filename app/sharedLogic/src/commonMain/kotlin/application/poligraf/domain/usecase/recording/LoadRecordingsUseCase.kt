package application.poligraf.domain.usecase.recording

import application.poligraf.data.recording.RecordingsRepository

class LoadRecordingsUseCase(
    private val repository: RecordingsRepository
) {
    suspend operator fun invoke(subjectId: String) {
        repository.loadRecordings(subjectId)
    }
}
