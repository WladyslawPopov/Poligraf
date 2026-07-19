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

tasks.withType<JavaExec> {
    // Local development environment setup
    // Using rootProject.file ensures the path is absolute and reachable from anywhere
    val gcpKeyFile = rootProject.file("gcp-key.json")
    
    environment("APP_ENV", "dev") //prod or dev
    environment("DB_URL", "jdbc:postgresql://localhost:5432/liedetector")
    environment("DB_URL_TEST", "jdbc:postgresql://localhost:5432/liedetector_test")
    environment("DB_USER", "krampus")
    environment("DB_PASSWORD", "password123")
    environment("GOOGLE_APPLICATION_CREDENTIALS", gcpKeyFile.absolutePath)
    environment("GCP_PROJECT_ID", "lie-detector-72fc9")
}

dependencies {
    api(projects.core)
    api(projects.uiWidgets)
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
