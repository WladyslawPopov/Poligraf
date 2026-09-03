package application.poligraf.domain.analyzer.types

import kotlinx.serialization.Serializable

@Serializable
enum class AnalyzerSkin {
    STATE_MAP,
    VOICE_RIBBON,
    EQUALIZER,
    RINGS
}
