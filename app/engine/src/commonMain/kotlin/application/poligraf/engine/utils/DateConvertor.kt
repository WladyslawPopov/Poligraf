package application.poligraf.engine.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun nowAsEpochSeconds(): Long {
    return Clock.System.now().epochSeconds
}

@OptIn(ExperimentalTime::class)
fun nowAsEpochMilliseconds(): Long {
    return Clock.System.now().toEpochMilliseconds()
}

@OptIn(ExperimentalTime::class)
fun getMinutesRemainingUntil(endTimestamp: Long): Long? {
    val endInstant = Instant.fromEpochSeconds(endTimestamp)
    val nowInstant = Clock.System.now()

    val duration = endInstant - nowInstant

    if (duration.isNegative()) return null

    return duration.inWholeMinutes
}

@OptIn(ExperimentalTime::class)
fun Instant.toCurrentSystemLocalDateTime(): LocalDateTime {
    return this.toLocalDateTime(TimeZone.currentSystemDefault())
}

@OptIn(ExperimentalTime::class)
fun Long.convertDateWithMinutes(): String {
    try {
        val instant = Instant.fromEpochSeconds(this)
        val localDateTime = instant.toCurrentSystemLocalDateTime()

        val day = localDateTime.day.toString().padStart(2, '0')
        val month = localDateTime.month.number.toString().padStart(2, '0')
        val year = localDateTime.year
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')

        return "$day.$month.$year $hour:$minute"
    } catch (_: Exception) {
        return ""
    }
}

@OptIn(ExperimentalTime::class)
fun Long.convertHoursAndMinutes(): String {
    try {
        val instant = Instant.fromEpochSeconds(this)
        val localDateTime = instant.toCurrentSystemLocalDateTime()

        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')

        return "$hour:$minute"
    } catch (_: Exception) {
        return ""
    }
}

@OptIn(ExperimentalTime::class)
fun Long.convertDateYear(): String {
    try {
        val instant = Instant.fromEpochSeconds(this)
        val localDateTime = instant.toCurrentSystemLocalDateTime()

        val day = localDateTime.day.toString().padStart(2, '0')
        val month = localDateTime.month.number.toString().padStart(2, '0')
        val year = localDateTime.year

        return "$day.$month.$year"
    } catch (_: Exception) {
        return ""
    }
}

@OptIn(ExperimentalTime::class)
fun Long.convertDateOnlyYear(): String {
    try {
        val instant = Instant.fromEpochSeconds(this)
        val localDateTime = instant.toCurrentSystemLocalDateTime()

        val year = localDateTime.year

        return "$year"
    } catch (_: Exception) {
        return ""
    }
}
