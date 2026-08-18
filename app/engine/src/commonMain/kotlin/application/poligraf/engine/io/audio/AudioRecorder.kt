package application.poligraf.engine.io.audio

import kotlinx.coroutines.flow.StateFlow

interface AudioRecorder {
    val isRecording: StateFlow<Boolean>
    val isPaused: StateFlow<Boolean>
    val durationMillis: StateFlow<Long>
    val amplitudes: StateFlow<List<Float>>
    val playbackPositionMillis: StateFlow<Long>
    val isPlaying: StateFlow<Boolean>
    val stressLevel: StateFlow<Float>

    fun start()
    fun pause()
    fun resume()
    suspend fun stop(): String? // Returns file path
    fun cancel()

    fun play()
    fun pausePlayback()
    fun seekTo(positionMillis: Long)
    suspend fun trim(startMillis: Long, endMillis: Long): String?
    fun replace(positionMillis: Long)
    suspend fun loadFile(path: String, amplitudes: List<Float>? = null)
}
