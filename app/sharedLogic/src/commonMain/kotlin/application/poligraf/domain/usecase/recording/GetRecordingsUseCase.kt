package application.poligraf.domain.usecase.recording

import application.poligraf.data.recording.Recording
import application.poligraf.data.recording.RecordingsRepository
import kotlinx.coroutines.flow.Flow

class GetRecordingsUseCase(
    private val repository: RecordingsRepository
) {
    operator fun invoke(subjectId: String): Flow<List<Recording>> {
        return repository.getRecordings(subjectId)
    }
}
