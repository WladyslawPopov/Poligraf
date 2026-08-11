package application.liedetector.domain.usecase.recording

import application.liedetector.data.recording.RecordingsRepository

class LoadRecordingsUseCase(
    private val repository: RecordingsRepository
) {
    suspend operator fun invoke(subjectId: String) {
        repository.loadRecordings(subjectId)
    }
}
