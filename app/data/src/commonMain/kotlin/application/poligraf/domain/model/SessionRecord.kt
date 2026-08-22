package application.poligraf.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionRecord(
    val id: String,
    val title: String,
    val timestampEpochMillis: Long,
    val durationMillis: Long,
    val markers: List<StressMarker>,
    val aggregates: AcousticAggregates,
    val notes: String = ""
)

@Serializable
data class StressMarker(
    val timestampMillis: Long,
    val type: MarkerType,
    val intensity: Float
)

@Serializable
enum class MarkerType {
    FEAR, STRESS, AGGRESSION, COMBINED
}

@Serializable
data class AcousticAggregates(
    val avgRms: Float,
    val avgPitch: Float,
    val avgJitter: Float,
    val maxRms: Float,
    val maxPitch: Float,
    val maxJitter: Float,
    val volatility: Float
)
