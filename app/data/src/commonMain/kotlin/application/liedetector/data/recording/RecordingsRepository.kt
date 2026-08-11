package application.liedetector.data.recording

import application.liedetector.engine.io.FileSystem
import application.liedetector.engine.utils.nowAsEpochSeconds
import application.liedetector.models.KmpResult
import application.liedetector.engine.error.toAppException
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

interface RecordingsRepository {
    fun getRecordings(subjectId: String): Flow<List<Recording>>
    suspend fun saveRecording(subjectId: String, recording: Recording): KmpResult<Recording>
    suspend fun deleteRecording(subjectId: String, recordingId: String): KmpResult<Unit>
    suspend fun loadRecordings(subjectId: String)
}

class RecordingsRepositoryImpl(
    private val fileSystem: FileSystem
) : RecordingsRepository {

    private val _recordings = MutableStateFlow<Map<String, List<Recording>>>(emptyMap())
    private val json = Json { ignoreUnknownKeys = true }

    override fun getRecordings(subjectId: String): Flow<List<Recording>> {
        return _recordings.map { map -> 
            val list = map[subjectId] ?: emptyList()
            list.map { recording ->
                if (recording.filePath.isEmpty() || !fileSystem.exists(recording.filePath)) {
                    val recordingsDir = "${fileSystem.getFilesDir()}/subjects/$subjectId/recordings"
                    val fileName = "rec_${recording.id}.m4a"
                    val healedPath = "$recordingsDir/$fileName"
                    
                    if (fileSystem.exists(healedPath)) {
                        Napier.i { "Repository: Healed path for recording ${recording.id}" }
                        recording.copy(filePath = healedPath)
                    } else {
                        Napier.w { "Repository: File still missing for ${recording.id} at $healedPath" }
                        recording
                    }
                } else {
                    recording
                }
            }
        }
    }

    override suspend fun loadRecordings(subjectId: String) {
        withContext(Dispatchers.IO) {
            val path = getMetadataPath(subjectId)
            val content = fileSystem.readFile(path) ?: return@withContext
            try {
                val list = json.decodeFromString(ListSerializer(Recording.serializer()), content)
                _recordings.update { current ->
                    current + (subjectId to list)
                }
                Napier.i { "Loaded ${list.size} recordings for subject $subjectId" }
            } catch (e: Exception) {
                Napier.e(e) { "Failed to decode recordings metadata" }
            }
        }
    }

    override suspend fun saveRecording(subjectId: String, recording: Recording): KmpResult<Recording> {
        return withContext(Dispatchers.IO) {
            val sourcePath = recording.filePath
            
            val recordingsDir = "${fileSystem.getFilesDir()}/subjects/$subjectId/recordings"
            fileSystem.makeDir(recordingsDir)
            
            val fileName = "rec_${recording.id}.m4a"
            val destPath = "$recordingsDir/$fileName"
            
            val finalRecording = if (sourcePath.isNotEmpty() && sourcePath != destPath && fileSystem.exists(sourcePath)) {
                if (fileSystem.moveFile(sourcePath, destPath)) {
                    Napier.i { "Repository: Moved file to $destPath" }
                    recording.copy(filePath = destPath)
                } else {
                    Napier.e { "Repository: Failed to move file from $sourcePath to $destPath" }
                    recording
                }
            } else if (sourcePath.isEmpty() && fileSystem.exists(destPath)) {
                Napier.i { "Repository: Recovered empty path from existing file $destPath" }
                recording.copy(filePath = destPath)
            } else {
                recording
            }

            if (finalRecording.filePath.isEmpty()) {
                Napier.e { "Repository: WARNING - Saving recording ${recording.id} with EMPTY path" }
            }

            _recordings.update { current ->
                val list = current[subjectId] ?: emptyList()
                val newList = list.filter { it.id != finalRecording.id } + finalRecording
                current + (subjectId to newList)
            }
            
            saveMetadata(subjectId)
            KmpResult.Success(finalRecording)
        }
    }

    override suspend fun deleteRecording(subjectId: String, recordingId: String): KmpResult<Unit> {
        val list = _recordings.value[subjectId] ?: return KmpResult.Success(Unit)
        val recording = list.find { it.id == recordingId }
        
        if (recording != null) {
            fileSystem.deleteFile(recording.filePath)
            _recordings.update { current ->
                current + (subjectId to list.filter { it.id != recordingId })
            }
            saveMetadata(subjectId)
        }
        return KmpResult.Success(Unit)
    }

    private fun saveMetadata(subjectId: String) {
        val list = _recordings.value[subjectId] ?: return
        val content = json.encodeToString(ListSerializer(Recording.serializer()), list)
        fileSystem.writeFile(getMetadataPath(subjectId), content)
    }

    private fun getMetadataPath(subjectId: String) = "${fileSystem.getFilesDir()}/subjects/$subjectId/recordings.json"
}
