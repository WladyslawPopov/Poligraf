package application.poligraf.domain.analyzer.types

import kotlinx.serialization.Serializable

/**
 * Single source of truth for the displayed signal state.
 */
@Serializable
enum class SignalLevel {
    NONE,
    GLOW,
    ANOMALY,
    CRITICAL
}
