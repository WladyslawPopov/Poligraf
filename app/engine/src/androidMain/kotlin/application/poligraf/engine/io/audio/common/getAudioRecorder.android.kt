package application.poligraf.engine.io.audio.common

import application.poligraf.engine.io.audio.AndroidAudioRecorder
import application.poligraf.engine.io.audio.AudioRecorder
import kotlinx.coroutines.CoroutineScope

actual fun getAudioRecorder(scope: CoroutineScope): AudioRecorder = AndroidAudioRecorder(scope)
