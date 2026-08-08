package application.liedetector.engine.io.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import application.liedetector.engine.io.audio.AudioRecorder
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

class AndroidAudioRecorder(
    private val context: Context,
    private val scope: CoroutineScope
) : AudioRecorder {

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var timerJob: Job? = null

    private val _isRecording = MutableStateFlow(false)
    override val isRecording = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    override val isPaused = _isPaused.asStateFlow()

    private val _durationMillis = MutableStateFlow(0L)
    override val durationMillis = _durationMillis.asStateFlow()

    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    override val amplitudes = _amplitudes.asStateFlow()

    private var startTime = 0L
    private var pausedTime = 0L

    override fun start() {
        if (_isRecording.value) return

        try {
            val file = File(context.cacheDir, "recording_${UUID.randomUUID()}.m4a")
            audioFile = file

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            _isRecording.value = true
            _isPaused.value = false
            _durationMillis.value = 0L
            _amplitudes.value = emptyList()
            startTime = System.currentTimeMillis()
            startTimer()
        } catch (e: Exception) {
            Napier.e(e) { "Failed to start recording" }
        }
    }

    override fun pause() {
        if (!_isRecording.value || _isPaused.value) return
        try {
            recorder?.pause()
            _isPaused.value = true
            pausedTime = System.currentTimeMillis()
            timerJob?.cancel()
        } catch (e: Exception) {
            Napier.e(e) { "Failed to pause recording" }
        }
    }

    override fun resume() {
        if (!_isRecording.value || !_isPaused.value) return
        try {
            recorder?.resume()
            _isPaused.value = false
            startTime += (System.currentTimeMillis() - pausedTime)
            startTimer()
        } catch (e: Exception) {
            Napier.e(e) { "Failed to resume recording" }
        }
    }

    override fun stop(): String? {
        if (!_isRecording.value) return null
        return try {
            timerJob?.cancel()
            recorder?.stop()
            recorder?.release()
            recorder = null
            _isRecording.value = false
            _isPaused.value = false
            audioFile?.absolutePath
        } catch (e: Exception) {
            Napier.e(e) { "Failed to stop recording" }
            null
        }
    }

    override fun cancel() {
        stop()
        audioFile?.delete()
        audioFile = null
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                _durationMillis.value = System.currentTimeMillis() - startTime
                
                // Get amplitude for visualization (0.0 to 1.0)
                val maxAmp = recorder?.maxAmplitude ?: 0
                val normalized = (maxAmp.toFloat() / 32767f).coerceIn(0f, 1f)
                
                val currentList = _amplitudes.value.toMutableList()
                currentList.add(normalized)
                if (currentList.size > 100) currentList.removeAt(0)
                _amplitudes.value = currentList
                
                delay(100.milliseconds)
            }
        }
    }
}
