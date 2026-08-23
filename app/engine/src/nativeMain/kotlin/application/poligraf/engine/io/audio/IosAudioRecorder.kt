package application.poligraf.engine.io.audio

import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionAllowBluetooth
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.AVFAudio.setPreferredSampleRate


/**
 * Native implementation of [AudioRecorder] for iOS using AVAudioEngine.
 * This implementation captures raw PCM data directly in Kotlin/Native.
 */
internal class IosAudioRecorderImpl(
    private val scope: CoroutineScope
) : AudioRecorder {

    private val _isCapturing = MutableStateFlow(false)
    override val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _rawAudioFlow = MutableSharedFlow<ShortArray>(extraBufferCapacity = 64)
    override val rawAudioFlow = _rawAudioFlow.asSharedFlow()

    private val audioEngine = AVAudioEngine()

    @OptIn(ExperimentalForeignApi::class)
    override fun startCapture() {
        if (_isCapturing.value) return

        try {
            val session = AVAudioSession.sharedInstance()
            
            // Try to set preferred sample rate to match our constants
            session.setPreferredSampleRate(AudioConstants.SAMPLING_RATE.toDouble(), error = null)
            
            session.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                mode = AVAudioSessionModeMeasurement,
                options = AVAudioSessionCategoryOptionDefaultToSpeaker or AVAudioSessionCategoryOptionAllowBluetooth,
                error = null
            )
            session.setActive(true, error = null)

            val inputNode = audioEngine.inputNode
            val format = inputNode.inputFormatForBus(0u)
            
            Napier.d { "iOS Audio Input Format: $format" }

            // Ensure we are getting mono if possible, or handle multi-channel
            // The engine usually handles this if we request a specific format in installTapOnBus,
            // but let's use the node's native format to avoid engine-level resampling if not needed,
            // or we can request 44100 mono directly.
            
            inputNode.installTapOnBus(0u, 1024u, format) { buffer, _ ->
                if (buffer == null) return@installTapOnBus
                
                val frameCount = buffer.frameLength.toInt()
                val floatData = buffer.floatChannelData?.get(0) ?: return@installTapOnBus
                
                val shortArray = ShortArray(frameCount)
                for (i in 0 until frameCount) {
                    // Convert Float32 to Int16 with clipping
                    val sample = (floatData[i] * 32767.0f).toInt().coerceIn(-32768, 32767)
                    shortArray[i] = sample.toShort()
                }
                
                // Use tryEmit for efficiency, fallback to launch if buffer is full
                if (!_rawAudioFlow.tryEmit(shortArray)) {
                    scope.launch {
                        _rawAudioFlow.emit(shortArray)
                    }
                }
            }

            audioEngine.prepare()
            if (!audioEngine.startAndReturnError(null)) {
                Napier.e { "Failed to start AVAudioEngine" }
                _isCapturing.value = false
            }
            
            _isCapturing.value = true
        } catch (e: Exception) {
            Napier.e(e) { "Error starting iOS audio capture" }
            _isCapturing.value = false
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun stopCapture() {
        if (!_isCapturing.value) return
        
        try {
            audioEngine.inputNode.removeTapOnBus(0u)
            if (audioEngine.running) {
                audioEngine.stop()
            }
            _isCapturing.value = false
            
            val session = AVAudioSession.sharedInstance()
            session.setActive(false, error = null)
        } catch (e: Exception) {
            Napier.e(e) { "Error stopping iOS audio capture: ${e.message}" }
        }
    }
}
