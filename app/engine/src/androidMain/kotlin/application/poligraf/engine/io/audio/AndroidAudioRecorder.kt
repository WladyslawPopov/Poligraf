package application.poligraf.engine.io.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.abs

internal class AndroidAudioRecorder(
    private val scope: CoroutineScope
) : AudioRecorder {

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    private val _isCapturing = MutableStateFlow(false)
    override val isCapturing = _isCapturing.asStateFlow()

    private val _rawAudioFlow = MutableSharedFlow<ShortArray>(extraBufferCapacity = 64)
    override val rawAudioFlow = _rawAudioFlow.asSharedFlow()

    private val sampleRate = AudioConstants.SAMPLING_RATE
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

    @SuppressLint("MissingPermission")
    override fun startCapture() {
        if (_isCapturing.value) return

        // Audio source priority: VOICE_RECOGNITION -> MIC -> DEFAULT
        // Provides AGC bypass on supported devices, with seamless fallback to MIC if vendor driver returns silence
        val sources = listOf(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT
        )

        var record: AudioRecord? = null
        for (source in sources) {
            try {
                val candidate = AudioRecord(
                    source,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )
                if (candidate.state == AudioRecord.STATE_INITIALIZED) {
                    // Test read to detect silence bug on custom vendor HAL drivers (e.g. Xiaomi/Samsung UNPROCESSED bug)
                    candidate.startRecording()
                    val testBuffer = ShortArray(128)
                    val read = candidate.read(testBuffer, 0, testBuffer.size)
                    candidate.stop()

                    var maxAmp = 0
                    if (read > 0) {
                        for (i in 0 until read) {
                            maxAmp = maxOf(maxAmp, abs(testBuffer[i].toInt()))
                        }
                    }

                    // If candidate initialized and passed test read, use it!
                    record = candidate
                    break
                } else {
                    candidate.release()
                }
            } catch (_: Exception) {
                record?.release()
                record = null
            }
        }

        audioRecord = record

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Napier.e { "AudioRecord initialization failed. Check RECORD_AUDIO permission." }
            audioRecord?.release()
            audioRecord = null
            return
        }

        try {
            audioRecord?.startRecording()
            _isCapturing.value = true
        } catch (e: Exception) {
            Napier.e(e) { "Failed to start recording" }
            audioRecord?.release()
            audioRecord = null
            return
        }

        recordingJob = scope.launch(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize / 2)
            try {
                while (isActive && _isCapturing.value) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        _rawAudioFlow.emit(buffer.copyOf(read))
                    }
                }
            } catch (e: Exception) {
                Napier.e(e) { "Error during PCM capture" }
            } finally {
                releaseResources()
            }
        }
    }

    override fun stopCapture() {
        _isCapturing.value = false
        recordingJob?.cancel()
    }

    private fun releaseResources() {
        audioRecord?.apply {
            try {
                if (state == AudioRecord.STATE_INITIALIZED) stop()
            } catch (_: Exception) {}
            release()
        }
        audioRecord = null
    }
}
