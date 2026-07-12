plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
}

group = "application.liedetector"
version = "1.0.0"

application {
    mainClass.set("application.liedetector.ApplicationKt")
}

dependencies {
    api(projects.core)
    implementation(libs.logback)
    
    // Ktor Server
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    
    // Database (Exposed + Postgres)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.postgresql)
    implementation(libs.hikaricp)

    // AI (Google Vertex AI)
    implementation(libs.google.vertexai)
    
    // Firebase Admin
    implementation(libs.firebase.admin)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
