package application.liedetector.domain.usecase.recording

import application.liedetector.data.recording.Recording
import application.liedetector.data.recording.RecordingsRepository
import kotlinx.coroutines.flow.Flow

class GetRecordingsUseCase(
    private val repository: RecordingsRepository
) {
    operator fun invoke(subjectId: String): Flow<List<Recording>> {
        return repository.getRecordings(subjectId)
    }
}
