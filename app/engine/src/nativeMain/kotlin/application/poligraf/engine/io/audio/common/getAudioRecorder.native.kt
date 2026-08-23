package application.poligraf.engine.io.audio.common

import application.poligraf.engine.io.audio.AudioRecorder
import application.poligraf.engine.io.audio.IosAudioRecorderImpl
import kotlinx.coroutines.CoroutineScope

actual fun getAudioRecorder(scope: CoroutineScope): AudioRecorder = IosAudioRecorderImpl(scope)
