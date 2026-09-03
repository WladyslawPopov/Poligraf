package application.poligraf.domain.analyzer.types

import kotlinx.serialization.Serializable

@Serializable
enum class MarkerShape {
    CIRCLE,
    STAR,
    DIAMOND,
    HEART
}
