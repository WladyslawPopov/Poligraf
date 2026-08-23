package application.poligraf.engine.io.audio.common

import application.poligraf.engine.io.audio.AudioRecorder
import kotlinx.coroutines.CoroutineScope

expect fun getAudioRecorder(scope: CoroutineScope): AudioRecorder
