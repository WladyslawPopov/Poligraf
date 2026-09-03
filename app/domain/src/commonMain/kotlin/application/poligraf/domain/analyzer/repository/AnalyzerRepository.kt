package application.poligraf.domain.analyzer.repository

import application.poligraf.domain.analyzer.model.AudioFrame
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for controlling real-time voice stress analysis.
 */
interface AnalyzerRepository {
    val currentFrame: StateFlow<AudioFrame?>
    val audioFrames: SharedFlow<AudioFrame>
    val isAnalyzing: StateFlow<Boolean>
    val isPaused: StateFlow<Boolean>
    val durationMillis: StateFlow<Long>

    fun startAnalysis(title: String = ""): String
    fun pauseAnalysis()
    fun resumeAnalysis()
    fun resumeFromDraft(sessionId: String, lastDuration: Long)
    suspend fun stopAnalysis(save: Boolean = true, anomalyCount: Long = 0L)
    suspend fun getActiveDraft(): Pair<String, Long>?
    fun cleanUpDrafts()
    suspend fun getFramesForSession(sessionId: String): List<AudioFrame>
}
