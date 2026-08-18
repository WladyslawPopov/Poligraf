package application.poligraf.data

import kotlinx.serialization.Serializable

sealed class AppRoute {
    @Serializable
    data object Main : AppRoute()
    @Serializable
    data object Debug : AppRoute()
    @Serializable
    data class Recording(val subjectId: String) : AppRoute()
    @Serializable
    data class RecordingsHistory(val subjectId: String, val startRecording: Boolean = false) : AppRoute()
}
