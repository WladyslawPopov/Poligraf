package application.liedetector.engine.io.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/**
 * Robust Android Audio Recorder.
 * Follows Clean Architecture by delegating heavy processing to [AndroidAudioProcessor].
 */
class AndroidAudioRecorder(
    private val context: Context,
    private val scope: CoroutineScope
) : AudioRecorder {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var audioFile: File? = null 
    private var tempPartFile: File? = null 
    private var timerJob: Job? = null
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

    private var startTime = 0L

    override fun start() {
        if (_isRecording.value) return
        
        if (audioFile == null) {
            _durationMillis.value = 0L
            _amplitudes.value = emptyList()
            _playbackPositionMillis.value = 0L
        }
        
        tempPartFile = null
        startRecordingSession()
    }

    private fun startRecordingSession() {
        scope.launch {
            try {
                releaseRecorder() 
                stopPlayback()
                
                delay(150.milliseconds)
                
                val file = File(context.cacheDir, "part_${UUID.randomUUID()}.m4a")
                tempPartFile = file

                val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
                
                recorder = newRecorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(AudioConstants.SAMPLING_RATE)
                    setAudioEncodingBitRate(AudioConstants.BITRATE)
                    setOutputFile(file.absolutePath)
                    prepare()
                }

                recorder?.start()

                _isRecording.value = true
                _isPaused.value = false
                
                startTime = System.currentTimeMillis() - _durationMillis.value
                startTimer()
                Napier.d { "Recording started: ${file.name}" }
            } catch (e: Exception) {
                Napier.e(e) { "Failed to start recording session" }
                _isRecording.value = false
                releaseRecorder()
            }
        }
    }

    override fun pause() {
        if (!_isRecording.value || _isPaused.value) return
        try {
            recorder?.pause()
            _isPaused.value = true
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
            startTime = System.currentTimeMillis() - _durationMillis.value
            startTimer()
        } catch (e: Exception) {
            Napier.e(e) { "Failed to resume recording" }
        }
    }

    private fun releaseRecorder() {
        try {
            timerJob?.cancel()
            recorder?.apply {
                if (_isRecording.value) {
                    try { stop() } catch (_: Exception) {}
                }
                release()
            }
            recorder = null
        } catch (e: Exception) {
            Napier.e(e) { "Error releasing recorder" }
        }
    }

    override suspend fun stop(): String? {
        if (!_isRecording.value) return audioFile?.absolutePath
        return withContext(Dispatchers.IO) {
            try {
                timerJob?.cancel()
                
                val maxAmp = try { recorder?.maxAmplitude ?: 0 } catch (_: Exception) { 0 }
                val normalized = kotlin.math.sqrt(maxAmp.toFloat() / 32767f).coerceIn(0f, 1f)
                _amplitudes.update { it + normalized }

                recorder?.stop()
                recorder?.release()
                recorder = null

                val currentPart = tempPartFile
                val baseFile = audioFile

                if (currentPart != null && currentPart.exists()) {
                    if (baseFile == null || !baseFile.exists()) {
                        audioFile = currentPart
                    } else {
                        val merged = AndroidAudioProcessor.merge(baseFile, currentPart, context.cacheDir)
                        if (merged != null) {
                            if (baseFile.absolutePath.contains("cache")) baseFile.delete()
                            currentPart.delete()
                            audioFile = merged
                        } else {
                            audioFile = currentPart
                        }
                    }
                }
                tempPartFile = null

                audioFile?.let { file ->
                    val mp = MediaPlayer()
                    try {
                        mp.setDataSource(file.absolutePath)
                        mp.prepare()
                        _durationMillis.value = mp.duration.toLong()
                    } finally {
                        mp.release()
                    }
                }

                _isRecording.value = false
                _isPaused.value = false
                _playbackPositionMillis.value = _durationMillis.value
                
                audioFile?.absolutePath
            } catch (e: Exception) {
                Napier.e(e) { "Failed to stop recording" }
                _isRecording.value = false
                null
            }
        }
    }

    override fun cancel() {
        scope.launch {
            stop()
            audioFile?.let { if (it.absolutePath.contains("cache")) it.delete() }
            audioFile = null
            tempPartFile?.delete()
            tempPartFile = null
        }
    }

    override fun play() {
        if (_isRecording.value) return
        
        val currentFile = audioFile
        if (currentFile == null || !currentFile.exists()) {
            Napier.e { "AudioRecorder: Play failed. audioFile is ${if (currentFile == null) "NULL" else "MISSING"}" }
            return
        }
        
        val path = currentFile.absolutePath
        try {
            stopPlayback() 
            val newPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
            }
            player = newPlayer
            val duration = newPlayer.duration.toLong()
            val startPos = _playbackPositionMillis.value

            if (startPos > 0 && startPos < duration - 200) {
                newPlayer.seekTo(startPos.toInt())
            } else if (newPlayer.currentPosition >= duration - 200) {
                newPlayer.seekTo(0)
                _playbackPositionMillis.value = 0
            }
            
            newPlayer.setOnCompletionListener {
                _isPlaying.value = false
                playbackJob?.cancel()
                _playbackPositionMillis.value = duration
            }
            
            newPlayer.start()
            _isPlaying.value = true
            startPlaybackTimer()
        } catch (e: Exception) {
            Napier.e(e) { "AudioRecorder: Playback failed for $path" }
            _isPlaying.value = false
        }
    }

    override fun pausePlayback() {
        player?.pause()
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    override fun seekTo(positionMillis: Long) {
        player?.seekTo(positionMillis.toInt())
        _playbackPositionMillis.value = positionMillis
    }

    override suspend fun trim(startMillis: Long, endMillis: Long): String? {
        val sourcePath = audioFile?.absolutePath ?: return null
        val outPath = File(context.cacheDir, "trimmed_${UUID.randomUUID()}.m4a").absolutePath
        
        return withContext(Dispatchers.IO) {
            val success = AndroidAudioProcessor.trim(sourcePath, outPath, startMillis, endMillis)
            if (success) {
                val startIdx = (startMillis / AudioConstants.SAMPLE_RATE_MS).toInt().coerceIn(0, _amplitudes.value.size)
                val endIdx = (endMillis / AudioConstants.SAMPLE_RATE_MS).toInt().coerceIn(startIdx, _amplitudes.value.size)
                _amplitudes.value = _amplitudes.value.subList(startIdx, endIdx).toList()

                audioFile = File(outPath)
                _durationMillis.value = endMillis - startMillis
                _playbackPositionMillis.value = 0
                stopPlayback()
                outPath
            } else null
        }
    }

    override fun replace(positionMillis: Long) {
        if (_isRecording.value) return
        scope.launch {
            val currentDuration = _durationMillis.value
            if (positionMillis <= 0L) {
                _durationMillis.value = 0
                _amplitudes.value = emptyList()
                audioFile = null
            } else if (positionMillis < currentDuration) {
                trim(0, positionMillis)
            } else {
                _playbackPositionMillis.value = currentDuration
            }
            
            val keepIdx = (positionMillis / AudioConstants.SAMPLE_RATE_MS).toInt().coerceIn(0, _amplitudes.value.size)
            _amplitudes.value = _amplitudes.value.take(keepIdx)
            _durationMillis.value = positionMillis.coerceAtLeast(0)
            
            startRecordingSession()
        }
    }

    override suspend fun loadFile(path: String, amplitudes: List<Float>?) {
        Napier.i { "AudioRecorder: Loading file from $path" }
        withContext(Dispatchers.Main) {
            stopPlayback()
            releaseRecorder()
        }
        
        withContext(Dispatchers.IO) {
            val file = File(path)
            if (!file.exists() || file.length() <= 0L) {
                Napier.e { "AudioRecorder: Load failed. File missing or empty. Path: $path" }
                audioFile = null
                _durationMillis.value = 0
                _amplitudes.value = emptyList()
                return@withContext
            }

            audioFile = file
            _playbackPositionMillis.value = 0
            
            try {
                val mp = MediaPlayer()
                mp.setDataSource(file.absolutePath)
                mp.prepare()
                val totalDuration = mp.duration.toLong()
                mp.release()
                
                _durationMillis.value = totalDuration

                if (!amplitudes.isNullOrEmpty()) {
                    _amplitudes.value = amplitudes
                } else {
                    val sampleCount = (totalDuration / AudioConstants.SAMPLE_RATE_MS).toInt().coerceAtLeast(1)
                    _amplitudes.value = List(sampleCount) { index ->
                        val base = 0.15f
                        val vari = kotlin.math.sin(index.toDouble() / 10.0).toFloat() * 0.1f
                        (base + vari + (Random.nextFloat() * 0.05f)).coerceIn(0.05f, 0.8f)
                    }
                }
                Napier.i { "AudioRecorder: Load SUCCESS. Duration: $totalDuration ms" }
            } catch (e: Exception) {
                Napier.e(e) { "AudioRecorder: MediaPlayer failed to prepare for $path" }
                _durationMillis.value = 0
                _amplitudes.value = emptyList()
                audioFile = null
            }
        }
    }

    private fun stopPlayback() {
        try {
            player?.stop()
            player?.release()
        } catch (_: Exception) {}
        player = null
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            var lastSampleCount = (_durationMillis.value / AudioConstants.SAMPLE_RATE_MS).toInt()
            while (isActive) {
                val now = System.currentTimeMillis()
                val currentDuration = now - startTime
                _durationMillis.value = currentDuration
                
                val expectedSamples = (currentDuration / AudioConstants.SAMPLE_RATE_MS).toInt()
                val samplesToAdd = expectedSamples - lastSampleCount
                
                if (samplesToAdd > 0) {
                    val maxAmp = try { recorder?.maxAmplitude ?: 0 } catch (_: Exception) { 0 }
                    val normalized = kotlin.math.sqrt(maxAmp.toFloat() / 32767f).coerceIn(0f, 1f)
                    
                    _amplitudes.update { current ->
                        current + List(samplesToAdd) { normalized }
                    }
                    lastSampleCount = expectedSamples
                }
                
                delay(AudioConstants.SAMPLE_RATE_MS.milliseconds) 
            }
        }
    }

    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (isActive) {
                player?.let {
                    _playbackPositionMillis.value = it.currentPosition.toLong()
                }
                delay(AudioConstants.SAMPLE_RATE_MS.milliseconds) 
            }
        }
    }
}
