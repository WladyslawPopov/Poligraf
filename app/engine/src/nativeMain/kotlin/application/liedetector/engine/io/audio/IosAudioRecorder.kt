package application.liedetector.engine.io.audio

import application.liedetector.engine.io.audio.AudioRecorder
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFAudio.AVAudioQualityHigh
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)
class IosAudioRecorder(
    private val scope: CoroutineScope
) : AudioRecorder {

    private var recorder: AVAudioRecorder? = null
    private var timerJob: Job? = null
    
    private val _isRecording = MutableStateFlow(false)
    override val isRecording = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    override val isPaused = _isPaused.asStateFlow()

    private val _durationMillis = MutableStateFlow(0L)
    override val durationMillis = _durationMillis.asStateFlow()

    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    override val amplitudes = _amplitudes.asStateFlow()

    override fun start() {
        if (_isRecording.value) return

        try {
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
            audioSession.setActive(true, error = null)

            val url = NSURL.fileURLWithPath(
                NSTemporaryDirectory() + "recording_${NSUUID().UUIDString()}.m4a"
            )

            @Suppress("UNCHECKED_CAST")
            val settings = mapOf<Any?, Any?>(
                AVFormatIDKey to kAudioFormatMPEG4AAC.toInt(),
                AVSampleRateKey to 44100.0,
                AVNumberOfChannelsKey to 1,
                AVEncoderAudioQualityKey to AVAudioQualityHigh.toInt()
            ) as Map<Any?, *>

            recorder = AVAudioRecorder(url, settings, null).apply {
                meteringEnabled = true
                prepareToRecord()
                record()
            }

            _isRecording.value = true
            _isPaused.value = false
            _durationMillis.value = 0L
            _amplitudes.value = emptyList()
            startTimer()
        } catch (e: Exception) {
            Napier.e(e) { "Failed to start recording on iOS" }
        }
    }

    override fun pause() {
        if (!_isRecording.value || _isPaused.value) return
        recorder?.pause()
        _isPaused.value = true
        timerJob?.cancel()
    }

    override fun resume() {
        if (!_isRecording.value || !_isPaused.value) return
        recorder?.record()
        _isPaused.value = false
        startTimer()
    }

    override fun stop(): String? {
        if (!_isRecording.value) return null
        timerJob?.cancel()
        val path = recorder?.url?.path
        recorder?.stop()
        recorder = null
        _isRecording.value = false
        _isPaused.value = false
        
        AVAudioSession.sharedInstance().setActive(false, error = null)
        
        return path
    }

    override fun cancel() {
        val path = stop()
        if (path != null) {
            NSFileManager.defaultManager.removeItemAtPath(path, error = null)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                recorder?.let {
                    _durationMillis.value = (it.currentTime * 1000).toLong()
                    
                    it.updateMeters()
                    val power = it.averagePowerForChannel(0u)
                    // Normalize power from -160..0 to 0..1
                    val normalized = ((power + 160f) / 160f).coerceIn(0f, 1f)
                    
                    val currentList = _amplitudes.value.toMutableList()
                    currentList.add(normalized)
                    if (currentList.size > 100) currentList.removeAt(0)
                    _amplitudes.value = currentList
                }
                delay(100)
            }
        }
    }
}
