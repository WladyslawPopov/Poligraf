package application.poligraf.engine.utils

import kotlinx.serialization.json.Json

val jsonSerializer = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}
