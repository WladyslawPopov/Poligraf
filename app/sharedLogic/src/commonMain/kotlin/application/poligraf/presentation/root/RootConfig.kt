package application.poligraf.presentation.root

import kotlinx.serialization.Serializable

@Serializable
sealed interface RootConfig {
    @Serializable
    data object Main : RootConfig

    @Serializable
    data object Debug : RootConfig

    @Serializable
    data object History : RootConfig

    @Serializable
    data class HistoryDetail(val sessionId: String) : RootConfig

    @Serializable
    data object Analyzer : RootConfig
}
