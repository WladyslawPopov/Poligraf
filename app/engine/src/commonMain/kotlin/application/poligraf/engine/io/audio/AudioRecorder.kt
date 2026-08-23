package application.poligraf.engine.io.audio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Pure data source. Its only job is to provide a stream of PCM data.
 */
interface AudioRecorder {
    /**
     * Returns true if the hardware microphone is currently active.
     */
    val isCapturing: StateFlow<Boolean>
    
    /**
     * Stream of raw PCM data.
     */
    val rawAudioFlow: Flow<ShortArray>

    fun startCapture()
    fun stopCapture()
}
