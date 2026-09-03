package application.poligraf.domain.analyzer.types

import kotlinx.serialization.Serializable

/**
 * Pure domain-level analysis status representing the continuous psychological/physiological state
 * or hardware environment warning of the voice analyzer.
 */
@Serializable
enum class AnalysisStatus {
    WARMUP,
    CLIPPING,
    LOW_SNR,
    CALM,
    MILD_FLUCTUATION,
    FEAR_SINGLE,
    STRESS_SINGLE,
    PRESSURE_SINGLE,
    PANIC,
    AGGRESSION,
    CONFRONTATION,
    DISORGANIZATION
}
