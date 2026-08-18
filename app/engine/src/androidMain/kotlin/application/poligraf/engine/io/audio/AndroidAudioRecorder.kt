package application.poligraf.engine.io.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import application.poligraf.engine.dsp.AudioAnalyzer
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/**
 * Science-based Android Audio Recorder using AudioRecord for PCM access.
 */
class AndroidAudioRecorder(
    private val context: Context,
    private val scope: CoroutineScope
) : AudioRecorder {

    private var audioRecord: AudioRecord? = null
    private var player: MediaPlayer? = null
    private var audioFile: File? = null
    private var recordingJob: Job? = null
    private var playbackJob: Job? = null

    // Engine State
    private val _isRecording = MutableStateFlow(false)
    override val isRecording = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    override val isPaused = _isPaused.asStateFlow()

    private val _durationMillis = MutableStateFlow(0L)
    override val durationMillis = _durationMillis.asStateFlow()

    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    override val amplitudes = _amplitudes.asStateFlow()

    private val _playbackPositionMillis = MutableStateFlow(0L)
    override val playbackPositionMillis = _playbackPositionMillis.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying = _isPlaying.asStateFlow()

    private val _stressLevel = MutableStateFlow(0f)
    override val stressLevel = _stressLevel.asStateFlow()

    private var startTime = 0L
    private val sampleRate = AudioConstants.SAMPLING_RATE
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

    @SuppressLint("MissingPermission")
    override fun start() {
        if (_isRecording.value) return
        
        stopPlayback()
        
        val file = File(context.cacheDir, "recording_${UUID.randomUUID()}.pcm")
        audioFile = file
        
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        audioRecord?.startRecording()
        _isRecording.value = true
        _isPaused.value = false
        _durationMillis.value = 0L
        _amplitudes.value = emptyList()
        
        startTime = System.currentTimeMillis()
        
        recordingJob = scope.launch(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize / 2)
            val pitchHistory = mutableListOf<Float>()
            FileOutputStream(file).use { output ->
                while (isActive && _isRecording.value && !_isPaused.value) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        // Write to file (raw PCM)
                        val byteBuffer = java.nio.ByteBuffer.allocate(read * 2)
                        byteBuffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
                        for (i in 0 until read) {
                            byteBuffer.putShort(buffer[i])
                            output.write(byteBuffer.array(), i * 2, 2)
                            byteBuffer.clear()
                        }
                        
                        // Analysis
                        val currentWindow = buffer.sliceArray(0 until read)
                        val rms = AudioAnalyzer.calculateRms(currentWindow)
                        _amplitudes.update { it + rms }
                        
                        val pitch = AudioAnalyzer.estimatePitch(currentWindow, sampleRate)
                        if (pitch in 50f..500f) {
                            pitchHistory.add(pitch)
                            if (pitchHistory.size > 20) pitchHistory.removeAt(0)
                            val jitter = AudioAnalyzer.calculateJitter(pitchHistory)
                            
                            // Heuristic stress score
                            val stress = (jitter * 10f + (rms * 0.2f)).coerceIn(0f, 1f)
                            _stressLevel.value = stress
                        }
                        
                        _durationMillis.value = System.currentTimeMillis() - startTime
                    }
                }
            }
        }
    }

    override fun pause() {
        _isPaused.value = true
        audioRecord?.stop()
    }

    override fun resume() {
        if (!_isRecording.value) return
        _isPaused.value = false
        startTime = System.currentTimeMillis() - _durationMillis.value
        audioRecord?.startRecording()
        // Note: The recordingJob should be handled to resume writing if it was stopped
        // For simplicity in MVP, we might just restart the job or keep it running and checking _isPaused
    }

    override suspend fun stop(): String? {
        _isRecording.value = false
        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        
        // For playback of PCM, we'd need a WAV header or AudioTrack. 
        // For now, let's just return the path.
        return audioFile?.absolutePath
    }

    override fun cancel() {
        scope.launch {
            stop()
            audioFile?.delete()
            audioFile = null
        }
    }

    override fun play() {
        // MediaPlayer cannot play raw PCM. In a real app we'd convert to WAV or use AudioTrack.
        Napier.w { "Playback of raw PCM not implemented in MVP yet" }
    }

    override fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    override fun seekTo(positionMillis: Long) {
        _playbackPositionMillis.value = positionMillis
    }

    override suspend fun trim(startMillis: Long, endMillis: Long): String? {
        // Raw PCM trimming is just byte-level slicing
        return null
    }

    override fun replace(positionMillis: Long) {
        // Not implemented for PCM yet
    }

    override suspend fun loadFile(path: String, amplitudes: List<Float>?) {
        audioFile = File(path)
        if (amplitudes != null) {
            _amplitudes.value = amplitudes
        }
    }

    private fun stopPlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }
}
