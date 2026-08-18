package application.poligraf.presentation.recordingHistory

import application.poligraf.data.recording.Recording
import application.poligraf.uicore.widgets.VoiceRecorder

fun Recording.toVoiceRecorder(): VoiceRecorder {
    return VoiceRecorder(
        id = id,
        title = title,
        filePath = filePath,
        durationMillis = durationMillis,
        amplitudes = amplitudes,
        status = VoiceRecorder.Status.FINISHED
    )
}

fun VoiceRecorder.toRecording(): Recording {
    return Recording(
        id = id,
        title = title,
        filePath = filePath ?: "",
        durationMillis = durationMillis,
        amplitudes = amplitudes,
        createdAt = 0L // Should be set by repository or passed
    )
}
