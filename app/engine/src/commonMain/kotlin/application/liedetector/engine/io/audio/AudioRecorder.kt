package application.liedetector.engine.io.audio

import kotlinx.coroutines.flow.StateFlow

interface AudioRecorder {
    val isRecording: StateFlow<Boolean>
    val isPaused: StateFlow<Boolean>
    val durationMillis: StateFlow<Long>
    val amplitudes: StateFlow<List<Float>>
    val playbackPositionMillis: StateFlow<Long>
    val isPlaying: StateFlow<Boolean>

    fun start()
    fun pause()
    fun resume()
    fun stop(): String? // Returns file path
    fun cancel()

    fun play()
    fun pausePlayback()
    fun seekTo(positionMillis: Long)
    fun trim(startMillis: Long, endMillis: Long): String?
    fun replace(positionMillis: Long)
    fun loadFile(path: String)
}
