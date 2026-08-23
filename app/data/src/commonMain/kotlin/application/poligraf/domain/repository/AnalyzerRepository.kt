package application.poligraf.domain.repository

import application.poligraf.domain.model.AudioFrame
import kotlinx.coroutines.flow.StateFlow

interface AnalyzerRepository {
    val currentFrame: StateFlow<AudioFrame?>
    val isAnomalous: StateFlow<Boolean>
    val isRecording: StateFlow<Boolean>
    val isPaused: StateFlow<Boolean>
    val durationMillis: StateFlow<Long>
    
    fun startAnalysis()
    fun pauseAnalysis()
    fun resumeAnalysis()
    
    /**
     * Stops the analysis.
     * @param save If true, the session is marked as completed. If false, all draft data is deleted.
     */
    fun stopAnalysis(save: Boolean)

    /**
     * Checks if there is an unfinished session from a previous run.
     * Returns the sessionId and duration if found.
     */
    suspend fun getActiveDraft(): Pair<String, Long>?

    /**
     * Resumes an existing draft session.
     */
    fun resumeFromDraft(sessionId: String, lastDuration: Long)

    /**
     * Deletes all uncompleted sessions.
     */
    fun cleanUpDrafts()
}
