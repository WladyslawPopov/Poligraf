package application.liedetector.engine.utils

import kotlinx.datetime.Instant
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun getMinutesRemainingUntil(endTimestamp: Long): Long? {
    val endInstant = Instant.fromEpochSeconds(endTimestamp)
    val nowInstant = Clock.System.now()

    val duration = endInstant - nowInstant

    if (duration.isNegative()) return null

    return duration.inWholeMinutes
}
