package application.poligraf.engine.io.audio

import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.Foundation.NSLog

/**
 * A bridge implementation of AudioRecorder for iOS.
 * This class holds the state that the shared ViewModel observes,
 * but delegates the actual hardware work to a Swift-based engine.
 */
class IosAudioRecorder(
    private val scope: CoroutineScope
) : AudioRecorder {

    // --- State managed by Swift engine through the bridge ---

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    override val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _durationMillis = MutableStateFlow(0L)
    override val durationMillis: StateFlow<Long> = _durationMillis.asStateFlow()

    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    override val amplitudes: StateFlow<List<Float>> = _amplitudes.asStateFlow()

    private val internalAmplitudes = mutableListOf<Float>()

    private val _playbackPositionMillis = MutableStateFlow(0L)
    override val playbackPositionMillis: StateFlow<Long> = _playbackPositionMillis.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _stressLevel = MutableStateFlow(0f)
    override val stressLevel: StateFlow<Float> = _stressLevel.asStateFlow()

    // --- Bridge Logic ---

    /**
     * Protocol-like interface that Swift engine must implement
     */
    interface Delegate {
        fun start()
        fun pause()
        fun resume()
        suspend fun stop(): String?
        fun cancel()
        fun play()
        fun pausePlayback()
        fun seekTo(positionMillis: Long)
        suspend fun trim(startMillis: Long, endMillis: Long): String?
        fun replace(positionMillis: Long)
        suspend fun loadFile(path: String, amplitudes: List<Float>?)
    }

    private var delegate: Delegate? = null

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
        Napier.d { "IosAudioRecorder: Delegate hooked!" }
    }

    // --- Swift Helper Methods (to update state flows from Swift) ---

    fun updateRecordingState(recording: Boolean, paused: Boolean) {
        _isRecording.value = recording
        _isPaused.value = paused
    }

    fun updateDuration(millis: Long) {
        _durationMillis.value = millis
    }

    fun updatePlayback(playing: Boolean, positionMillis: Long) {
        _isPlaying.value = playing
        _playbackPositionMillis.value = positionMillis
    }

    fun updateStressLevel(level: Float) {
        _stressLevel.value = level
    }

    fun addAmplitude(amplitude: Float) {
        internalAmplitudes.add(amplitude)
        
        // Batch updates to the Flow to avoid excessive KMP bridge crossings and UI re-renders
        // Emitting every 3 samples (~100ms) is enough for "live" feel while being much more efficient
        if (internalAmplitudes.size % 3 == 0 || internalAmplitudes.size < 10) {
            _amplitudes.value = internalAmplitudes.toList()
        }
    }

    fun setAmplitudes(amplitudes: List<Float>) {
        internalAmplitudes.clear()
        internalAmplitudes.addAll(amplitudes)
        _amplitudes.value = internalAmplitudes.toList()
    }

    // --- AudioRecorder Interface Implementation (Delegating to Swift) ---

    override fun start() {
        delegate?.start()
    }

    override fun pause() {
        delegate?.pause()
    }

    override fun resume() {
        delegate?.resume()
    }

    override suspend fun stop(): String? {
        return delegate?.stop()
    }

    override fun cancel() {
        delegate?.cancel()
        internalAmplitudes.clear()
        _amplitudes.value = emptyList()
        _durationMillis.value = 0
        _playbackPositionMillis.value = 0
        _stressLevel.value = 0f
        _isRecording.value = false
        _isPaused.value = false
        _isPlaying.value = false
    }

    override fun play() {
        delegate?.play()
    }

    override fun pausePlayback() {
        delegate?.pausePlayback()
    }

    override fun seekTo(positionMillis: Long) {
        delegate?.seekTo(positionMillis)
        _playbackPositionMillis.value = positionMillis
    }

    override suspend fun trim(startMillis: Long, endMillis: Long): String? {
        return delegate?.trim(startMillis, endMillis)
    }

    override fun replace(positionMillis: Long) {
        delegate?.replace(positionMillis)
    }

    override suspend fun loadFile(path: String, amplitudes: List<Float>?) {
        delegate?.loadFile(path, amplitudes)
    }
}
