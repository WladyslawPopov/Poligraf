package application.poligraf.domain.analyzer.types

import kotlinx.serialization.Serializable

/**
 * Pure domain-level analysis status representing the continuous acoustic state
 * or hardware environment warning of the voice analyzer.
 */
@Serializable
enum class AnalysisStatus {
    WARMUP,
    WARMUP_ROOM,
    CLIPPING,
    LOW_SNR,
    CALM,
    MILD_FLUCTUATION,
    FEAR_SINGLE,
    STRESS_SINGLE,
    PRESSURE_SINGLE,
    PITCH_DROP,
    RMS_DROP,
    SUBDUED_TREMOR,
    SUBDUED_SPEECH,
    PANIC,
    AGGRESSION,
    CONFRONTATION,
    DISORGANIZATION
}
