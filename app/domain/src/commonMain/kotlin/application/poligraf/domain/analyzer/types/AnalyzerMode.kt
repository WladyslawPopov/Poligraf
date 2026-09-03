package application.poligraf.domain.analyzer.types

import kotlinx.serialization.Serializable

@Serializable
enum class AnalyzerMode {
    LIVE,
    REVIEW
}
