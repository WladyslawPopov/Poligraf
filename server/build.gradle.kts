plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
}

group = "application.liedetector"
version = "1.0.0"
application {
    mainClass = "application.liedetector.ApplicationKt"
}

dependencies {
    api(projects.core)
    implementation(libs.logback)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverCore)
    // Add ktor-serialization-kotlinx-json if needed for server
}
