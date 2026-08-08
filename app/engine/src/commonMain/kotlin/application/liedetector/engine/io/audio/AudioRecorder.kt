package application.liedetector.engine.io.audio

import kotlinx.coroutines.flow.StateFlow

interface AudioRecorder {
    val isRecording: StateFlow<Boolean>
    val isPaused: StateFlow<Boolean>
    val durationMillis: StateFlow<Long>
    val amplitudes: StateFlow<List<Float>>

    fun start()
    fun pause()
    fun resume()
    fun stop(): String? // Returns file path
    fun cancel()
}
